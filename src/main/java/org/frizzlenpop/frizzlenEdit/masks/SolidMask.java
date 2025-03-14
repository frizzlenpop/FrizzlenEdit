package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.block.Block;

/**
 * A mask that matches solid (non-air) blocks.
 */
public class SolidMask implements Mask {
    
    @Override
    public boolean matches(Block block) {
        return !AirMask.isAir(block);
    }
    
    @Override
    public String getDescription() {
        return "#solid";
    }
} 