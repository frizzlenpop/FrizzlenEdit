package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.ServerPerformanceMonitor;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An operation that pastes blocks from a clipboard in batches to minimize performance impact.
 * Uses adaptive batch sizing based on server performance.
 */
public class BatchPasteOperation implements Operation {
    private final FrizzlenEdit plugin;
    private final Player player;
    private final Vector3 position;
    private final Clipboard clipboard;
    private final boolean ignoreAir;
    private final World world;
    
    // Batch processing settings
    private final int baseBatchSize;
    private final int baseTickDelay;
    private final String operationName;
    
    // Adaptive settings
    private int currentBatchSize;
    private int currentTickDelay;
    
    // Default batch settings
    private static final int DEFAULT_BATCH_SIZE = 1000; // Blocks per batch
    private static final int DEFAULT_TICK_DELAY = 1;    // Ticks between batches
    
    // Performance adjustment interval (in batches)
    private static final int PERFORMANCE_CHECK_INTERVAL = 5;
    
    /**
     * Create a new batch paste operation.
     * @param plugin The plugin instance
     * @param player The player
     * @param position The position to paste at
     * @param clipboard The clipboard to paste from
     * @param ignoreAir Whether to ignore air blocks
     * @param batchSize The number of blocks to process per batch
     * @param tickDelay The number of ticks to wait between batches
     * @param operationName The name of the operation
     */
    public BatchPasteOperation(FrizzlenEdit plugin, Player player, Vector3 position, Clipboard clipboard, 
                               boolean ignoreAir, int batchSize, int tickDelay, String operationName) {
        this.plugin = plugin;
        this.player = player;
        this.position = position;
        this.clipboard = clipboard;
        this.ignoreAir = ignoreAir;
        this.world = player.getWorld();
        this.baseBatchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        this.baseTickDelay = tickDelay > 0 ? tickDelay : DEFAULT_TICK_DELAY;
        this.operationName = operationName != null ? operationName : "Batch Paste";
        
        // Initialize adaptive settings
        ServerPerformanceMonitor monitor = ServerPerformanceMonitor.getInstance();
        if (monitor != null) {
            this.currentBatchSize = monitor.getAdaptiveBatchSize(this.baseBatchSize);
            this.currentTickDelay = monitor.getAdaptiveTickDelay(this.baseTickDelay);
        } else {
            this.currentBatchSize = this.baseBatchSize;
            this.currentTickDelay = this.baseTickDelay;
        }
    }
    
    /**
     * Create a new batch paste operation with default batch settings.
     * @param plugin The plugin instance
     * @param player The player
     * @param position The position to paste at
     * @param clipboard The clipboard to paste from
     * @param ignoreAir Whether to ignore air blocks
     */
    public BatchPasteOperation(FrizzlenEdit plugin, Player player, Vector3 position, Clipboard clipboard, boolean ignoreAir) {
        this(plugin, player, position, clipboard, ignoreAir, DEFAULT_BATCH_SIZE, DEFAULT_TICK_DELAY, "Batch Paste");
    }
    
    @Override
    public HistoryEntry execute() {
        // Get the blocks from the clipboard
        Map<Vector3, BlockData> blocks = clipboard.getBlocks();
        
        // Filter air blocks if needed and optimize the block list
        List<Map.Entry<Vector3, BlockData>> blocksList = optimizeBlockList(blocks);
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, operationName);
        
        // Send initial message
        int totalBlocks = blocksList.size();
        player.sendMessage(ChatColor.YELLOW + "Starting batch paste operation: " + totalBlocks + " blocks");
        player.sendMessage(ChatColor.YELLOW + "Initial batch size: " + currentBatchSize + ", delay: " + currentTickDelay + " tick(s)");
        
        // Process blocks in batches
        processBatches(blocksList, entry, totalBlocks);
        
        return entry;
    }
    
    /**
     * Optimize the block list by filtering air blocks if needed and sorting by Y coordinate
     * to improve performance and reduce memory reallocation.
     * 
     * @param blocks The map of blocks from the clipboard
     * @return An optimized list of block entries
     */
    private List<Map.Entry<Vector3, BlockData>> optimizeBlockList(Map<Vector3, BlockData> blocks) {
        // Pre-allocate the full size to avoid reallocations
        List<Map.Entry<Vector3, BlockData>> blocksList = new ArrayList<>(
            ignoreAir ? (int)(blocks.size() * 0.8) : blocks.size()
        );
        
        // Filter out air blocks if needed
        for (Map.Entry<Vector3, BlockData> entry : blocks.entrySet()) {
            if (!ignoreAir || !entry.getValue().getMaterial().isAir()) {
                blocksList.add(entry);
            }
        }
        
        // Sort blocks by Y coordinate (bottom to top)
        // This can improve performance by minimizing block updates
        blocksList.sort((a, b) -> {
            int cmp = Integer.compare(a.getKey().getY(), b.getKey().getY());
            if (cmp != 0) return cmp;
            
            // Secondary sort by X and Z for more localized changes
            cmp = Integer.compare(a.getKey().getX(), b.getKey().getX());
            if (cmp != 0) return cmp;
            
            return Integer.compare(a.getKey().getZ(), b.getKey().getZ());
        });
        
        return blocksList;
    }
    
    /**
     * Process blocks in batches with adaptive sizing based on server performance.
     * 
     * @param blocksList The list of blocks to process
     * @param entry The history entry
     * @param totalBlocks The total number of blocks
     */
    private void processBatches(List<Map.Entry<Vector3, BlockData>> blocksList, HistoryEntry entry, int totalBlocks) {
        final Iterator<Map.Entry<Vector3, BlockData>> iterator = blocksList.iterator();
        final AtomicInteger blocksProcessed = new AtomicInteger(0);
        final long startTime = System.currentTimeMillis();
        
        // Batch counter for progress reporting and performance adjustment
        final AtomicInteger batchCounter = new AtomicInteger(0);
        final int initialTotalBatches = (int) Math.ceil((double) totalBlocks / currentBatchSize);
        
        // Progress reporting interval (report every 10% progress)
        final int progressReportInterval = Math.max(1, initialTotalBatches / 10);
        
        // Performance monitoring
        final AtomicLong lastPerformanceCheckTime = new AtomicLong(System.currentTimeMillis());
        
        // Create a map to cache block states before setting them to reduce object creation
        final Map<Vector3, BlockState> blockStateCache = new HashMap<>();
        
        final BukkitTask[] taskRef = new BukkitTask[1];
        
        taskRef[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Adjust batch size based on server performance periodically
            int batchCount = batchCounter.get();
            if (batchCount % PERFORMANCE_CHECK_INTERVAL == 0) {
                adjustBatchSettings(lastPerformanceCheckTime);
            }
            
            // Process a batch of blocks
            int count = 0;
            int maxCount = currentBatchSize;
            
            while (iterator.hasNext() && count < maxCount) {
                Map.Entry<Vector3, BlockData> blockEntry = iterator.next();
                Vector3 relPos = blockEntry.getKey();
                BlockData data = blockEntry.getValue();
                
                // Calculate the world position
                Vector3 worldPos = position.add(relPos);
                
                // Get the block
                Block block = worldPos.toBlock(world);
                
                // Save the previous state for undo
                // Reuse existing BlockState objects when possible to reduce garbage collection
                BlockState oldState = blockStateCache.computeIfAbsent(worldPos, pos -> block.getState());
                
                // Set the new block data
                block.setBlockData(data, false);  // false means don't apply physics for better performance
                
                // Save the new state for redo
                BlockState newState = block.getState();
                
                // Add to history
                entry.addBlockState(worldPos, oldState, newState);
                
                count++;
                blocksProcessed.incrementAndGet();
            }
            
            // Clear cache if it's getting too large to prevent memory issues
            if (blockStateCache.size() > 10000) {
                blockStateCache.clear();
            }
            
            // Update batch counter
            int currentBatch = batchCounter.incrementAndGet();
            
            // Report progress at intervals
            if (currentBatch % progressReportInterval == 0 || !iterator.hasNext()) {
                int percent = (int) ((double) blocksProcessed.get() / totalBlocks * 100);
                
                // Get current TPS for progress report
                double currentTps = 20.0;
                ServerPerformanceMonitor monitor = ServerPerformanceMonitor.getInstance();
                if (monitor != null) {
                    currentTps = monitor.getCurrentTps();
                }
                
                player.sendMessage(ChatColor.AQUA + "Batch paste progress: " + 
                                  percent + "% (" + blocksProcessed.get() + "/" + totalBlocks + 
                                  " blocks) - TPS: " + String.format("%.1f", currentTps));
            }
            
            // If all blocks are processed, cancel the task and report completion
            if (!iterator.hasNext()) {
                long duration = System.currentTimeMillis() - startTime;
                double seconds = duration / 1000.0;
                
                player.sendMessage(ChatColor.GREEN + "Batch paste completed: " + 
                                  blocksProcessed.get() + " blocks in " + String.format("%.2f", seconds) + " seconds");
                
                // Calculate and report the blocks per second
                double blocksPerSecond = seconds > 0 ? blocksProcessed.get() / seconds : blocksProcessed.get();
                player.sendMessage(ChatColor.GREEN + "Performance: " + String.format("%.1f", blocksPerSecond) + 
                                  " blocks/second");
                
                // Save the history entry
                plugin.getHistoryManager().addEntry(entry);
                
                // Clear the cache
                blockStateCache.clear();
                
                // Cancel this task
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
            }
        }, 0L, currentTickDelay);
    }
    
    /**
     * Adjust batch settings based on server performance.
     * 
     * @param lastPerformanceCheckTime Atomic reference to the last time performance was checked
     */
    private void adjustBatchSettings(AtomicLong lastPerformanceCheckTime) {
        ServerPerformanceMonitor monitor = ServerPerformanceMonitor.getInstance();
        if (monitor == null) {
            return;
        }
        
        // Only adjust if some time has passed since the last check
        long now = System.currentTimeMillis();
        if (now - lastPerformanceCheckTime.get() < 500) {
            return;
        }
        
        // Get new adaptive settings
        int newBatchSize = monitor.getAdaptiveBatchSize(baseBatchSize);
        int newTickDelay = monitor.getAdaptiveTickDelay(baseTickDelay);
        
        // Only log changes if they're significant
        if (Math.abs(newBatchSize - currentBatchSize) > baseBatchSize * 0.1 ||
            newTickDelay != currentTickDelay) {
            
            double tps = monitor.getCurrentTps();
            Logger.info("Adjusting batch settings - TPS: " + String.format("%.1f", tps) + 
                       ", Batch Size: " + currentBatchSize + " -> " + newBatchSize + 
                       ", Tick Delay: " + currentTickDelay + " -> " + newTickDelay);
        }
        
        // Update current settings
        currentBatchSize = newBatchSize;
        currentTickDelay = newTickDelay;
        
        // Update the last check time
        lastPerformanceCheckTime.set(now);
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        return operationName;
    }
    
    @Override
    public int getVolume() {
        if (ignoreAir) {
            // Count non-air blocks
            int count = 0;
            for (BlockData data : clipboard.getBlocks().values()) {
                if (!data.getMaterial().isAir()) {
                    count++;
                }
            }
            return count;
        } else {
            return clipboard.getVolume();
        }
    }
} 