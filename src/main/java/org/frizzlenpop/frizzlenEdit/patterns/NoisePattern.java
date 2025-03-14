package org.frizzlenpop.frizzlenEdit.patterns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.frizzlenpop.frizzlenEdit.utils.PerlinNoiseGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A pattern that selects between multiple patterns based on Perlin noise
 */
public class NoisePattern implements Pattern {
    private final List<Pattern> patterns = new ArrayList<>();
    private final PerlinNoiseGenerator noiseGenerator;
    private final double scale;
    
    /**
     * Create a new noise pattern with the specified scale
     * @param scale The scale of the noise (higher values = smoother transitions)
     */
    public NoisePattern(double scale) {
        this.scale = Math.max(0.1, scale); // Ensure scale is at least 0.1
        this.noiseGenerator = new PerlinNoiseGenerator(System.currentTimeMillis()); // Use current time as seed
    }
    
    /**
     * Add a pattern to the noise pattern
     * @param pattern The pattern to add
     */
    public void add(Pattern pattern) {
        patterns.add(pattern);
    }
    
    /**
     * Get the number of patterns in this noise pattern
     * @return The number of patterns
     */
    public int size() {
        return patterns.size();
    }
    
    @Override
    public BlockData getBlockData(Location location) {
        if (patterns.isEmpty()) {
            return null;
        }
        
        if (patterns.size() == 1) {
            return patterns.get(0).getBlockData(location);
        }
        
        // Generate noise value between -1 and 1
        double noiseValue = noiseGenerator.noise(
                location.getX() / scale,
                location.getY() / scale,
                location.getZ() / scale
        );
        
        // Normalize to 0-1 range
        noiseValue = (noiseValue + 1) / 2;
        
        // Map to pattern index
        int patternIndex = (int) (noiseValue * patterns.size());
        patternIndex = Math.min(patternIndex, patterns.size() - 1); // Ensure we don't exceed the bounds
        
        return patterns.get(patternIndex).getBlockData(location);
    }
    
    @Override
    public boolean apply(Block block) {
        BlockData data = getBlockData(block.getLocation());
        if (data == null) {
            return false;
        }
        
        if (!block.getBlockData().equals(data)) {
            block.setBlockData(data);
            return true;
        }
        
        return false;
    }
    
    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder("Noise[scale=" + scale + ", ");
        
        for (int i = 0; i < patterns.size(); i++) {
            if (i > 0) {
                description.append(", ");
            }
            description.append(patterns.get(i).getDescription());
        }
        
        description.append("]");
        return description.toString();
    }
} 