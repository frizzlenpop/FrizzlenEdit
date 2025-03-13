package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * An operation that creates a cylinder of blocks.
 */
public class CylinderOperation implements Operation {
    private final Player player;
    private final Vector3 center;
    private final Material material;
    private final int radius;
    private final int height;
    private final boolean hollow;
    
    /**
     * Create a new cylinder operation.
     * @param player The player
     * @param center The center position of the cylinder
     * @param material The material to use
     * @param radius The radius of the cylinder
     * @param height The height of the cylinder
     * @param hollow Whether the cylinder should be hollow
     */
    public CylinderOperation(Player player, Vector3 center, Material material, int radius, int height, boolean hollow) {
        this.player = player;
        this.center = center;
        this.material = material;
        this.radius = radius;
        this.height = height;
        this.hollow = hollow;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = new HistoryEntry(player, world, getDescription());
        
        // Create block data for the material
        BlockData blockData = material.createBlockData();
        
        // Calculate the bounds of the cylinder
        int radiusSquared = radius * radius;
        
        // Loop through all blocks in the cylinder bounding box
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Calculate the distance squared from the center
                int distanceSquared = x*x + z*z;
                
                // Skip blocks outside the circle
                if (distanceSquared > radiusSquared) {
                    continue;
                }
                
                // For hollow cylinders, skip blocks that aren't on the edge
                if (hollow && distanceSquared < (radiusSquared - 2*radius + 1)) {
                    continue;
                }
                
                // Set blocks along the height
                for (int y = 0; y < height; y++) {
                    // For hollow cylinders, only set blocks on the top, bottom, or edge
                    if (hollow && y > 0 && y < height - 1) {
                        continue;
                    }
                    
                    // Calculate the position of this block
                    Vector3 pos = center.add(new Vector3(x, y, z));
                    
                    // Get the block
                    Block block = pos.toBlock(world);
                    
                    // Skip air blocks if we're not changing anything
                    if (block.getType() == material) {
                        continue;
                    }
                    
                    // Save the previous state for undo
                    entry.addBlockState(pos, block.getState(), null);
                    
                    // Set the block
                    block.setBlockData(blockData);
                    
                    // Save the new state for redo
                    entry.addBlockState(pos, null, block.getState());
                }
            }
        }
        
        return entry;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        String type = hollow ? "hollow" : "solid";
        return type + " cylinder of " + material.name().toLowerCase() + " (radius: " + radius + ", height: " + height + ")";
    }
    
    @Override
    public int getVolume() {
        // Calculate the approximate volume of the cylinder
        if (hollow) {
            // For a hollow cylinder, we're just creating the shell
            int topBottomArea = (int)(Math.PI * radius * radius);
            int sideArea = (int)(2 * Math.PI * radius * (height - 2));
            return topBottomArea * 2 + sideArea;
        } else {
            // For a solid cylinder, use the standard formula
            return (int)(Math.PI * radius * radius * height);
        }
    }
} 