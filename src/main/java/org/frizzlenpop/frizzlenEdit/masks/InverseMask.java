package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.block.Block;

/**
 * A mask that inverts another mask.
 * If the original mask would match a block, this mask does not match it and vice versa.
 */
public class InverseMask implements Mask {
    private final Mask mask;
    
    /**
     * Create a new inverse mask.
     * @param mask The mask to invert
     */
    public InverseMask(Mask mask) {
        this.mask = mask;
    }
    
    @Override
    public boolean matches(Block block) {
        return !mask.matches(block);
    }
    
    @Override
    public String getDescription() {
        return "!" + mask.getDescription();
    }
    
    @Override
    public Mask inverse() {
        return mask; // Inverting an inverse returns the original
    }
} 