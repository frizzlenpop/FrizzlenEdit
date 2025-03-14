package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.AirMask;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * Operation that adds a layer on top of existing terrain.
 */
public class OverlayOperation extends AbstractOperation {
    private final Region region;
    private final Pattern pattern;
    private final int thickness;
    private final boolean ignoreWater;
    
    /**
     * Creates a new overlay operation with default thickness of 1.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param pattern The pattern to use for the overlay
     * @param ignoreWater Whether to ignore water when adding the overlay
     */
    public OverlayOperation(Player player, Region region, Pattern pattern, boolean ignoreWater) {
        this(player, region, pattern, 1, ignoreWater);
    }
    
    /**
     * Creates a new overlay operation with specified thickness.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param pattern The pattern to use for the overlay
     * @param thickness The thickness of the overlay in blocks
     * @param ignoreWater Whether to ignore water when adding the overlay
     */
    public OverlayOperation(Player player, Region region, Pattern pattern, int thickness, boolean ignoreWater) {
        super(player);
        this.region = region;
        this.pattern = pattern;
        this.thickness = Math.max(1, thickness); // Minimum thickness of 1
        this.ignoreWater = ignoreWater;
    }
    
    /**
     * Creates a new overlay operation with default thickness and a mask.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param pattern The pattern to use for the overlay
     * @param ignoreWater Whether to ignore water when adding the overlay
     * @param mask The mask to apply
     */
    public OverlayOperation(Player player, Region region, Pattern pattern, boolean ignoreWater, Mask mask) {
        this(player, region, pattern, 1, ignoreWater, mask);
    }
    
    /**
     * Creates a new overlay operation with specified thickness and a mask.
     * @param player The player executing the operation
     * @param region The region to overlay
     * @param pattern The pattern to use for the overlay
     * @param thickness The thickness of the overlay in blocks
     * @param ignoreWater Whether to ignore water when adding the overlay
     * @param mask The mask to apply
     */
    public OverlayOperation(Player player, Region region, Pattern pattern, int thickness, boolean ignoreWater, Mask mask) {
        super(player, mask);
        this.region = region;
        this.pattern = pattern;
        this.thickness = Math.max(1, thickness); // Minimum thickness of 1
        this.ignoreWater = ignoreWater;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Get region bounds
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // Create a mask to identify air blocks
        AirMask airMask = new AirMask();
        
        // First pass: Find the highest non-air block for each x,z column
        Map<Vector3, Integer> topYCoordinates = new HashMap<>();
        
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                // Find the highest non-air block
                for (int y = max.getY(); y >= min.getY(); y--) {
                    Block block = world.getBlockAt(x, y, z);
                    
                    if (!matchesMask(block)) {
                        continue;
                    }
                    
                    Material material = block.getType();
                    
                    // Skip air blocks
                    if (airMask.matches(block)) {
                        continue;
                    }
                    
                    // Skip water blocks if ignoreWater is true
                    if (ignoreWater && (material == Material.WATER || material == Material.LAVA)) {
                        continue;
                    }
                    
                    // Found a solid block, save its y coordinate
                    topYCoordinates.put(new Vector3(x, 0, z), y);
                    break;
                }
            }
        }
        
        // Second pass: Add the overlay
        for (Map.Entry<Vector3, Integer> entry2 : topYCoordinates.entrySet()) {
            Vector3 columnKey = entry2.getKey();
            int topY = entry2.getValue();
            
            int x = columnKey.getX();
            int z = columnKey.getZ();
            
            // Add the overlay
            for (int i = 0; i < thickness; i++) {
                int y = topY + 1 + i;
                
                // Skip if out of region bounds
                if (y > max.getY()) {
                    continue;
                }
                
                Block block = world.getBlockAt(x, y, z);
                
                // Only replace air blocks
                if (!airMask.matches(block)) {
                    continue;
                }
                
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
        
        sendMessage("Added overlay with " + affected + " blocks.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = "Overlay (region=" + region.getVolume() + " blocks, pattern=" + pattern.getDescription() + 
                      ", thickness=" + thickness;
        if (ignoreWater) {
            desc += ", ignoring water";
        }
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Conservative estimate: assume 1/4 of the region's columns will be affected
        return (region.getVolume() / (region.getHeight() * 4)) * thickness;
    }
} 