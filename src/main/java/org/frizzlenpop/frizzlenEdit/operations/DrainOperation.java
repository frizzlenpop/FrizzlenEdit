package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashSet;
import java.util.Set;

/**
 * An operation that drains water and other liquids from a region.
 */
public class DrainOperation implements Operation {
    private final Player player;
    private final Region region;
    private final int radius;
    private final boolean removeAllLiquids;
    
    // Materials that are considered liquids
    private static final Set<Material> LIQUID_MATERIALS = new HashSet<>();
    
    static {
        // Water materials
        LIQUID_MATERIALS.add(Material.WATER);
        LIQUID_MATERIALS.add(Material.BUBBLE_COLUMN);
        LIQUID_MATERIALS.add(Material.KELP);
        LIQUID_MATERIALS.add(Material.KELP_PLANT);
        LIQUID_MATERIALS.add(Material.SEAGRASS);
        LIQUID_MATERIALS.add(Material.TALL_SEAGRASS);
        
        // Lava materials (only if removeAllLiquids is true)
        LIQUID_MATERIALS.add(Material.LAVA);
    }
    
    /**
     * Create a new drain operation.
     * @param player The player
     * @param region The region to drain
     * @param radius The radius (if sphere is used)
     * @param removeAllLiquids Whether to remove all liquids (including lava) or just water
     */
    public DrainOperation(Player player, Region region, int radius, boolean removeAllLiquids) {
        this.player = player;
        this.region = region;
        this.radius = radius;
        this.removeAllLiquids = removeAllLiquids;
    }
    
    /**
     * Create a new drain operation with default parameters (water only).
     * @param player The player
     * @param region The region to drain
     */
    public DrainOperation(Player player, Region region) {
        this(player, region, 0, false);
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = new HistoryEntry(player, world, getDescription());
        
        // Iterate through all blocks in the region
        for (Vector3 pos : region) {
            Block block = pos.toBlock(world);
            Material material = block.getType();
            
            // Skip if not a liquid
            if (!isLiquid(material)) {
                continue;
            }
            
            // Save the previous state for undo
            entry.addBlockState(pos, block.getState(), null);
            
            // Set to air
            block.setType(Material.AIR);
            
            // Save the new state for redo
            entry.addBlockState(pos, null, block.getState());
        }
        
        return entry;
    }
    
    /**
     * Check if a material is a liquid that should be drained.
     * @param material The material to check
     * @return True if the material is a liquid
     */
    private boolean isLiquid(Material material) {
        if (LIQUID_MATERIALS.contains(material)) {
            // If not removing all liquids, skip lava
            if (!removeAllLiquids && material == Material.LAVA) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        String liquids = removeAllLiquids ? "all liquids" : "water";
        return "Drain " + liquids + " operation";
    }
    
    @Override
    public int getVolume() {
        return region.getVolume();
    }
} 