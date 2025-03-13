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
import org.frizzlenpop.frizzlenEdit.utils.ServerPerformanceMonitor;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

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
        // Create Vector3 objects for min, max and offset
        Vector3 min = new Vector3(0, 0, 0);
        Vector3 max = new Vector3(clipboard.getWidth() - 1, clipboard.getHeight() - 1, clipboard.getLength() - 1);
        Vector3 offset = clipboard.getOrigin().multiply(-1);
        
        // Iterate through clipboard dimensions
        for (int x = 0; x <= max.getX(); x++) {
            for (int y = 0; y <= max.getY(); y++) {
                for (int z = 0; z <= max.getZ(); z++) {
                    Vector3 pos = new Vector3(x, y, z);
                    BlockData data = clipboard.getBlock(pos);
                    
                    if (data != null && (!noAir || !data.getMaterial().isAir())) {
                        Vector3 target = pos.add(offset);
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
        
        // Mark that processing is complete once all batches are done
        processingPool.shutdown();
    }
    
    private List<List<BlockEntry>> splitIntoBatches(List<BlockEntry> blocks, int batchSize) {
        List<List<BlockEntry>> batches = new ArrayList<>();
        int blockCount = blocks.size();
        
        for (int i = 0; i < blockCount; i += batchSize) {
            batches.add(new ArrayList<>(blocks.subList(i, Math.min(blockCount, i + batchSize))));
        }
        
        return batches;
    }
    
    private void startPasteTask() {
        final long startTime = System.currentTimeMillis();
        int currentDelay = initialDelay;
        
        pasteTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            private int consecutiveEmptyPolls = 0;
            private int currentBatchSize = initialBatchSize;
            private int currentTickDelay = initialDelay;
            
            @Override
            public void run() {
                if (!isRunning.get()) {
                    if (pasteTask != null) {
                        pasteTask.cancel();
                    }
                    return;
                }
                
                // Check if processing is complete and queue is empty
                if (processingPool.isTerminated() && batchQueue.isEmpty() && remainingBlocks.get() == 0) {
                    isRunning.set(false);
                    long duration = System.currentTimeMillis() - startTime;
                    player.sendMessage("§aOptimized paste completed in " + (duration / 1000.0) + " seconds.");
                    
                    if (pasteTask != null) {
                        pasteTask.cancel();
                    }
                    return;
                }
                
                try {
                    // Adjust batch size and delay based on server performance
                    adjustBatchSettings();
                    
                    // Try to get a batch, but don't block
                    OptimizedBatch batch = batchQueue.poll();
                    
                    if (batch == null) {
                        consecutiveEmptyPolls++;
                        
                        // If we've had many empty polls and processing is done, we're probably finished
                        if (consecutiveEmptyPolls > 20 && processingPool.isTerminated()) {
                            if (remainingBlocks.get() <= 0) {
                                isRunning.set(false);
                                long duration = System.currentTimeMillis() - startTime;
                                player.sendMessage("§aOptimized paste completed in " + (duration / 1000.0) + " seconds.");
                                
                                if (pasteTask != null) {
                                    pasteTask.cancel();
                                }
                            }
                        }
                        return;
                    }
                    
                    consecutiveEmptyPolls = 0;
                    
                    // Process the batch by chunks
                    int blocksProcessed = 0;
                    for (Map.Entry<ChunkCoordinate, List<BlockEntry>> entry : batch.blocksByChunk.entrySet()) {
                        List<BlockEntry> chunkBlocks = entry.getValue();
                        
                        // Make sure the chunk is loaded
                        int chunkX = entry.getKey().x;
                        int chunkZ = entry.getKey().z;
                        if (!world.isChunkLoaded(chunkX, chunkZ)) {
                            world.loadChunk(chunkX, chunkZ);
                        }
                        
                        // Place blocks in this chunk
                        for (BlockEntry blockEntry : chunkBlocks) {
                            Block block = blockEntry.getLocation().getBlock();
                            block.setBlockData(blockEntry.getBlockData(), false);
                            blocksProcessed++;
                        }
                        
                        // Apply physics updates if needed
                        if (batch.hasPhysicsBlocks) {
                            applyDeferredPhysics(chunkBlocks);
                        }
                    }
                    
                    // Update progress
                    blocksPlaced.addAndGet(blocksProcessed);
                    remainingBlocks.addAndGet(-blocksProcessed);
                    
                    // Report progress periodically
                    int totalPlaced = blocksPlaced.get();
                    int totalBlocks = allBlocks.size();
                    if (totalPlaced % 5000 < blocksProcessed || totalPlaced == totalBlocks) {
                        updateProgress(player, totalPlaced, totalBlocks, startTime);
                    }
                    
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error during paste operation", e);
                    player.sendMessage("§cError during paste: " + e.getMessage());
                    stop();
                }
            }
            
            private void adjustBatchSettings() {
                if (performanceMonitor != null) {
                    // Adjust based on server performance
                    double tps = performanceMonitor.getCurrentTps();
                    
                    // Dynamically adjust batch size
                    if (tps > 19.0) {
                        // Excellent performance, increase batch size
                        currentBatchSize = Math.min(MAX_BATCH_SIZE, (int)(currentBatchSize * 1.2));
                        currentTickDelay = Math.max(1, currentTickDelay - 1);
                    } else if (tps > 17.0) {
                        // Good performance, slight increase
                        currentBatchSize = Math.min(MAX_BATCH_SIZE, (int)(currentBatchSize * 1.1));
                    } else if (tps < 10.0) {
                        // Very poor performance, dramatically reduce
                        currentBatchSize = Math.max(MIN_BATCH_SIZE, currentBatchSize / 3);
                        currentTickDelay = Math.min(10, currentTickDelay + 2);
                    } else if (tps < 15.0) {
                        // Poor performance, reduce
                        currentBatchSize = Math.max(MIN_BATCH_SIZE, (int)(currentBatchSize * 0.7));
                        currentTickDelay = Math.min(5, currentTickDelay + 1);
                    }
                }
            }
        }, 0L, currentDelay);
    }
    
    private OptimizedBatch prepareOptimizedBatch(List<BlockEntry> blocks) {
        Map<ChunkCoordinate, List<BlockEntry>> blocksByChunk = new HashMap<>();
        boolean hasPhysicsBlocks = false;
        
        for (BlockEntry entry : blocks) {
            Location loc = entry.getLocation();
            ChunkCoordinate chunkCoord = new ChunkCoordinate(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
            
            // Group by chunk
            blocksByChunk.computeIfAbsent(chunkCoord, k -> new ArrayList<>()).add(entry);
            
            // Check if any blocks need physics updates
            if (!hasPhysicsBlocks && needsPhysicsUpdate(entry.getBlockData())) {
                hasPhysicsBlocks = true;
            }
        }
        
        return new OptimizedBatch(blocksByChunk, hasPhysicsBlocks);
    }
    
    private void applyDeferredPhysics(List<BlockEntry> chunkBlocks) {
        // Apply physics to blocks that need it
        for (BlockEntry entry : chunkBlocks) {
            if (needsPhysicsUpdate(entry.getBlockData())) {
                Block block = entry.getLocation().getBlock();
                // Just update the block to trigger physics
                block.getState().update(true, true);
            }
        }
    }
    
    private boolean needsPhysicsUpdate(BlockData data) {
        Material type = data.getMaterial();
        return type.hasGravity() 
            || type == Material.WATER 
            || type == Material.LAVA
            || type == Material.REDSTONE_WIRE
            || type.name().contains("DOOR")
            || type.name().contains("BUTTON");
    }
    
    private int getBlockPriority(Material material) {
        // Structural blocks first, then solids, then gravity-affected blocks, then everything else
        if (material.name().contains("COMMAND") || material.name().contains("STRUCTURE")) {
            return 0; // Special blocks first
        } else if (material.isSolid() && !material.hasGravity()) {
            return 1; // Solid blocks next
        } else if (material.hasGravity()) {
            return 3; // Gravity blocks later
        } else {
            return 2; // Everything else in between
        }
    }
    
    private void updateProgress(Player player, int blocksPlaced, int totalBlocks, long startTime) {
        long currentTime = System.currentTimeMillis();
        double progress = (double) blocksPlaced / totalBlocks;
        long elapsedTime = currentTime - startTime;
        
        // Only calculate estimated time if we've made some progress
        String timeInfo = "";
        if (progress > 0.05 && elapsedTime > 1000) {
            long estimatedTotalTime = (long) (elapsedTime / progress);
            long remainingTime = estimatedTotalTime - elapsedTime;
            
            // Format remaining time
            long remainingSeconds = remainingTime / 1000;
            if (remainingSeconds < 60) {
                timeInfo = String.format(" (ETA: %ds)", remainingSeconds);
            } else {
                timeInfo = String.format(" (ETA: %dm %ds)", remainingSeconds / 60, remainingSeconds % 60);
            }
        }
        
        // Send progress message
        player.sendMessage(String.format("§aPaste progress: §f%d/%d §7(%.1f%%)%s", 
                                        blocksPlaced, totalBlocks, progress * 100, timeInfo));
    }
} 