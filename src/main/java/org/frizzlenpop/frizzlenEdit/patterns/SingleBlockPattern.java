package org.frizzlenpop.frizzlenEdit.patterns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

/**
 * A pattern that always returns the same block data.
 */
public class SingleBlockPattern implements Pattern {
    
    private final BlockData blockData;
    
    /**
     * Create a new single block pattern.
     * @param blockData The block data to use
     */
    public SingleBlockPattern(BlockData blockData) {
        this.blockData = blockData;
    }
    
    /**
     * Create a new single block pattern from a block type string.
     * @param blockType The block type string (e.g., "stone", "oak_planks")
     * @throws IllegalArgumentException If the block type is invalid
     */
    public SingleBlockPattern(String blockType) throws IllegalArgumentException {
        try {
            this.blockData = Bukkit.createBlockData(blockType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid block type: " + blockType);
        }
    }
    
    @Override
    public BlockData getBlockData(Location location) {
        return blockData;
    }
    
    @Override
    public String getDescription() {
        return blockData.getAsString(true);
    }
    
    /**
     * Get the block data for this pattern.
     * @return The block data
     */
    public BlockData getBlockData() {
        return blockData;
    }
} 