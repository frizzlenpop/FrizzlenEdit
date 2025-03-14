package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.AirMask;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.NoiseGenerator;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.Random;

/**
 * Operation that generates realistic caves in a region using 3D noise.
 */
public class CavesOperation extends AbstractOperation {
    private final Region region;
    private final Random random;
    private final NoiseGenerator noiseGen;
    private final double threshold;
    private final double scale;
    private final boolean addOres;
    private final double oreFrequency;
    
    // Air block data for cave generation
    private final BlockData airData;
    
    /**
     * Creates a new caves operation with default settings.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     */
    public CavesOperation(Player player, Region region) {
        this(player, region, 0.4, 0.03, true, 0.1);
    }
    
    /**
     * Creates a new caves operation with custom settings.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     * @param threshold The noise threshold for cave generation (0.0-1.0, higher = more open space)
     * @param scale The scale of the noise (smaller = larger caves)
     * @param addOres Whether to add ores to the cave walls
     * @param oreFrequency The frequency of ore generation (0.0-1.0)
     */
    public CavesOperation(Player player, Region region, double threshold, double scale, boolean addOres, double oreFrequency) {
        super(player);
        this.region = region;
        this.threshold = Math.max(0.1, Math.min(0.9, threshold)); // Threshold between 0.1 and 0.9
        this.scale = Math.max(0.01, Math.min(0.1, scale)); // Scale between 0.01 and 0.1
        this.addOres = addOres;
        this.oreFrequency = Math.max(0.0, Math.min(0.5, oreFrequency)); // Ore frequency between 0.0 and 0.5
        this.random = new Random();
        this.noiseGen = new NoiseGenerator(random.nextLong());
        this.airData = Material.AIR.createBlockData();
    }
    
    /**
     * Creates a new caves operation with custom settings and a mask.
     * @param player The player executing the operation
     * @param region The region in which to generate caves
     * @param threshold The noise threshold for cave generation (0.0-1.0, higher = more open space)
     * @param scale The scale of the noise (smaller = larger caves)
     * @param addOres Whether to add ores to the cave walls
     * @param oreFrequency The frequency of ore generation (0.0-1.0)
     * @param mask The mask to apply
     */
    public CavesOperation(Player player, Region region, double threshold, double scale, boolean addOres, double oreFrequency, Mask mask) {
        super(player, mask);
        this.region = region;
        this.threshold = Math.max(0.1, Math.min(0.9, threshold)); // Threshold between 0.1 and 0.9
        this.scale = Math.max(0.01, Math.min(0.1, scale)); // Scale between 0.01 and 0.1
        this.addOres = addOres;
        this.oreFrequency = Math.max(0.0, Math.min(0.5, oreFrequency)); // Ore frequency between 0.0 and 0.5
        this.random = new Random();
        this.noiseGen = new NoiseGenerator(random.nextLong());
        this.airData = Material.AIR.createBlockData();
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Get region bounds
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // Create a mask to identify air blocks
        AirMask airMask = new AirMask();
        
        // Track affected blocks for ore generation
        boolean[][][] affectedBlocks = null;
        if (addOres) {
            int width = max.getX() - min.getX() + 1;
            int height = max.getY() - min.getY() + 1;
            int depth = max.getZ() - min.getZ() + 1;
            affectedBlocks = new boolean[width][height][depth];
        }
        
        // First pass: Carve caves
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    
                    // Skip if block doesn't match mask
                    if (!matchesMask(block)) {
                        continue;
                    }
                    
                    // Skip air blocks
                    if (airMask.matches(block)) {
                        continue;
                    }
                    
                    // Calculate noise value for this position
                    double noise = noiseGen.noise(x * scale, y * scale, z * scale);
                    
                    // Adjust noise based on depth (more caves in the lower part of the region)
                    double depthFactor = 1.0 - ((double)(y - min.getY()) / (double)(max.getY() - min.getY()));
                    noise += depthFactor * 0.2; // Boost noise for lower areas
                    
                    // Create a cave where noise exceeds threshold
                    if (noise > threshold) {
                        BlockState oldState = block.getState();
                        
                        Vector3 pos = new Vector3(x, y, z);
                        
                        // Save the old state for undo
                        entry.addBlockState(pos, oldState, null);
                        
                        // Set the block to air
                        block.setBlockData(airData);
                        
                        // Save the new state for redo
                        entry.addBlockState(pos, null, block.getState());
                        
                        affected++;
                        
                        // Mark this block as affected for ore generation
                        if (addOres) {
                            int relX = x - min.getX();
                            int relY = y - min.getY();
                            int relZ = z - min.getZ();
                            affectedBlocks[relX][relY][relZ] = true;
                        }
                    }
                }
            }
        }
        
        // Second pass: Add ores to cave walls
        if (addOres) {
            int width = max.getX() - min.getX() + 1;
            int height = max.getY() - min.getY() + 1;
            int depth = max.getZ() - min.getZ() + 1;
            int oresAdded = 0;
            
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    for (int y = 0; y < height; y++) {
                        // Only process blocks adjacent to air
                        if (!isAdjacentToAir(affectedBlocks, x, y, z, width, height, depth)) {
                            continue;
                        }
                        
                        // Get actual world coordinates
                        int worldX = x + min.getX();
                        int worldY = y + min.getY();
                        int worldZ = z + min.getZ();
                        
                        Block block = world.getBlockAt(worldX, worldY, worldZ);
                        
                        // Skip if block doesn't match mask
                        if (!matchesMask(block)) {
                            continue;
                        }
                        
                        // Skip air blocks
                        if (airMask.matches(block)) {
                            continue;
                        }
                        
                        // Determine if this block should be an ore
                        double oreNoise = noiseGen.noise(worldX * 0.1, worldY * 0.1, worldZ * 0.1);
                        if (oreNoise > (1.0 - oreFrequency)) {
                            BlockState oldState = block.getState();
                            Material oreMaterial = selectOreMaterial(worldY, min.getY(), max.getY());
                            BlockData oreData = oreMaterial.createBlockData();
                            
                            Vector3 pos = new Vector3(worldX, worldY, worldZ);
                            
                            // Save the old state for undo
                            entry.addBlockState(pos, oldState, null);
                            
                            // Set the block to the ore
                            block.setBlockData(oreData);
                            
                            // Save the new state for redo
                            entry.addBlockState(pos, null, block.getState());
                            
                            oresAdded++;
                        }
                    }
                }
            }
            
            affected += oresAdded;
            sendMessage("Generated caves with " + (affected - oresAdded) + " blocks carved and " + oresAdded + " ores added.");
        } else {
            sendMessage("Generated caves with " + affected + " blocks carved.");
        }
        
        return entry;
    }
    
    /**
     * Check if a block is adjacent to an air block.
     * @param affectedBlocks 3D array of affected blocks
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param width Region width
     * @param height Region height
     * @param depth Region depth
     * @return True if adjacent to air
     */
    private boolean isAdjacentToAir(boolean[][][] affectedBlocks, int x, int y, int z, int width, int height, int depth) {
        // Check the 6 adjacent blocks
        if (x > 0 && affectedBlocks[x-1][y][z]) return true;
        if (x < width-1 && affectedBlocks[x+1][y][z]) return true;
        if (y > 0 && affectedBlocks[x][y-1][z]) return true;
        if (y < height-1 && affectedBlocks[x][y+1][z]) return true;
        if (z > 0 && affectedBlocks[x][y][z-1]) return true;
        if (z < depth-1 && affectedBlocks[x][y][z+1]) return true;
        
        return false;
    }
    
    /**
     * Select an ore material based on depth.
     * @param y Current Y coordinate
     * @param minY Minimum Y coordinate of the region
     * @param maxY Maximum Y coordinate of the region
     * @return The ore material
     */
    private Material selectOreMaterial(int y, int minY, int maxY) {
        // Calculate relative depth (0.0 = bottom, 1.0 = top)
        double depth = 1.0 - ((double)(y - minY) / (double)(maxY - minY));
        
        // Use random value and depth to select ore
        double rand = random.nextDouble();
        
        // Deep ores (diamonds, redstone, lapis)
        if (depth > 0.8) {
            if (rand < 0.05) return Material.DIAMOND_ORE;
            if (rand < 0.15) return Material.REDSTONE_ORE;
            if (rand < 0.25) return Material.LAPIS_ORE;
            return Material.COAL_ORE;
        }
        // Mid-depth ores (gold, iron)
        else if (depth > 0.4) {
            if (rand < 0.1) return Material.GOLD_ORE;
            if (rand < 0.3) return Material.IRON_ORE;
            return Material.COAL_ORE;
        }
        // Surface ores (coal, copper)
        else {
            if (rand < 0.4) return Material.COPPER_ORE;
            return Material.COAL_ORE;
        }
    }
    
    @Override
    public String getDescription() {
        String desc = "Caves (region=" + region.getVolume() + " blocks, threshold=" + threshold + 
                      ", scale=" + scale;
        if (addOres) {
            desc += ", with ores (frequency=" + oreFrequency + ")";
        }
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Estimate that approximately (threshold)% of the region will be affected
        return (int)(region.getVolume() * threshold);
    }
} 