package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.masks.Mask;

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
     * Create a sphere operation.
     * @param player The player
     * @param center The center position of the sphere
     * @param pattern The pattern to use
     * @param radius The radius of the sphere
     * @param hollow Whether the sphere should be hollow
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createSphereOperation(Player player, Vector3 center, Pattern pattern, int radius, boolean hollow, Mask mask) {
        return new SphereOperation(player, center, radius, pattern, hollow, mask);
    }
    
    /**
     * Create a sphere operation.
     * @param player The player
     * @param center The center position of the sphere
     * @param pattern The pattern to use
     * @param radius The radius of the sphere
     * @param hollow Whether the sphere should be hollow
     * @return The operation
     */
    public Operation createSphereOperation(Player player, Vector3 center, Pattern pattern, int radius, boolean hollow) {
        return new SphereOperation(player, center, radius, pattern, hollow);
    }
    
    /**
     * Create a sphere operation with a material.
     * @param player The player
     * @param center The center position of the sphere
     * @param material The material to use
     * @param radius The radius of the sphere
     * @param hollow Whether the sphere should be hollow
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createSphereOperation(Player player, Vector3 center, Material material, int radius, boolean hollow, Mask mask) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new SphereOperation(player, center, radius, pattern, hollow, mask);
    }
    
    /**
     * Create a sphere operation with a material.
     * @param player The player
     * @param center The center position of the sphere
     * @param material The material to use
     * @param radius The radius of the sphere
     * @param hollow Whether the sphere should be hollow
     * @return The operation
     */
    public Operation createSphereOperation(Player player, Vector3 center, Material material, int radius, boolean hollow) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new SphereOperation(player, center, radius, pattern, hollow);
    }
    
    /**
     * Create a pyramid operation.
     * @param player The player
     * @param base The base center point of the pyramid
     * @param pattern The pattern to use
     * @param size The size (width) of the pyramid
     * @param hollow Whether the pyramid should be hollow
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createPyramidOperation(Player player, Vector3 base, Pattern pattern, int size, boolean hollow, Mask mask) {
        return new PyramidOperation(player, base, size, pattern, hollow, mask);
    }
    
    /**
     * Create a pyramid operation.
     * @param player The player
     * @param base The base center point of the pyramid
     * @param pattern The pattern to use
     * @param size The size (width) of the pyramid
     * @param hollow Whether the pyramid should be hollow
     * @return The operation
     */
    public Operation createPyramidOperation(Player player, Vector3 base, Pattern pattern, int size, boolean hollow) {
        return new PyramidOperation(player, base, size, pattern, hollow);
    }
    
    /**
     * Create a pyramid operation with a material.
     * @param player The player
     * @param base The base center point of the pyramid
     * @param material The material to use
     * @param size The size (width) of the pyramid
     * @param hollow Whether the pyramid should be hollow
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createPyramidOperation(Player player, Vector3 base, Material material, int size, boolean hollow, Mask mask) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new PyramidOperation(player, base, size, pattern, hollow, mask);
    }
    
    /**
     * Create a pyramid operation with a material.
     * @param player The player
     * @param base The base center point of the pyramid
     * @param material The material to use
     * @param size The size (width) of the pyramid
     * @param hollow Whether the pyramid should be hollow
     * @return The operation
     */
    public Operation createPyramidOperation(Player player, Vector3 base, Material material, int size, boolean hollow) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new PyramidOperation(player, base, size, pattern, hollow);
    }
    
    /**
     * Create a fill operation.
     * @param player The player executing the operation
     * @param region The region to fill
     * @param pattern The pattern to use for filling
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createFillOperation(Player player, Region region, Pattern pattern, Mask mask) {
        return new FillOperation(player, region, pattern, mask);
    }
    
    /**
     * Create a fill operation.
     * @param player The player executing the operation
     * @param region The region to fill
     * @param pattern The pattern to use for filling
     * @return The operation
     */
    public Operation createFillOperation(Player player, Region region, Pattern pattern) {
        return new FillOperation(player, region, pattern);
    }
    
    /**
     * Create a fill operation with a material.
     * @param player The player executing the operation
     * @param region The region to fill
     * @param material The material to use for filling
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createFillOperation(Player player, Region region, Material material, Mask mask) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new FillOperation(player, region, pattern, mask);
    }
    
    /**
     * Create a fill operation with a material.
     * @param player The player executing the operation
     * @param region The region to fill
     * @param material The material to use for filling
     * @return The operation
     */
    public Operation createFillOperation(Player player, Region region, Material material) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new FillOperation(player, region, pattern);
    }
    
    /**
     * Create a walls operation.
     * @param player The player executing the operation
     * @param region The region around which to create walls
     * @param pattern The pattern to use for the walls
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createWallsOperation(Player player, Region region, Pattern pattern, Mask mask) {
        return new WallsOperation(player, region, pattern, mask);
    }
    
    /**
     * Create a walls operation.
     * @param player The player executing the operation
     * @param region The region around which to create walls
     * @param pattern The pattern to use for the walls
     * @return The operation
     */
    public Operation createWallsOperation(Player player, Region region, Pattern pattern) {
        return new WallsOperation(player, region, pattern);
    }
    
    /**
     * Create a walls operation with a material.
     * @param player The player executing the operation
     * @param region The region around which to create walls
     * @param material The material to use for the walls
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createWallsOperation(Player player, Region region, Material material, Mask mask) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new WallsOperation(player, region, pattern, mask);
    }
    
    /**
     * Create a walls operation with a material.
     * @param player The player executing the operation
     * @param region The region around which to create walls
     * @param material The material to use for the walls
     * @return The operation
     */
    public Operation createWallsOperation(Player player, Region region, Material material) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new WallsOperation(player, region, pattern);
    }
    
    /**
     * Create an outline operation.
     * @param player The player executing the operation
     * @param region The region to outline
     * @param pattern The pattern to use for the outline
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createOutlineOperation(Player player, Region region, Pattern pattern, Mask mask) {
        return new OutlineOperation(player, region, pattern, mask);
    }
    
    /**
     * Create an outline operation.
     * @param player The player executing the operation
     * @param region The region to outline
     * @param pattern The pattern to use for the outline
     * @return The operation
     */
    public Operation createOutlineOperation(Player player, Region region, Pattern pattern) {
        return new OutlineOperation(player, region, pattern);
    }
    
    /**
     * Create an outline operation with a material.
     * @param player The player executing the operation
     * @param region The region to outline
     * @param material The material to use for the outline
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createOutlineOperation(Player player, Region region, Material material, Mask mask) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new OutlineOperation(player, region, pattern, mask);
    }
    
    /**
     * Create an outline operation with a material.
     * @param player The player executing the operation
     * @param region The region to outline
     * @param material The material to use for the outline
     * @return The operation
     */
    public Operation createOutlineOperation(Player player, Region region, Material material) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new OutlineOperation(player, region, pattern);
    }
    
    /**
     * Create a hollow operation with optional pattern for the shell.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellPattern The pattern to use for the shell (null to keep original blocks)
     * @param thickness The thickness of the shell in blocks
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createHollowOperation(Player player, Region region, Pattern shellPattern, int thickness, Mask mask) {
        return new HollowOperation(player, region, shellPattern, thickness, mask);
    }
    
    /**
     * Create a hollow operation with optional pattern for the shell.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellPattern The pattern to use for the shell (null to keep original blocks)
     * @param thickness The thickness of the shell in blocks
     * @return The operation
     */
    public Operation createHollowOperation(Player player, Region region, Pattern shellPattern, int thickness) {
        return new HollowOperation(player, region, shellPattern, thickness);
    }
    
    /**
     * Create a hollow operation with a material for the shell.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellMaterial The material to use for the shell
     * @param thickness The thickness of the shell in blocks
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createHollowOperation(Player player, Region region, Material shellMaterial, int thickness, Mask mask) {
        Pattern shellPattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(shellMaterial.name().toLowerCase());
        return new HollowOperation(player, region, shellPattern, thickness, mask);
    }
    
    /**
     * Create a hollow operation with a material for the shell.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellMaterial The material to use for the shell
     * @param thickness The thickness of the shell in blocks
     * @return The operation
     */
    public Operation createHollowOperation(Player player, Region region, Material shellMaterial, int thickness) {
        Pattern shellPattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(shellMaterial.name().toLowerCase());
        return new HollowOperation(player, region, shellPattern, thickness);
    }
    
    /**
     * Create a hollow operation that keeps the original shell blocks.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param thickness The thickness of the shell in blocks
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createHollowOperation(Player player, Region region, int thickness, Mask mask) {
        return new HollowOperation(player, region, null, thickness, mask);
    }
    
    /**
     * Create a hollow operation that keeps the original shell blocks.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param thickness The thickness of the shell in blocks
     * @return The operation
     */
    public Operation createHollowOperation(Player player, Region region, int thickness) {
        return new HollowOperation(player, region, null, thickness);
    }
    
    /**
     * Create a naturalize operation.
     * @param player The player executing the operation
     * @param region The region to naturalize
     * @param preserveWater Whether to preserve water bodies
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createNaturalizeOperation(Player player, Region region, boolean preserveWater, Mask mask) {
        return new NaturalizeOperation(player, region, preserveWater, mask);
    }
    
    /**
     * Create a naturalize operation.
     * @param player The player executing the operation
     * @param region The region to naturalize
     * @param preserveWater Whether to preserve water bodies
     * @return The operation
     */
    public Operation createNaturalizeOperation(Player player, Region region, boolean preserveWater) {
        return new NaturalizeOperation(player, region, preserveWater);
    }
    
    /**
     * Create an overlay operation.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param pattern The pattern to use for the overlay
     * @param thickness The thickness of the overlay in blocks
     * @param ignoreWater Whether to ignore water when adding the overlay
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createOverlayOperation(Player player, Region region, Pattern pattern, int thickness, boolean ignoreWater, Mask mask) {
        return new OverlayOperation(player, region, pattern, thickness, ignoreWater, mask);
    }
    
    /**
     * Create an overlay operation.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param pattern The pattern to use for the overlay
     * @param thickness The thickness of the overlay in blocks
     * @param ignoreWater Whether to ignore water when adding the overlay
     * @return The operation
     */
    public Operation createOverlayOperation(Player player, Region region, Pattern pattern, int thickness, boolean ignoreWater) {
        return new OverlayOperation(player, region, pattern, thickness, ignoreWater);
    }
    
    /**
     * Create an overlay operation with a material.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param material The material to use for the overlay
     * @param thickness The thickness of the overlay in blocks
     * @param ignoreWater Whether to ignore water when adding the overlay
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createOverlayOperation(Player player, Region region, Material material, int thickness, boolean ignoreWater, Mask mask) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new OverlayOperation(player, region, pattern, thickness, ignoreWater, mask);
    }
    
    /**
     * Create an overlay operation with a material.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param material The material to use for the overlay
     * @param thickness The thickness of the overlay in blocks
     * @param ignoreWater Whether to ignore water when adding the overlay
     * @return The operation
     */
    public Operation createOverlayOperation(Player player, Region region, Material material, int thickness, boolean ignoreWater) {
        Pattern pattern = new org.frizzlenpop.frizzlenEdit.patterns.SingleBlockPattern(material.name().toLowerCase());
        return new OverlayOperation(player, region, pattern, thickness, ignoreWater);
    }
    
    /**
     * Create a caves operation with custom settings.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     * @param threshold The noise threshold for cave generation (0.0-1.0, higher = more open space)
     * @param scale The scale of the noise (smaller = larger caves)
     * @param addOres Whether to add ores to the cave walls
     * @param oreFrequency The frequency of ore generation (0.0-1.0)
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createCavesOperation(Player player, Region region, double threshold, double scale, 
                                         boolean addOres, double oreFrequency, Mask mask) {
        return new CavesOperation(player, region, threshold, scale, addOres, oreFrequency, mask);
    }
    
    /**
     * Create a caves operation with custom settings.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     * @param threshold The noise threshold for cave generation (0.0-1.0, higher = more open space)
     * @param scale The scale of the noise (smaller = larger caves)
     * @param addOres Whether to add ores to the cave walls
     * @param oreFrequency The frequency of ore generation (0.0-1.0)
     * @return The operation
     */
    public Operation createCavesOperation(Player player, Region region, double threshold, double scale, 
                                         boolean addOres, double oreFrequency) {
        return new CavesOperation(player, region, threshold, scale, addOres, oreFrequency);
    }
    
    /**
     * Create a caves operation with default settings.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     * @param mask Optional mask to apply
     * @return The operation
     */
    public Operation createCavesOperation(Player player, Region region, Mask mask) {
        return new CavesOperation(player, region, 0.4, 0.03, true, 0.1, mask);
    }
    
    /**
     * Create a caves operation with default settings.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     * @return The operation
     */
    public Operation createCavesOperation(Player player, Region region) {
        return new CavesOperation(player, region);
    }
    
    /**
     * Create a chunk regeneration operation.
     * @param player The player executing the operation
     * @param region The region containing chunks to regenerate
     * @param keepEntities Whether to preserve entities in the regenerated chunks
     * @param keepStructures Whether to preserve structures in the regenerated chunks
     * @return The operation
     */
    public Operation createChunkRegenerationOperation(Player player, Region region, boolean keepEntities, boolean keepStructures) {
        return new ChunkRegenerationOperation(player, region, keepEntities, keepStructures, plugin);
    }
    
    /**
     * Get the number of active operations.
     * @return The number of active operations
     */
    public int getActiveOperations() {
        return activeOperations.get();
    }
} 