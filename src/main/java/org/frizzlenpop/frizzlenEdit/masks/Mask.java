package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.block.Block;

/**
 * Represents a mask that can be used to filter blocks in operations.
 * Masks determine whether a block should be affected by an operation.
 */
public interface Mask {
    
    /**
     * Check if a block matches this mask.
     * @param block The block to check
     * @return True if the block matches the mask, false otherwise
     */
    boolean matches(Block block);
    
    /**
     * Get a descriptive string for this mask.
     * @return A description of the mask
     */
    String getDescription();
    
    /**
     * Get a mask that inverts this mask.
     * @return The inverted mask
     */
    default Mask inverse() {
        return new InverseMask(this);
    }
} 