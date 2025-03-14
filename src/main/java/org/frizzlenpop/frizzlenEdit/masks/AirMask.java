package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.Set;

/**
 * A mask that matches air blocks.
 */
public class AirMask implements Mask {
    private static final Set<Material> AIR_MATERIALS = new HashSet<>();
    
    static {
        AIR_MATERIALS.add(Material.AIR);
        AIR_MATERIALS.add(Material.CAVE_AIR);
        AIR_MATERIALS.add(Material.VOID_AIR);
        // Add any other materials that should be considered "air"
    }
    
    @Override
    public boolean matches(Block block) {
        return AIR_MATERIALS.contains(block.getType());
    }
    
    @Override
    public String getDescription() {
        return "#air";
    }
    
    /**
     * Check if a material is considered air.
     * @param material The material to check
     * @return True if the material is considered air
     */
    public static boolean isAir(Material material) {
        return AIR_MATERIALS.contains(material);
    }
    
    /**
     * Check if a block has air material.
     * @param block The block to check
     * @return True if the block has air material
     */
    public static boolean isAir(Block block) {
        return isAir(block.getType());
    }
    
    /**
     * Check if a block data is air.
     * @param data The block data to check
     * @return True if the block data is air
     */
    public static boolean isAir(BlockData data) {
        return isAir(data.getMaterial());
    }
} 