package org.frizzlenpop.frizzlenEdit.brushes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * A brush that smooths terrain.
 */
public class SmoothBrush implements Brush {
    private final FrizzlenEdit plugin;
    private final int radius;
    private final int iterations;
    private final double heightFactor;
    
    // Materials that should be considered as air (fluids, plants, etc.)
    private static final Set<Material> AIR_MATERIALS = new HashSet<>();
    
    static {
        // Add materials that should be treated as air for smoothing
        AIR_MATERIALS.add(Material.AIR);
        AIR_MATERIALS.add(Material.CAVE_AIR);
        AIR_MATERIALS.add(Material.VOID_AIR);
        AIR_MATERIALS.add(Material.WATER);
        AIR_MATERIALS.add(Material.LAVA);
        AIR_MATERIALS.add(Material.GRASS_BLOCK);
        AIR_MATERIALS.add(Material.TALL_GRASS);
        AIR_MATERIALS.add(Material.FERN);
        AIR_MATERIALS.add(Material.LARGE_FERN);
        AIR_MATERIALS.add(Material.SEAGRASS);
        AIR_MATERIALS.add(Material.TALL_SEAGRASS);
        AIR_MATERIALS.add(Material.KELP);
        AIR_MATERIALS.add(Material.KELP_PLANT);
    }
    
    /**
     * Create a new smooth brush.
     * @param plugin The plugin instance
     * @param radius The radius
     */
    public SmoothBrush(FrizzlenEdit plugin, int radius) {
        this(plugin, radius, 4, 2.0);
    }
    
    /**
     * Create a new smooth brush with specified iterations and height factor.
     * @param plugin The plugin instance
     * @param radius The radius
     * @param iterations Number of smoothing iterations
     * @param heightFactor Higher values smooth the terrain more aggressively
     */
    public SmoothBrush(FrizzlenEdit plugin, int radius, int iterations, double heightFactor) {
        this.plugin = plugin;
        this.radius = radius;
        this.iterations = iterations;
        this.heightFactor = heightFactor;
    }
    
    @Override
    public void use(Player player, Vector3 position, String mask) {
        World world = player.getWorld();
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Smooth brush");
        
        // Calculate the bounds of the sphere
        int radiusSquared = radius * radius;
        
        // Collect original block data
        Map<Vector3, BlockData> originalBlocks = new HashMap<>();
        Map<Vector3, BlockData> currentBlocks = new HashMap<>();
        
        // Iterate through all blocks in the cube around the position
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Calculate the distance squared from the center
                    int distanceSquared = x*x + y*y + z*z;
                    
                    // Skip blocks outside the sphere
                    if (distanceSquared > radiusSquared) {
                        continue;
                    }
                    
                    // Calculate the position of this block
                    Vector3 blockPos = position.add(new Vector3(x, y, z));
                    
                    // Get the block
                    Block block = blockPos.toBlock(world);
                    
                    // Check the mask
                    if (mask != null) {
                        Material maskMaterial = Material.matchMaterial(mask);
                        if (maskMaterial != null && block.getType() != maskMaterial) {
                            continue;
                        }
                    }
                    
                    // Store the original block data
                    BlockData blockData = block.getBlockData();
                    originalBlocks.put(blockPos, blockData);
                    currentBlocks.put(blockPos, blockData);
                }
            }
        }
        
        // Execute multiple smoothing iterations
        for (int iter = 0; iter < iterations; iter++) {
            Map<Vector3, BlockData> newBlocks = new HashMap<>();
            
            // Process each block
            for (Map.Entry<Vector3, BlockData> entry2 : currentBlocks.entrySet()) {
                Vector3 blockPos = entry2.getKey();
                
                // Treat air-like blocks specially for terrain smoothing
                if (isAirLike(entry2.getValue().getMaterial())) {
                    newBlocks.put(blockPos, entry2.getValue());
                    continue;
                }
                
                // Get the weighted average of surrounding blocks
                BlockData newData = getWeightedCommonBlockData(blockPos, currentBlocks);
                
                if (newData != null) {
                    newBlocks.put(blockPos, newData);
                } else {
                    newBlocks.put(blockPos, entry2.getValue());
                }
            }
            
            // Update current blocks for next iteration
            currentBlocks = newBlocks;
        }
        
        // Apply the changes
        for (Map.Entry<Vector3, BlockData> entry2 : currentBlocks.entrySet()) {
            Vector3 blockPos = entry2.getKey();
            Block block = blockPos.toBlock(world);
            BlockData originalData = originalBlocks.get(blockPos);
            BlockData newData = entry2.getValue();
            
            // Skip if the block type is the same
            if (newData == null || newData.getMaterial() == originalData.getMaterial()) {
                continue;
            }
            
            // Save the previous state for undo
            entry.addBlockState(blockPos, block.getState(), null);
            
            // Set the block
            block.setBlockData(newData);
            
            // Save the new state for redo
            entry.addBlockState(blockPos, null, block.getState());
        }
        
        // Add the entry to the history
        plugin.getHistoryManager().addEntry(entry);
        
        player.sendMessage(ChatColor.GREEN + "Smooth brush used at " + position + " with radius " + radius + " and " + iterations + " iterations.");
    }
    
    /**
     * Check if a material should be treated as air for smoothing purposes.
     */
    private boolean isAirLike(Material material) {
        return material.isAir() || AIR_MATERIALS.contains(material);
    }
    
    /**
     * Get a weighted average of block data in the neighborhood of a position.
     * This method prioritizes terrain smoothing by applying height factors.
     * 
     * @param position The position
     * @param blocks The map of positions to block data
     * @return The most appropriate block data for smoothing
     */
    private BlockData getWeightedCommonBlockData(Vector3 position, Map<Vector3, BlockData> blocks) {
        Map<Material, Double> weights = new HashMap<>();
        double totalWeight = 0;
        
        // Calculate the weighted score for each material
        // The neighborhood extends further horizontally than vertically due to the height factor
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    // Skip the center block
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    
                    // Calculate distance, applying the height factor to the y-coordinate
                    double distance = Math.sqrt(x*x + (y*y*heightFactor) + z*z);
                    
                    // Skip blocks that are too far away
                    if (distance > 3.0) {
                        continue;
                    }
                    
                    // Calculate weight based on distance (closer blocks have higher weight)
                    double weight = 1.0 / (distance + 0.1);
                    
                    Vector3 neighborPos = position.add(new Vector3(x, y, z));
                    BlockData data = blocks.get(neighborPos);
                    
                    if (data != null) {
                        Material material = data.getMaterial();
                        
                        // Skip air-like materials for smoothing calculations
                        if (isAirLike(material)) {
                            continue;
                        }
                        
                        weights.put(material, weights.getOrDefault(material, 0.0) + weight);
                        totalWeight += weight;
                    }
                }
            }
        }
        
        // Find the material with the highest weight
        Material bestMaterial = null;
        double highestWeight = 0;
        
        for (Map.Entry<Material, Double> entry : weights.entrySet()) {
            Material material = entry.getKey();
            double weight = entry.getValue();
            
            if (weight > highestWeight) {
                highestWeight = weight;
                bestMaterial = material;
            }
        }
        
        // If no suitable material was found or insufficient data
        if (bestMaterial == null || totalWeight < 1.0) {
            return null;
        }
        
        // Find an existing block with this material to get the proper block data
        final Material finalBestMaterial = bestMaterial;
        return blocks.values().stream()
            .filter(data -> data.getMaterial() == finalBestMaterial)
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public int getRadius() {
        return radius;
    }
    
    @Override
    public String getDescription() {
        return "Smooth brush, radius " + radius + ", iterations " + iterations;
    }
} 