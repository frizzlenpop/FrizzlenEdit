package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
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
 * Operation that makes a region hollow by setting inner blocks to air and keeping the outer shell.
 * Optionally applies a pattern to the outer shell.
 */
public class HollowOperation extends AbstractOperation {
    private final Region region;
    private final Pattern shellPattern;
    private final int thickness;
    
    /**
     * Creates a new hollow operation with default shell thickness of 1.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellPattern The pattern to use for the shell (null to keep original blocks)
     */
    public HollowOperation(Player player, Region region, Pattern shellPattern) {
        this(player, region, shellPattern, 1);
    }
    
    /**
     * Creates a new hollow operation with a specified shell thickness.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellPattern The pattern to use for the shell (null to keep original blocks)
     * @param thickness The thickness of the shell in blocks
     */
    public HollowOperation(Player player, Region region, Pattern shellPattern, int thickness) {
        super(player);
        this.region = region;
        this.shellPattern = shellPattern;
        this.thickness = Math.max(1, thickness); // Minimum thickness of 1
    }
    
    /**
     * Creates a new hollow operation with a mask and default shell thickness of 1.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellPattern The pattern to use for the shell (null to keep original blocks)
     * @param mask The mask to apply
     */
    public HollowOperation(Player player, Region region, Pattern shellPattern, Mask mask) {
        this(player, region, shellPattern, 1, mask);
    }
    
    /**
     * Creates a new hollow operation with a mask and specified shell thickness.
     * @param player The player executing the operation
     * @param region The region to make hollow
     * @param shellPattern The pattern to use for the shell (null to keep original blocks)
     * @param thickness The thickness of the shell in blocks
     * @param mask The mask to apply
     */
    public HollowOperation(Player player, Region region, Pattern shellPattern, int thickness, Mask mask) {
        super(player, mask);
        this.region = region;
        this.shellPattern = shellPattern;
        this.thickness = Math.max(1, thickness); // Minimum thickness of 1
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Get region bounds
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // Check if region is too small to be hollowed
        int width = max.getX() - min.getX() + 1;
        int height = max.getY() - min.getY() + 1;
        int depth = max.getZ() - min.getZ() + 1;
        
        if (width <= thickness * 2 || height <= thickness * 2 || depth <= thickness * 2) {
            sendMessage("Region is too small to hollow with a thickness of " + thickness + ".");
            return entry;
        }
        
        // Create a BlockData for air
        BlockData airData = Material.AIR.createBlockData();
        
        // Iterate through the region
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    
                    // Check if the block matches the mask
                    if (!matchesMask(block)) {
                        continue;
                    }
                    
                    // Check if this is within the shell thickness
                    boolean isShell = 
                            x < min.getX() + thickness || x > max.getX() - thickness ||
                            y < min.getY() + thickness || y > max.getY() - thickness ||
                            z < min.getZ() + thickness || z > max.getZ() - thickness;
                    
                    BlockState oldState = block.getState();
                    BlockData newData;
                    
                    if (isShell) {
                        // If it's part of the shell and we have a pattern, use it
                        if (shellPattern != null) {
                            newData = shellPattern.getBlockData(block.getLocation());
                        } else {
                            // Keep original block for shell if no pattern is specified
                            continue;
                        }
                    } else {
                        // For inner blocks, make them air
                        newData = airData;
                    }
                    
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
        
        sendMessage("Made region hollow with " + affected + " blocks affected.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = "Hollow (region=" + region.getVolume() + " blocks";
        if (shellPattern != null) {
            desc += ", shell pattern=" + shellPattern.getDescription();
        }
        desc += ", thickness=" + thickness;
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Get region dimensions
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        int width = max.getX() - min.getX() + 1;
        int height = max.getY() - min.getY() + 1;
        int depth = max.getZ() - min.getZ() + 1;
        
        // Calculate volume of the inner region to be hollowed
        // If the region is too small, return 0
        if (width <= thickness * 2 || height <= thickness * 2 || depth <= thickness * 2) {
            return 0;
        }
        
        int innerWidth = width - (thickness * 2);
        int innerHeight = height - (thickness * 2);
        int innerDepth = depth - (thickness * 2);
        
        return innerWidth * innerHeight * innerDepth;
    }
} 