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
 * Operation that creates an outline frame around a region.
 * Only the edges of the region are filled.
 */
public class OutlineOperation extends AbstractOperation {
    private final Region region;
    private final Pattern pattern;
    
    /**
     * Creates a new outline operation.
     * @param player The player executing the operation
     * @param region The region to outline
     * @param pattern The pattern to use for the outline
     */
    public OutlineOperation(Player player, Region region, Pattern pattern) {
        super(player);
        this.region = region;
        this.pattern = pattern;
    }
    
    /**
     * Creates a new outline operation with a mask.
     * @param player The player executing the operation
     * @param region The region to outline
     * @param pattern The pattern to use for the outline
     * @param mask The mask to apply
     */
    public OutlineOperation(Player player, Region region, Pattern pattern, Mask mask) {
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
        
        // Iterate through the region and create the outline
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    // Check if this position is on an edge of the region
                    boolean isEdge = 
                            (x == min.getX() || x == max.getX()) &&
                            (y == min.getY() || y == max.getY() || z == min.getZ() || z == max.getZ()) ||
                            (z == min.getZ() || z == max.getZ()) &&
                            (y == min.getY() || y == max.getY() || x == min.getX() || x == max.getX()) ||
                            (y == min.getY() || y == max.getY()) &&
                            (x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ());
                            
                    if (isEdge) {
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
        
        sendMessage("Created outline with " + affected + " blocks.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = "Outline (region=" + region.getVolume() + " blocks, pattern=" + pattern.getDescription();
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Calculate the outline
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        int xLength = max.getX() - min.getX() + 1;
        int yLength = max.getY() - min.getY() + 1;
        int zLength = max.getZ() - min.getZ() + 1;
        
        // For the outline, we need the 12 edges of the cuboid
        int edgeVolume = 0;
        
        // X-aligned edges (4)
        edgeVolume += xLength * 4;
        
        // Y-aligned edges (4)
        edgeVolume += yLength * 4;
        
        // Z-aligned edges (4)
        edgeVolume += zLength * 4;
        
        // Subtract 8 for the corners (counted twice)
        edgeVolume -= 8;
        
        return edgeVolume;
    }
} 