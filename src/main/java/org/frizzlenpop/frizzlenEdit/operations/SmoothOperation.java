package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An operation that smooths a terrain within a region.
 */
public class SmoothOperation implements Operation {
    private final Player player;
    private final Region region;
    private final int iterations;
    private final double heightFactor;
    
    // Materials that should be considered as air or non-terrain (fluids, plants, etc.)
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
     * Create a new smooth operation.
     * @param player The player
     * @param region The region to smooth
     * @param iterations Number of smoothing iterations (default: 4)
     * @param heightFactor Height weighting factor (default: 2.0)
     */
    public SmoothOperation(Player player, Region region, int iterations, double heightFactor) {
        this.player = player;
        this.region = region;
        this.iterations = iterations;
        this.heightFactor = heightFactor;
    }
    
    /**
     * Create a new smooth operation with default parameters.
     * @param player The player
     * @param region The region to smooth
     */
    public SmoothOperation(Player player, Region region) {
        this(player, region, 4, 2.0);
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = new HistoryEntry(player, world, getDescription());
        
        // First pass: collect all blocks in the region
        Map<Vector3, BlockData> originalBlocks = new HashMap<>();
        Map<Vector3, BlockData> currentBlocks = new HashMap<>();
        
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // Collect all blocks in the region plus a 2-block border for neighborhood calculations
        for (int x = min.getX() - 2; x <= max.getX() + 2; x++) {
            for (int y = min.getY() - 2; y <= max.getY() + 2; y++) {
                for (int z = min.getZ() - 2; z <= max.getZ() + 2; z++) {
                    Vector3 pos = new Vector3(x, y, z);
                    Block block = pos.toBlock(world);
                    BlockData blockData = block.getBlockData();
                    
                    // Store all blocks including those outside the region (for neighborhood calculations)
                    currentBlocks.put(pos, blockData);
                    
                    // Only track original blocks within the actual region (for history)
                    if (region.contains(pos)) {
                        originalBlocks.put(pos, blockData);
                    }
                }
            }
        }
        
        // Execute multiple smoothing iterations
        for (int iter = 0; iter < iterations; iter++) {
            Map<Vector3, BlockData> newBlocks = new HashMap<>();
            
            // Process each block in the extended region
            for (Map.Entry<Vector3, BlockData> blockEntry : currentBlocks.entrySet()) {
                Vector3 pos = blockEntry.getKey();
                
                // Only modify blocks within the actual region
                if (!region.contains(pos)) {
                    newBlocks.put(pos, blockEntry.getValue());
                    continue;
                }
                
                // Skip air-like blocks
                if (isAirLike(blockEntry.getValue().getMaterial())) {
                    newBlocks.put(pos, blockEntry.getValue());
                    continue;
                }
                
                // Get the weighted average of surrounding blocks
                BlockData newData = getWeightedCommonBlockData(pos, currentBlocks);
                
                if (newData != null) {
                    newBlocks.put(pos, newData);
                } else {
                    newBlocks.put(pos, blockEntry.getValue());
                }
            }
            
            // Update current blocks for next iteration
            currentBlocks = newBlocks;
        }
        
        // Apply the changes and record history
        for (Vector3 pos : originalBlocks.keySet()) {
            Block block = pos.toBlock(world);
            BlockData originalData = originalBlocks.get(pos);
            BlockData newData = currentBlocks.get(pos);
            
            // Skip if the block type is the same
            if (newData == null || newData.getMaterial() == originalData.getMaterial()) {
                continue;
            }
            
            // Save the previous state for undo
            entry.addBlockState(pos, block.getState(), null);
            
            // Set the block
            block.setBlockData(newData);
            
            // Save the new state for redo
            entry.addBlockState(pos, null, block.getState());
        }
        
        return entry;
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
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        return "Smooth operation with " + iterations + " iterations";
    }
    
    @Override
    public int getVolume() {
        return region.getVolume();
    }
} 