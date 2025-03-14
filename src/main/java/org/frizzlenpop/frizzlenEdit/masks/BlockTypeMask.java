package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A mask that matches blocks of specific types.
 */
public class BlockTypeMask implements Mask {
    private final Set<Material> materials;
    
    /**
     * Create a new block type mask with a single material.
     * @param material The material to match
     */
    public BlockTypeMask(Material material) {
        this.materials = new HashSet<>();
        this.materials.add(material);
    }
    
    /**
     * Create a new block type mask with multiple materials.
     * @param materials The materials to match
     */
    public BlockTypeMask(Material... materials) {
        this.materials = new HashSet<>(Arrays.asList(materials));
    }
    
    /**
     * Create a new block type mask with a set of materials.
     * @param materials The materials to match
     */
    public BlockTypeMask(Set<Material> materials) {
        this.materials = new HashSet<>(materials);
    }
    
    /**
     * Add a material to the mask.
     * @param material The material to add
     */
    public void addMaterial(Material material) {
        materials.add(material);
    }
    
    /**
     * Remove a material from the mask.
     * @param material The material to remove
     */
    public void removeMaterial(Material material) {
        materials.remove(material);
    }
    
    /**
     * Get the materials matched by this mask.
     * @return The materials
     */
    public Set<Material> getMaterials() {
        return new HashSet<>(materials);
    }
    
    @Override
    public boolean matches(Block block) {
        return materials.contains(block.getType());
    }
    
    @Override
    public String getDescription() {
        return materials.stream()
            .map(Material::name)
            .map(String::toLowerCase)
            .collect(Collectors.joining(","));
    }
} 