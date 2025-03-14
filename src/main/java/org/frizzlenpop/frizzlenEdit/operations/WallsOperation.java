package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Operation that creates walls around a region.
 * Walls are created on the sides of the region (not the top and bottom).
 */
public class WallsOperation extends AbstractOperation {
    private final Region region;
    private final Pattern pattern;
    
    /**
     * Creates a new walls operation.
     * @param player The player executing the operation
     * @param region The region around which to create walls
     * @param pattern The pattern to use for the walls
     */
    public WallsOperation(Player player, Region region, Pattern pattern) {
        super(player);
        this.region = region;
        this.pattern = pattern;
    }
    
    /**
     * Creates a new walls operation with a mask.
     * @param player The player executing the operation
     * @param region The region around which to create walls
     * @param pattern The pattern to use for the walls
     * @param mask The mask to apply
     */
    public WallsOperation(Player player, Region region, Pattern pattern, Mask mask) {
        super(player, mask);
        this.region = region;
        this.pattern = pattern;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Get region bounds
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // Iterate through the region and create walls
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    // Only process blocks on the perimeter
                    boolean isPerimeter = 
                            x == min.getX() || x == max.getX() || 
                            z == min.getZ() || z == max.getZ();
                            
                    if (isPerimeter) {
                        Block block = world.getBlockAt(x, y, z);
                        
                        // Check if the block matches the mask
                        if (!matchesMask(block)) {
                            continue;
                        }
                        
                        BlockState oldState = block.getState();
                        BlockData newData = pattern.getBlockData(block.getLocation());
                        
                        if (newData != null && !oldState.getBlockData().equals(newData)) {
                            Vector3 pos = new Vector3(x, y, z);
                            
                            // Save the old state for undo
                            entry.addBlockState(pos, oldState, null);
                            
                            // Set the new block data
                            block.setBlockData(newData);
                            
                            // Save the new state for redo
                            entry.addBlockState(pos, null, block.getState());
                            
                            affected++;
                        }
                    }
                }
            }
        }
        
        sendMessage("Created walls with " + affected + " blocks.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = "Walls (region=" + region.getVolume() + " blocks, pattern=" + pattern.getDescription();
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Calculate the perimeter
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        int xLength = max.getX() - min.getX() + 1;
        int yLength = max.getY() - min.getY() + 1;
        int zLength = max.getZ() - min.getZ() + 1;
        
        // Calculate the perimeter blocks
        int xWallsVolume = zLength * yLength * 2; // Both x-walls
        int zWallsVolume = (xLength - 2) * yLength * 2; // Both z-walls, minus the corners (already counted)
        
        return xWallsVolume + zWallsVolume;
    }
} 