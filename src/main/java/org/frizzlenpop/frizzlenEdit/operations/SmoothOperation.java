package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.*;

/**
 * An operation that smooths a terrain within a region with enhanced realism.
 */
public class SmoothOperation implements Operation {
    private final Player player;
    private final Region region;
    private final int iterations;
    private final double heightFactor;
    private final boolean erodeSteepSlopes;
    private final boolean preserveTopLayer;
    private final double naturalVariation;
    
    // Materials that should be considered as air or non-terrain (fluids, plants, etc.)
    private static final Set<Material> AIR_MATERIALS = new HashSet<>();
    
    // Materials typically found on terrain surface
    private static final Set<Material> SURFACE_MATERIALS = new HashSet<>();
    
    // Groups of materials that naturally appear together in geological formations
    private static final Map<Material, List<Material>> GEOLOGICAL_GROUPS = new HashMap<>();
    
    // Material density/hardness for erosion simulation (higher = more resistant to erosion)
    private static final Map<Material, Integer> MATERIAL_HARDNESS = new HashMap<>();
    
    static {
        // Add materials that should be treated as air for smoothing
        AIR_MATERIALS.add(Material.AIR);
        AIR_MATERIALS.add(Material.CAVE_AIR);
        AIR_MATERIALS.add(Material.VOID_AIR);
        AIR_MATERIALS.add(Material.WATER);
        AIR_MATERIALS.add(Material.LAVA);
        AIR_MATERIALS.add(Material.SHORT_GRASS);
        AIR_MATERIALS.add(Material.TALL_GRASS);
        AIR_MATERIALS.add(Material.FERN);
        AIR_MATERIALS.add(Material.LARGE_FERN);
        AIR_MATERIALS.add(Material.SEAGRASS);
        AIR_MATERIALS.add(Material.TALL_SEAGRASS);
        AIR_MATERIALS.add(Material.KELP);
        AIR_MATERIALS.add(Material.KELP_PLANT);
        AIR_MATERIALS.add(Material.VINE);
        AIR_MATERIALS.add(Material.GLOW_LICHEN);
        AIR_MATERIALS.add(Material.HANGING_ROOTS);
        AIR_MATERIALS.add(Material.SNOW);
        
        // Surface materials - typically form the top layer of terrain
        SURFACE_MATERIALS.add(Material.GRASS_BLOCK);
        SURFACE_MATERIALS.add(Material.DIRT);
        SURFACE_MATERIALS.add(Material.COARSE_DIRT);
        SURFACE_MATERIALS.add(Material.PODZOL);
        SURFACE_MATERIALS.add(Material.MYCELIUM);
        SURFACE_MATERIALS.add(Material.SAND);
        SURFACE_MATERIALS.add(Material.RED_SAND);
        SURFACE_MATERIALS.add(Material.GRAVEL);
        SURFACE_MATERIALS.add(Material.MOSS_BLOCK);
        SURFACE_MATERIALS.add(Material.SNOW_BLOCK);
        
        // Define geological groups - materials that naturally appear together
        List<Material> stoneGroup = Arrays.asList(
            Material.STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE,
            Material.COBBLESTONE, Material.MOSSY_COBBLESTONE
        );
        
        List<Material> sandGroup = Arrays.asList(
            Material.SAND, Material.SANDSTONE, Material.RED_SAND, Material.RED_SANDSTONE,
            Material.SMOOTH_SANDSTONE
        );
        
        List<Material> dirtGroup = Arrays.asList(
            Material.DIRT, Material.COARSE_DIRT, Material.GRASS_BLOCK, Material.ROOTED_DIRT,
            Material.FARMLAND, Material.DIRT_PATH
        );
        
        List<Material> deepslateGroup = Arrays.asList(
            Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.POLISHED_DEEPSLATE,
            Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES
        );
        
        // Add all groups to the geological map
        for (Material m : stoneGroup) {
            GEOLOGICAL_GROUPS.put(m, stoneGroup);
        }
        
        for (Material m : sandGroup) {
            GEOLOGICAL_GROUPS.put(m, sandGroup);
        }
        
        for (Material m : dirtGroup) {
            GEOLOGICAL_GROUPS.put(m, dirtGroup);
        }
        
        for (Material m : deepslateGroup) {
            GEOLOGICAL_GROUPS.put(m, deepslateGroup);
        }
        
        // Define material hardness for erosion simulation
        MATERIAL_HARDNESS.put(Material.BEDROCK, 100);
        MATERIAL_HARDNESS.put(Material.OBSIDIAN, 90);
        MATERIAL_HARDNESS.put(Material.ANCIENT_DEBRIS, 85);
        MATERIAL_HARDNESS.put(Material.CRYING_OBSIDIAN, 80);
        MATERIAL_HARDNESS.put(Material.NETHERITE_BLOCK, 75);
        MATERIAL_HARDNESS.put(Material.DEEPSLATE, 70);
        MATERIAL_HARDNESS.put(Material.REINFORCED_DEEPSLATE, 70);
        MATERIAL_HARDNESS.put(Material.END_STONE, 65);
        MATERIAL_HARDNESS.put(Material.STONE, 60);
        MATERIAL_HARDNESS.put(Material.GRANITE, 58);
        MATERIAL_HARDNESS.put(Material.DIORITE, 57);
        MATERIAL_HARDNESS.put(Material.ANDESITE, 56);
        MATERIAL_HARDNESS.put(Material.COBBLESTONE, 55);
        MATERIAL_HARDNESS.put(Material.BLACKSTONE, 54);
        MATERIAL_HARDNESS.put(Material.BASALT, 52);
        MATERIAL_HARDNESS.put(Material.SANDSTONE, 45);
        MATERIAL_HARDNESS.put(Material.RED_SANDSTONE, 45);
        MATERIAL_HARDNESS.put(Material.TERRACOTTA, 42);
        MATERIAL_HARDNESS.put(Material.PACKED_ICE, 40);
        MATERIAL_HARDNESS.put(Material.PACKED_MUD, 35);
        MATERIAL_HARDNESS.put(Material.CLAY, 30);
        MATERIAL_HARDNESS.put(Material.DIRT, 25);
        MATERIAL_HARDNESS.put(Material.COARSE_DIRT, 25);
        MATERIAL_HARDNESS.put(Material.GRASS_BLOCK, 25);
        MATERIAL_HARDNESS.put(Material.PODZOL, 25);
        MATERIAL_HARDNESS.put(Material.MYCELIUM, 25);
        MATERIAL_HARDNESS.put(Material.MOSS_BLOCK, 22);
        MATERIAL_HARDNESS.put(Material.GRAVEL, 20);
        MATERIAL_HARDNESS.put(Material.SAND, 15);
        MATERIAL_HARDNESS.put(Material.RED_SAND, 15);
        MATERIAL_HARDNESS.put(Material.SNOW_BLOCK, 10);
        MATERIAL_HARDNESS.put(Material.MUD, 8);
    }
    
    /**
     * Create a new enhanced smooth operation with full customization.
     * @param player The player
     * @param region The region to smooth
     * @param iterations Number of smoothing iterations
     * @param heightFactor Height weighting factor
     * @param erodeSteepSlopes Whether to simulate erosion on steep slopes
     * @param preserveTopLayer Whether to preserve surface materials on top
     * @param naturalVariation Amount of natural variation (0.0-1.0) to add
     */
    public SmoothOperation(Player player, Region region, int iterations, double heightFactor,
                           boolean erodeSteepSlopes, boolean preserveTopLayer, double naturalVariation) {
        this.player = player;
        this.region = region;
        this.iterations = iterations;
        this.heightFactor = heightFactor;
        this.erodeSteepSlopes = erodeSteepSlopes;
        this.preserveTopLayer = preserveTopLayer;
        this.naturalVariation = Math.max(0.0, Math.min(1.0, naturalVariation));
    }
    
    /**
     * Create a new smooth operation with common parameters.
     * @param player The player
     * @param region The region to smooth
     * @param iterations Number of smoothing iterations
     * @param heightFactor Height weighting factor
     */
    public SmoothOperation(Player player, Region region, int iterations, double heightFactor) {
        this(player, region, iterations, heightFactor, true, true, 0.2);
    }
    
    /**
     * Create a new smooth operation with default parameters.
     * @param player The player
     * @param region The region to smooth
     */
    public SmoothOperation(Player player, Region region) {
        this(player, region, 4, 2.0, true, true, 0.2);
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = new HistoryEntry(player, world, getDescription());
        
        // First pass: collect all blocks in the region
        Map<Vector3, BlockData> originalBlocks = new HashMap<>();
        Map<Vector3, BlockData> currentBlocks = new HashMap<>();
        Map<Vector3, Material> topLayerMaterials = new HashMap<>(); // For preserving top layer
        
        // Track highest solid blocks for preserving top layer and finding steep slopes
        Map<Integer, Map<Integer, Integer>> highestSolidY = new HashMap<>();
        
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
                        
                        // Track highest solid block for terrain analysis
                        if (!isAirLike(blockData.getMaterial())) {
                            // Initialize the inner map if needed
                            highestSolidY.computeIfAbsent(x, k -> new HashMap<>());
                            
                            // Update highest Y for this X,Z column
                            Map<Integer, Integer> zMap = highestSolidY.get(x);
                            int currentHighest = zMap.getOrDefault(z, Integer.MIN_VALUE);
                            
                            if (y > currentHighest) {
                                zMap.put(z, y);
                                
                                // If it's the new highest and a surface material, track it
                                if (SURFACE_MATERIALS.contains(blockData.getMaterial())) {
                                    topLayerMaterials.put(new Vector3(x, y, z), blockData.getMaterial());
                                }
                            }
                        }
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
                
                // Process steep slopes with erosion if enabled
                if (erodeSteepSlopes && isOnSteepSlope(pos, highestSolidY)) {
                    BlockData erodedData = simulateErosion(pos, currentBlocks);
                    if (erodedData != null) {
                        newBlocks.put(pos, erodedData);
                        continue;
                    }
                }
                
                // Get the weighted average of surrounding blocks with geological awareness
                BlockData newData = getGeologicallyAwareBlockData(pos, currentBlocks);
                
                // Apply natural variation if enabled
                if (naturalVariation > 0 && newData != null && Math.random() < naturalVariation) {
                    newData = addNaturalVariation(newData);
                }
                
                if (newData != null) {
                    newBlocks.put(pos, newData);
                } else {
                    newBlocks.put(pos, blockEntry.getValue());
                }
            }
            
            // Update current blocks for next iteration
            currentBlocks = newBlocks;
        }
        
        // Final pass: preserve top layer materials if enabled
        if (preserveTopLayer) {
            for (Map.Entry<Vector3, Material> layerEntry : topLayerMaterials.entrySet()) {
                Vector3 topPos = layerEntry.getKey();
                
                // Skip if not in our working set or outside region
                if (!currentBlocks.containsKey(topPos) || !region.contains(topPos)) {
                    continue;
                }
                
                // Preserve the top layer material
                Material originalTopMaterial = layerEntry.getValue();
                if (SURFACE_MATERIALS.contains(originalTopMaterial)) {
                    currentBlocks.put(topPos, originalTopMaterial.createBlockData());
                }
            }
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
     * Determines if a block is on a steep slope based on surrounding terrain.
     * 
     * @param position The position to check
     * @param heightMap The map of highest blocks for each X,Z column
     * @return True if the block is on a steep slope
     */
    private boolean isOnSteepSlope(Vector3 position, Map<Integer, Map<Integer, Integer>> heightMap) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        
        // Check surrounding 8 blocks for steep height differences
        int steepThreshold = 2; // Height difference that constitutes a steep slope
        int steepNeighbors = 0;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Skip center
                
                Map<Integer, Integer> zMap = heightMap.getOrDefault(x + dx, Collections.emptyMap());
                int neighborHeight = zMap.getOrDefault(z + dz, y);
                
                if (Math.abs(neighborHeight - y) >= steepThreshold) {
                    steepNeighbors++;
                }
            }
        }
        
        // If enough neighbors have steep differences, it's a steep slope
        return steepNeighbors >= 3;
    }
    
    /**
     * Simulates erosion by considering material hardness and neighboring blocks.
     * 
     * @param position The position to erode
     * @param blocks The current block map
     * @return The eroded block data, or null if no erosion occurred
     */
    private BlockData simulateErosion(Vector3 position, Map<Vector3, BlockData> blocks) {
        BlockData currentData = blocks.get(position);
        if (currentData == null) return null;
        
        Material currentMaterial = currentData.getMaterial();
        int hardness = MATERIAL_HARDNESS.getOrDefault(currentMaterial, 30);
        
        // Calculate erosion probability based on hardness (softer = more likely to erode)
        double erosionProb = 1.0 - (hardness / 100.0);
        
        // Random check if this block should erode
        if (Math.random() > erosionProb) {
            return null; // No erosion
        }
        
        // Find a suitable softer material for this block to erode into
        List<Material> geologicalGroup = GEOLOGICAL_GROUPS.getOrDefault(
            currentMaterial, Collections.singletonList(currentMaterial));
        
        // Sort materials in the group by hardness (ascending)
        List<Material> softestFirst = new ArrayList<>(geologicalGroup);
        softestFirst.sort(Comparator.comparingInt(m -> MATERIAL_HARDNESS.getOrDefault(m, 50)));
        
        // Find a material softer than the current one
        for (Material material : softestFirst) {
            if (MATERIAL_HARDNESS.getOrDefault(material, 50) < hardness) {
                return material.createBlockData();
            }
        }
        
        // If no softer material in the group, use some fallbacks based on material type
        if (currentMaterial == Material.STONE || geologicalGroup.contains(Material.STONE)) {
            return Material.COBBLESTONE.createBlockData();
        } else if (currentMaterial == Material.GRASS_BLOCK) {
            return Material.DIRT.createBlockData();
        } else if (currentMaterial == Material.SANDSTONE) {
            return Material.SAND.createBlockData();
        } else if (currentMaterial == Material.DIRT) {
            return Material.COARSE_DIRT.createBlockData();
        }
        
        // No erosion if we couldn't find a suitable material
        return null;
    }
    
    /**
     * Add natural variation to a block to make terrain look less uniform.
     * 
     * @param blockData The original block data
     * @return Modified block data with natural variation
     */
    private BlockData addNaturalVariation(BlockData blockData) {
        Material baseMaterial = blockData.getMaterial();
        
        // Get geological group for this material
        List<Material> group = GEOLOGICAL_GROUPS.getOrDefault(
            baseMaterial, Collections.singletonList(baseMaterial));
        
        // Only add variation to geological materials
        if (group.size() <= 1) {
            return blockData;
        }
        
        // Randomly select a similar material from the same geological group
        Material newMaterial = group.get(new Random().nextInt(group.size()));
        return newMaterial.createBlockData();
    }
    
    /**
     * Get a weighted average of block data in the neighborhood with geological awareness.
     * This method prioritizes terrain smoothing while maintaining geological realism.
     * 
     * @param position The position
     * @param blocks The map of positions to block data
     * @return The most appropriate block data for smoothing
     */
    private BlockData getGeologicallyAwareBlockData(Vector3 position, Map<Vector3, BlockData> blocks) {
        Map<Material, Double> weights = new HashMap<>();
        double totalWeight = 0;
        Material currentMaterial = blocks.get(position).getMaterial();
        
        // Get geological group for this material
        List<Material> group = GEOLOGICAL_GROUPS.getOrDefault(
            currentMaterial, Collections.singletonList(currentMaterial));
        
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
                        
                        // Give bonus weight to materials in the same geological group
                        if (group.contains(material)) {
                            weight *= 1.5;
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
        String features = "";
        if (erodeSteepSlopes) features += " with erosion";
        if (preserveTopLayer) features += (features.isEmpty() ? " with" : " and") + " surface preservation";
        if (naturalVariation > 0) features += (features.isEmpty() ? " with" : " and") + 
                                             String.format(" %.0f%%", naturalVariation * 100) + " variation";
        
        return "Enhanced smooth operation" + features + 
               " (" + iterations + " iterations, height factor " + heightFactor + ")";
    }
    
    @Override
    public int getVolume() {
        return region.getVolume();
    }
} 