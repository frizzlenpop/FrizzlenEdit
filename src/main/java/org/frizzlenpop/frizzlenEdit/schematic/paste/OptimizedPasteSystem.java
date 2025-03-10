package org.frizzlenpop.frizzlenEdit.schematic.paste;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.monitoring.ServerPerformanceMonitor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class OptimizedPasteSystem {

    // For organizing blocks by chunk
    public static class ChunkCoordinate {
        final int x;
        final int z;
        
        public ChunkCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCoordinate that = (ChunkCoordinate) o;
            return x == that.x && z == that.z;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
    
    // Container for a block location and its data
    public static class BlockEntry {
        private final Location location;
        private final BlockData blockData;
        
        public BlockEntry(Location location, BlockData blockData) {
            this.location = location;
            this.blockData = blockData;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public BlockData getBlockData() {
            return blockData;
        }
    }
    
    // Prepared batch of blocks ready for placement
    public static class OptimizedBatch {
        final Map<ChunkCoordinate, List<BlockEntry>> blocksByChunk;
        final boolean hasPhysicsBlocks;
        
        public OptimizedBatch(Map<ChunkCoordinate, List<BlockEntry>> blocksByChunk, boolean hasPhysicsBlocks) {
            this.blocksByChunk = blocksByChunk;
            this.hasPhysicsBlocks = hasPhysicsBlocks;
        }
    }
    
    private final FrizzlenEdit plugin;
    private final Player player;
    private final Clipboard clipboard;
    private final World world;
    private final Location origin;
    private final boolean noAir;
    private final int initialBatchSize;
    private final int initialDelay;
    private final ServerPerformanceMonitor performanceMonitor;
    
    private final List<BlockEntry> allBlocks;
    private final AtomicBoolean isRunning;
    private final AtomicInteger blocksPlaced;
    private final AtomicInteger remainingBlocks;
    private ExecutorService processingPool;
    private BlockingQueue<OptimizedBatch> batchQueue;
    private BukkitTask pasteTask;
    
    private static final int MIN_BATCH_SIZE = 50;
    private static final int MAX_BATCH_SIZE = 5000;
    
    public OptimizedPasteSystem(FrizzlenEdit plugin, Player player, Clipboard clipboard, 
                              World world, Location origin, boolean noAir,
                              int initialBatchSize, int initialDelay,
                              ServerPerformanceMonitor performanceMonitor) {
        this.plugin = plugin;
        this.player = player;
        this.clipboard = clipboard;
        this.world = world;
        this.origin = origin;
        this.noAir = noAir;
        this.initialBatchSize = initialBatchSize;
        this.initialDelay = initialDelay;
        this.performanceMonitor = performanceMonitor;
        
        this.allBlocks = new ArrayList<>();
        this.isRunning = new AtomicBoolean(false);
        this.blocksPlaced = new AtomicInteger(0);
        this.remainingBlocks = new AtomicInteger(0);
        
        prepareAllBlocks();
    }
    
    private void prepareAllBlocks() {
        Vector min = clipboard.getMinimumPoint();
        Vector max = clipboard.getMaximumPoint();
        Vector offset = clipboard.getOrigin().clone().multiply(-1);
        
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Vector pos = new Vector(x, y, z);
                    BlockData data = clipboard.getBlock(pos);
                    
                    if (data != null && (!noAir || !data.getMaterial().isAir())) {
                        Vector target = pos.clone().add(offset);
                        Location location = origin.clone().add(target.getX(), target.getY(), target.getZ());
                        allBlocks.add(new BlockEntry(location, data));
                    }
                }
            }
        }
        
        // Sort all blocks initially by priority
        allBlocks.sort((a, b) -> {
            int priorityA = getBlockPriority(a.getBlockData().getMaterial());
            int priorityB = getBlockPriority(b.getBlockData().getMaterial());
            return Integer.compare(priorityA, priorityB);
        });
        
        remainingBlocks.set(allBlocks.size());
    }
    
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            blocksPlaced.set(0);
            processingPool = Executors.newFixedThreadPool(3);
            batchQueue = new LinkedBlockingQueue<>(10);
            
            startProcessingThreads();
            startPasteTask();
            
            player.sendMessage("§aStarted optimized paste with " + allBlocks.size() + 
                              " blocks using initial batch size of " + initialBatchSize);
        } else {
            player.sendMessage("§cA paste operation is already running!");
        }
    }
    
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (pasteTask != null) {
                pasteTask.cancel();
            }
            processingPool.shutdownNow();
            player.sendMessage("§cPaste operation stopped.");
        }
    }
    
    private void startProcessingThreads() {
        // Divide all blocks into initial chunks for processing
        List<List<BlockEntry>> initialBatches = splitIntoBatches(allBlocks, initialBatchSize);
        
        for (List<BlockEntry> batch : initialBatches) {
            processingPool.submit(() -> {
                OptimizedBatch optimizedBatch = prepareOptimizedBatch(batch);
                try {
                    batchQueue.put(optimizedBatch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    plugin.getLogger().log(Level.WARNING, "Thread interrupted while preparing batch", e);
                }
            });
        }
        
        // Submit a completed marker
        processingPool.submit(() -> {
            try {
                processingPool.shutdown();
                processingPool.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    private List<List<BlockEntry>> splitIntoBatches(List<BlockEntry> blocks, int batchSize) {
        List<List<BlockEntry>> batches = new ArrayList<>();
        int totalBlocks = blocks.size();
        
        for (int i = 0; i < totalBlocks; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalBlocks);
            batches.add(new ArrayList<>(blocks.subList(i, endIndex)));
        }
        
        return batches;
    }
    
    private void startPasteTask() {
        final AtomicInteger consecutiveGoodPerformance = new AtomicInteger(0);
        final AtomicInteger consecutivePoorPerformance = new AtomicInteger(0);
        final AtomicInteger currentBatchSize = new AtomicInteger(initialBatchSize);
        final AtomicInteger currentDelay = new AtomicInteger(initialDelay);
        final long startTime = System.currentTimeMillis();
        
        Runnable pasteRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning.get()) {
                    pasteTask.cancel();
                    return;
                }
                
                // Check if we're done
                if (batchQueue.isEmpty() && (processingPool.isTerminated() || processingPool.isShutdown())) {
                    isRunning.set(false);
                    pasteTask.cancel();
                    long duration = System.currentTimeMillis() - startTime;
                    player.sendMessage(String.format("§aPaste completed: %d blocks in %.2f seconds", 
                                                   blocksPlaced.get(), duration / 1000.0));
                    return;
                }
                
                // Get next batch if available
                OptimizedBatch batch = batchQueue.poll();
                if (batch == null) {
                    return; // Wait for next tick
                }
                
                int blocksPastedInThisBatch = 0;
                
                // Process this batch by chunks
                for (Map.Entry<ChunkCoordinate, List<BlockEntry>> entry : batch.blocksByChunk.entrySet()) {
                    List<BlockEntry> chunkBlocks = entry.getValue();
                    
                    // Place blocks with physics disabled
                    for (BlockEntry blockEntry : chunkBlocks) {
                        Location loc = blockEntry.getLocation();
                        Block block = loc.getBlock();
                        BlockData data = blockEntry.getBlockData();
                        
                        block.setBlockData(data, false); // No physics updates
                        blocksPlaced.incrementAndGet();
                        remainingBlocks.decrementAndGet();
                        blocksPastedInThisBatch++;
                    }
                    
                    // Apply deferred physics for this chunk if needed
                    if (batch.hasPhysicsBlocks) {
                        applyDeferredPhysics(chunkBlocks);
                    }
                }
                
                // Update progress for the player
                updateProgress(player, blocksPlaced.get(), allBlocks.size(), startTime);
                
                // Adjust batch size and delay based on performance
                double performanceFactor = performanceMonitor.getPerformanceFactor();
                
                if (performanceFactor > 0.95) {
                    consecutiveGoodPerformance.incrementAndGet();
                    consecutivePoorPerformance.set(0);
                    
                    // Progressive increase for sustained good performance
                    if (consecutiveGoodPerformance.get() > 3) {
                        int newSize = (int)(currentBatchSize.get() * 1.5);
                        currentBatchSize.set(Math.min(newSize, MAX_BATCH_SIZE));
                        
                        // Also decrease delay if possible
                        if (currentDelay.get() > 1) {
                            currentDelay.decrementAndGet();
                        }
                        
                        consecutiveGoodPerformance.set(0);
                        
                        // Log the adaptive adjustment
                        plugin.getLogger().info("Performance is good. Increasing batch size to " + 
                                               currentBatchSize.get() + " and setting delay to " + 
                                               currentDelay.get());
                        
                        // Update task delay - fix for rescheduling
                        if (isRunning.get()) {
                            pasteTask.cancel();
                            pasteTask = plugin.getServer().getScheduler().runTaskTimer(plugin, 
                                this, 0, currentDelay.get());
                        }
                    }
                } else if (performanceFactor < 0.8) {
                    consecutivePoorPerformance.incrementAndGet();
                    consecutiveGoodPerformance.set(0);
                    
                    // Reduce batch size for poor performance
                    if (consecutivePoorPerformance.get() > 2) {
                        int newSize = (int)(currentBatchSize.get() * 0.7);
                        currentBatchSize.set(Math.max(newSize, MIN_BATCH_SIZE));
                        
                        // Increase delay
                        currentDelay.incrementAndGet();
                        
                        consecutivePoorPerformance.set(0);
                        
                        // Log the adaptive adjustment
                        plugin.getLogger().info("Performance is poor. Decreasing batch size to " + 
                                               currentBatchSize.get() + " and setting delay to " + 
                                               currentDelay.get());
                        
                        // Update task delay - fix for rescheduling
                        if (isRunning.get()) {
                            pasteTask.cancel();
                            pasteTask = plugin.getServer().getScheduler().runTaskTimer(plugin, 
                                this, 0, currentDelay.get());
                        }
                    }
                }
                
                // Fix 3: Avoid concurrent modification when submitting more batches
                if (!processingPool.isShutdown() && !processingPool.isTerminated() && 
                    batchQueue.size() < 3 && remainingBlocks.get() > 0) {
                    
                    // Create a new list to avoid concurrent modification
                    List<BlockEntry> remainingBlocksList = new ArrayList<>();
                    int remainingCount = remainingBlocks.get();
                    
                    synchronized (allBlocks) {
                        // Get the remaining blocks safely
                        int startIndex = Math.max(0, allBlocks.size() - remainingCount);
                        for (int i = startIndex; i < allBlocks.size() && remainingBlocksList.size() < currentBatchSize.get(); i++) {
                            remainingBlocksList.add(allBlocks.get(i));
                        }
                    }
                    
                    if (!remainingBlocksList.isEmpty()) {
                        processingPool.submit(() -> {
                            OptimizedBatch optimizedBatch = prepareOptimizedBatch(remainingBlocksList);
                            try {
                                batchQueue.put(optimizedBatch);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                }
            }
        };
        
        pasteTask = plugin.getServer().getScheduler().runTaskTimer(plugin, pasteRunnable, initialDelay, currentDelay.get());
    }
    
    private OptimizedBatch prepareOptimizedBatch(List<BlockEntry> blocks) {
        // Group blocks by chunk
        Map<ChunkCoordinate, List<BlockEntry>> blocksByChunk = new HashMap<>();
        boolean hasPhysicsBlocks = false;
        
        for (BlockEntry entry : blocks) {
            Location loc = entry.getLocation();
            ChunkCoordinate chunkCoord = new ChunkCoordinate(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
            
            if (!blocksByChunk.containsKey(chunkCoord)) {
                blocksByChunk.put(chunkCoord, new ArrayList<>());
            }
            blocksByChunk.get(chunkCoord).add(entry);
            
            // Check if this block might need physics updates
            if (needsPhysicsUpdate(entry.getBlockData())) {
                hasPhysicsBlocks = true;
            }
        }
        
        // Sort blocks within each chunk by priority
        for (List<BlockEntry> chunkBlocks : blocksByChunk.values()) {
            chunkBlocks.sort((a, b) -> {
                int priorityA = getBlockPriority(a.getBlockData().getMaterial());
                int priorityB = getBlockPriority(b.getBlockData().getMaterial());
                return Integer.compare(priorityA, priorityB);
            });
        }
        
        return new OptimizedBatch(blocksByChunk, hasPhysicsBlocks);
    }
    
    private void applyDeferredPhysics(List<BlockEntry> chunkBlocks) {
        // Apply physics only to blocks that need it
        for (BlockEntry entry : chunkBlocks) {
            if (needsPhysicsUpdate(entry.getBlockData())) {
                Block block = entry.getLocation().getBlock();
                block.getState().update(true, true);
            }
        }
    }
    
    private boolean needsPhysicsUpdate(BlockData data) {
        Material material = data.getMaterial();
        // List of materials that typically need physics updates
        return material == Material.SAND || material == Material.GRAVEL || 
               material.name().contains("DOOR") || material.name().contains("BUTTON") ||
               material.name().contains("PRESSURE_PLATE") || material.name().contains("REDSTONE") ||
               material == Material.WATER || material == Material.LAVA;
    }
    
    private int getBlockPriority(Material material) {
        // Lower number = higher priority
        if (material.isSolid() && material.isBlock() && !material.isTransparent()) {
            return 1; // Solid structure blocks first
        } else if (material.isSolid() && material.isBlock()) {
            return 2; // Other solid blocks
        } else if (material == Material.WATER || material == Material.LAVA) {
            return 4; // Fluids later
        } else if (material.isTransparent()) {
            return 3; // Transparent blocks
        }
        return 5; // Everything else
    }
    
    private void updateProgress(Player player, int blocksPlaced, int totalBlocks, long startTime) {
        if (blocksPlaced % 1000 == 0 || blocksPlaced == totalBlocks) {
            long duration = System.currentTimeMillis() - startTime;
            double seconds = duration / 1000.0;
            double blocksPerSecond = seconds > 0 ? blocksPlaced / seconds : 0;
            
            String progressMessage = String.format(
                "§aPaste progress: §f%d/%d blocks §7(%.1f%%, %.1f blocks/sec)", 
                blocksPlaced, totalBlocks, 
                (blocksPlaced * 100.0) / totalBlocks,
                blocksPerSecond
            );
            
            // Use regular chat message instead of action bar which might not be available in all versions
            player.sendMessage(progressMessage);
        }
    }
} 