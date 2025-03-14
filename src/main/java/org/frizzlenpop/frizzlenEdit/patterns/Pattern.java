package org.frizzlenpop.frizzlenEdit.patterns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * Represents a pattern that can be applied to blocks in a region.
 * Patterns are used to determine which block type should be placed at a specific location.
 */
public interface Pattern {
    
    /**
     * Get the block data that should be applied at a specific location.
     * @param location The location to get block data for
     * @return The block data to apply
     */
    BlockData getBlockData(Location location);
    
    /**
     * Apply this pattern directly to a block.
     * @param block The block to apply the pattern to
     * @return True if the block was changed, false otherwise
     */
    default boolean apply(Block block) {
        BlockData newData = getBlockData(block.getLocation());
        if (!block.getBlockData().equals(newData)) {
            block.setBlockData(newData);
            return true;
        }
        return false;
    }
    
    /**
     * Get a descriptive string for this pattern.
     * @return A description of the pattern
     */
    String getDescription();
} 