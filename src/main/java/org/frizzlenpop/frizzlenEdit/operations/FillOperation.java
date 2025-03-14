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
 * Operation that fills a region with a pattern.
 */
public class FillOperation extends AbstractOperation {
    private final Region region;
    private final Pattern pattern;
    
    /**
     * Creates a new fill operation.
     * @param player The player executing the operation
     * @param region The region to fill
     * @param pattern The pattern to use for filling
     */
    public FillOperation(Player player, Region region, Pattern pattern) {
        super(player);
        this.region = region;
        this.pattern = pattern;
    }
    
    /**
     * Creates a new fill operation with a mask.
     * @param player The player executing the operation
     * @param region The region to fill
     * @param pattern The pattern to use for filling
     * @param mask The mask to apply
     */
    public FillOperation(Player player, Region region, Pattern pattern, Mask mask) {
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
        
        // Iterate through the region
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
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
        
        sendMessage("Filled region with " + affected + " blocks.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = "Fill (region=" + region.getVolume() + " blocks, pattern=" + pattern.getDescription();
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        return region.getVolume();
    }
} 