package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Manages operations and their execution.
 */
public class OperationManager {
    private final FrizzlenEdit plugin;
    
    // Counter for tracking active operations
    private final AtomicInteger activeOperations = new AtomicInteger(0);
    
    /**
     * Create a new operation manager.
     * @param plugin The plugin instance
     */
    public OperationManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Execute an operation.
     * @param player The player
     * @param operation The operation to execute
     */
    public void execute(Player player, Operation operation) {
        // Check if the operation is too large
        int volume = operation.getVolume();
        int maxSize = plugin.getConfigManager().getMaxSelectionBlocks();
        
        if (volume > maxSize) {
            player.sendMessage(ChatColor.RED + "Operation too large: " + volume + " blocks. Maximum is " + maxSize + ".");
            return;
        }
        
        // Increment active operations counter
        activeOperations.incrementAndGet();
        
        // Show a progress message
        player.sendMessage(ChatColor.YELLOW + "Executing " + operation.getDescription() + " operation...");
        
        // Pre-process data async, but execute block changes on the main thread
        CompletableFuture.runAsync(() -> {
            try {
                // Run the actual block changes on the main server thread
                // This ensures we don't get "Asynchronous block modification" errors
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    HistoryEntry entry = null;
                    try {
                        entry = operation.execute();
                    } catch (Exception e) {
                        Logger.severe("Error executing operation: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Process the result
                    handleOperationResult(player, operation, entry);
                });
            } catch (Exception e) {
                Logger.severe("Error scheduling operation: " + e.getMessage());
                e.printStackTrace();
                activeOperations.decrementAndGet();
            }
        }, plugin.getAsyncExecutor());
    }
    
    /**
     * Handle the result of an operation.
     * @param player The player
     * @param operation The operation
     * @param entry The history entry, or null if the operation failed
     */
    private void handleOperationResult(Player player, Operation operation, HistoryEntry entry) {
        // Decrement active operations counter
        activeOperations.decrementAndGet();
        
        if (entry != null) {
            // Add the entry to the history
            plugin.getHistoryManager().addEntry(entry);
            
            // Show a success message
            player.sendMessage(ChatColor.GREEN + "Operation completed: " + operation.getDescription());
        } else {
            // Show a failure message
            player.sendMessage(ChatColor.RED + "Operation failed: " + operation.getDescription());
        }
    }
    
    /**
     * Create a set operation.
     * @param player The player
     * @param region The region
     * @param blockType The block type
     * @return The operation
     */
    public Operation createSetOperation(Player player, Region region, String blockType) {
        return new SetOperation(player, region, blockType);
    }
    
    /**
     * Create a replace operation.
     * @param player The player
     * @param region The region
     * @param fromType The block type to replace
     * @param toType The block type to replace with
     * @return The operation
     */
    public Operation createReplaceOperation(Player player, Region region, String fromType, String toType) {
        return new ReplaceOperation(player, region, fromType, toType);
    }
    
    /**
     * Create a smooth operation with full customization.
     * @param player The player
     * @param region The region
     * @param iterations Number of smoothing iterations
     * @param heightFactor Height factor for vertical smoothing
     * @param erodeSteepSlopes Whether to simulate erosion on steep slopes
     * @param preserveTopLayer Whether to preserve surface materials on top
     * @param naturalVariation Amount of natural variation (0.0-1.0) to add
     * @return The operation
     */
    public Operation createSmoothOperation(Player player, Region region, int iterations, double heightFactor, 
                                           boolean erodeSteepSlopes, boolean preserveTopLayer, double naturalVariation) {
        return new SmoothOperation(player, region, iterations, heightFactor, 
                                  erodeSteepSlopes, preserveTopLayer, naturalVariation);
    }
    
    /**
     * Create a smooth operation with custom iterations and height factor.
     * @param player The player
     * @param region The region
     * @param iterations Number of smoothing iterations
     * @param heightFactor Height factor for vertical smoothing
     * @return The operation
     */
    public Operation createSmoothOperation(Player player, Region region, int iterations, double heightFactor) {
        return new SmoothOperation(player, region, iterations, heightFactor);
    }
    
    /**
     * Create a smooth operation with default parameters.
     * @param player The player
     * @param region The region
     * @return The operation
     */
    public Operation createSmoothOperation(Player player, Region region) {
        return new SmoothOperation(player, region);
    }
    
    /**
     * Create a drain operation to remove water and other liquids.
     * @param player The player
     * @param region The region
     * @param radius The radius (if sphere is used)
     * @param removeAllLiquids Whether to remove all liquids (including lava) or just water
     * @return The operation
     */
    public Operation createDrainOperation(Player player, Region region, int radius, boolean removeAllLiquids) {
        return new DrainOperation(player, region, radius, removeAllLiquids);
    }
    
    /**
     * Create a drain operation with default parameters (water only).
     * @param player The player
     * @param region The region
     * @return The operation
     */
    public Operation createDrainOperation(Player player, Region region) {
        return new DrainOperation(player, region);
    }
    
    /**
     * Create a cylinder operation.
     * @param player The player
     * @param center The center position of the cylinder
     * @param material The material to use
     * @param radius The radius of the cylinder
     * @param height The height of the cylinder
     * @param hollow Whether the cylinder should be hollow
     * @return The operation
     */
    public Operation createCylinderOperation(Player player, Vector3 center, Material material, int radius, int height, boolean hollow) {
        return new CylinderOperation(player, center, material, radius, height, hollow);
    }
    
    /**
     * Create a remove near operation using a specific material.
     * @param player The player
     * @param radius The radius around the player to search
     * @param material The material to remove
     * @return The operation
     */
    public Operation createRemoveNearOperation(Player player, int radius, Material material) {
        return new RemoveNearOperation(player, radius, material);
    }
    
    /**
     * Create a remove near operation using the item in the player's hand.
     * @param player The player
     * @param radius The radius around the player to search
     * @return The operation
     */
    public Operation createRemoveNearOperation(Player player, int radius) {
        return new RemoveNearOperation(player, radius);
    }
    
    /**
     * Get the number of active operations.
     * @return The number of active operations
     */
    public int getActiveOperations() {
        return activeOperations.get();
    }
} 