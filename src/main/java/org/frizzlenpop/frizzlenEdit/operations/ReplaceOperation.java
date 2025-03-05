package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * An operation that replaces blocks of one type with another.
 */
public class ReplaceOperation implements Operation {
    private final Player player;
    private final Region region;
    private final BlockData fromData;
    private final BlockData toData;
    
    /**
     * Create a new replace operation.
     * @param player The player
     * @param region The region
     * @param fromType The block type to replace
     * @param toType The block type to replace with
     * @throws IllegalArgumentException If the block type is invalid
     */
    public ReplaceOperation(Player player, Region region, String fromType, String toType) throws IllegalArgumentException {
        this.player = player;
        this.region = region;
        
        try {
            this.fromData = Bukkit.createBlockData(fromType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid 'from' block type: " + fromType);
        }
        
        try {
            this.toData = Bukkit.createBlockData(toType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid 'to' block type: " + toType);
        }
    }
    
    @Override
    public HistoryEntry execute() {
        World world = region.getWorld();
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Replace " + fromData.getAsString() + " with " + toData.getAsString());
        
        // Replace each matching block in the region
        for (Vector3 pos : region) {
            // Get the block
            Block block = pos.toBlock(world);
            BlockData currentData = block.getBlockData();
            
            // Check if the block matches the from type
            if (currentData.getMaterial() == fromData.getMaterial()) {
                // Save the previous state for undo
                BlockState oldState = block.getState();
                
                // Set the new block data
                block.setBlockData(toData);
                
                // Save the new state for redo
                BlockState newState = block.getState();
                
                // Add to history
                entry.addBlockState(pos, oldState, newState);
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
        return "Replace " + fromData.getAsString() + " with " + toData.getAsString();
    }
    
    @Override
    public int getVolume() {
        // This is an estimate, as we don't know exactly how many blocks will match
        return region.getVolume();
    }
} 