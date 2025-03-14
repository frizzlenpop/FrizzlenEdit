package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.NoiseGenerator;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Operation that makes terrain look more natural by applying natural block layers.
 */
public class NaturalizeOperation extends AbstractOperation {
    private final Region region;
    private final Random random;
    private final NoiseGenerator noiseGen;
    private final boolean preserveWater;
    private final Map<Material, Integer> topSoilDepth;
    
    /**
     * Creates a new naturalize operation.
     * @param player The player executing the operation
     * @param region The region to naturalize
     * @param preserveWater Whether to preserve water bodies
     */
    public NaturalizeOperation(Player player, Region region, boolean preserveWater) {
        super(player);
        this.region = region;
        this.preserveWater = preserveWater;
        this.random = new Random();
        this.noiseGen = new NoiseGenerator(random.nextLong());
        this.topSoilDepth = initializeTopSoilDepths();
    }
    
    /**
     * Creates a new naturalize operation with a mask.
     * @param player The player executing the operation
     * @param region The region to naturalize
     * @param preserveWater Whether to preserve water bodies
     * @param mask The mask to apply
     */
    public NaturalizeOperation(Player player, Region region, boolean preserveWater, Mask mask) {
        super(player, mask);
        this.region = region;
        this.preserveWater = preserveWater;
        this.random = new Random();
        this.noiseGen = new NoiseGenerator(random.nextLong());
        this.topSoilDepth = initializeTopSoilDepths();
    }
    
    /**
     * Initialize the map of materials to their typical topsoil depths.
     * @return A map of materials to their topsoil depths
     */
    private Map<Material, Integer> initializeTopSoilDepths() {
        Map<Material, Integer> depths = new HashMap<>();
        
        // Basic overworld blocks
        depths.put(Material.GRASS_BLOCK, 1);
        depths.put(Material.DIRT, 3);
        depths.put(Material.STONE, 12);
        
        // Desert
        depths.put(Material.SAND, 3);
        depths.put(Material.SANDSTONE, 8);
        
        // Nether
        depths.put(Material.NETHERRACK, 15);
        depths.put(Material.SOUL_SAND, 2);
        depths.put(Material.SOUL_SOIL, 3);
        
        // End
        depths.put(Material.END_STONE, 8);
        
        // Snow biomes
        depths.put(Material.SNOW, 1);
        depths.put(Material.SNOW_BLOCK, 2);
        
        // Beach
        depths.put(Material.GRAVEL, 3);
        
        return depths;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Get region bounds
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // First pass: Identify surface blocks for each x,z column
        Map<Vector3, Material> topBlocks = new HashMap<>();
        Map<Vector3, Integer> columnHeights = new HashMap<>();
        
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                // Find the highest solid block
                boolean foundTop = false;
                for (int y = max.getY(); y >= min.getY() && !foundTop; y--) {
                    Block block = world.getBlockAt(x, y, z);
                    
                    if (!matchesMask(block)) {
                        continue;
                    }
                    
                    Material mat = block.getType();
                    
                    // Skip air and water if preserving water
                    if (mat == Material.AIR || 
                        (preserveWater && (mat == Material.WATER || mat == Material.LAVA))) {
                        continue;
                    }
                    
                    // Found a solid block
                    Vector3 columnKey = new Vector3(x, 0, z); // Key for the column
                    topBlocks.put(columnKey, mat);
                    columnHeights.put(columnKey, y);
                    foundTop = true;
                }
            }
        }
        
        // Second pass: Apply naturalization to each column
        for (Map.Entry<Vector3, Material> entry2 : topBlocks.entrySet()) {
            Vector3 columnKey = entry2.getKey();
            Material topMaterial = entry2.getValue();
            int topY = columnHeights.get(columnKey);
            
            int x = columnKey.getX();
            int z = columnKey.getZ();
            
            // Determine the top soil material and stone material based on the biome
            Material topSoilMaterial = determineTopSoilMaterial(topMaterial);
            Material subSoilMaterial = determineSubSoilMaterial(topMaterial);
            Material stoneMaterial = determineBaseMaterial(topMaterial);
            
            // Get typical depths, adjusted by noise
            int topSoilThickness = Math.max(1, calculateLayerThickness(topSoilMaterial, x, z, 0.2));
            int subSoilThickness = Math.max(1, calculateLayerThickness(subSoilMaterial, x, z, 0.3));
            
            // Process the column from top to bottom
            for (int y = topY; y >= min.getY(); y--) {
                Block block = world.getBlockAt(x, y, z);
                
                if (!matchesMask(block)) {
                    continue;
                }
                
                BlockState oldState = block.getState();
                Material newMaterial;
                
                int depth = topY - y;
                
                if (depth == 0) {
                    // Top layer
                    newMaterial = topSoilMaterial;
                } else if (depth <= topSoilThickness) {
                    // Top soil layer
                    newMaterial = topSoilMaterial;
                } else if (depth <= topSoilThickness + subSoilThickness) {
                    // Sub soil layer
                    newMaterial = subSoilMaterial;
                } else {
                    // Stone layer
                    newMaterial = stoneMaterial;
                }
                
                // Skip if the material is already correct
                if (block.getType() == newMaterial) {
                    continue;
                }
                
                // Get block data for the new material
                BlockData newData = newMaterial.createBlockData();
                
                Vector3 pos = new Vector3(x, y, z);
                
                // Save the old state for undo
                entry.addBlockState(pos, oldState, null);
                
                // Set the new block data
                block.setBlockData(newData);
                
                // Save the new state for redo
                entry.addBlockState(pos, null, block.getState());
                
                affected++;
            }
        }
        
        sendMessage("Naturalized terrain with " + affected + " blocks affected.");
        return entry;
    }
    
    /**
     * Calculate the thickness of a layer based on noise.
     * @param material The material
     * @param x The x coordinate
     * @param z The z coordinate
     * @param variationFactor How much variation to apply (0.0-1.0)
     * @return The adjusted thickness
     */
    private int calculateLayerThickness(Material material, int x, int z, double variationFactor) {
        Integer baseDepth = topSoilDepth.getOrDefault(material, 3);
        
        // Generate noise value for this position
        double noise = noiseGen.noise(x * 0.05, z * 0.05, 0.5);
        
        // Scale noise to variation range
        double variation = noise * baseDepth * variationFactor;
        
        return Math.max(1, (int) Math.round(baseDepth + variation));
    }
    
    /**
     * Determine the top soil material based on the existing top material.
     * @param existingMaterial The existing material
     * @return The appropriate top soil material
     */
    private Material determineTopSoilMaterial(Material existingMaterial) {
        // Desert
        if (existingMaterial == Material.SAND || existingMaterial == Material.SANDSTONE) {
            return Material.SAND;
        }
        
        // Nether
        if (existingMaterial == Material.NETHERRACK || existingMaterial == Material.SOUL_SAND) {
            return existingMaterial; // Keep the nether material
        }
        
        // End
        if (existingMaterial == Material.END_STONE) {
            return Material.END_STONE;
        }
        
        // Snow biome
        if (existingMaterial == Material.SNOW || existingMaterial == Material.SNOW_BLOCK) {
            return Material.SNOW_BLOCK; // Use snow blocks for solidity
        }
        
        // Beach
        if (existingMaterial == Material.GRAVEL) {
            return Material.GRAVEL;
        }
        
        // Default to grass
        return Material.GRASS_BLOCK;
    }
    
    /**
     * Determine the sub soil material based on the existing top material.
     * @param existingMaterial The existing material
     * @return The appropriate sub soil material
     */
    private Material determineSubSoilMaterial(Material existingMaterial) {
        // Desert
        if (existingMaterial == Material.SAND || existingMaterial == Material.SANDSTONE) {
            return Material.SANDSTONE;
        }
        
        // Nether
        if (existingMaterial == Material.NETHERRACK) {
            return Material.NETHERRACK;
        }
        
        if (existingMaterial == Material.SOUL_SAND) {
            return Material.SOUL_SOIL;
        }
        
        // End
        if (existingMaterial == Material.END_STONE) {
            return Material.END_STONE;
        }
        
        // Default to dirt
        return Material.DIRT;
    }
    
    /**
     * Determine the base material (stone) based on the existing top material.
     * @param existingMaterial The existing material
     * @return The appropriate base material
     */
    private Material determineBaseMaterial(Material existingMaterial) {
        // Desert
        if (existingMaterial == Material.SAND || existingMaterial == Material.SANDSTONE) {
            return Material.SANDSTONE;
        }
        
        // Nether
        if (existingMaterial == Material.NETHERRACK || existingMaterial == Material.SOUL_SAND) {
            return Material.NETHERRACK;
        }
        
        // End
        if (existingMaterial == Material.END_STONE) {
            return Material.END_STONE;
        }
        
        // Default to stone
        return Material.STONE;
    }
    
    @Override
    public String getDescription() {
        String desc = "Naturalize (region=" + region.getVolume() + " blocks";
        if (preserveWater) {
            desc += ", preserving water";
        }
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        return region.getVolume();
    }
} 