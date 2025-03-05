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
 * An operation that sets all blocks in a region to a specific type.
 */
public class SetOperation implements Operation {
    private final Player player;
    private final Region region;
    private final BlockData blockData;
    
    /**
     * Create a new set operation.
     * @param player The player
     * @param region The region
     * @param blockType The block type
     * @throws IllegalArgumentException If the block type is invalid
     */
    public SetOperation(Player player, Region region, String blockType) throws IllegalArgumentException {
        this.player = player;
        this.region = region;
        
        try {
            this.blockData = Bukkit.createBlockData(blockType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid block type: " + blockType);
        }
    }
    
    @Override
    public HistoryEntry execute() {
        World world = region.getWorld();
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Set " + blockData.getAsString());
        
        // Set each block in the region
        for (Vector3 pos : region) {
            // Get the block
            Block block = pos.toBlock(world);
            
            // Save the previous state for undo
            BlockState oldState = block.getState();
            
            // Set the new block data
            block.setBlockData(blockData);
            
            // Save the new state for redo
            BlockState newState = block.getState();
            
            // Add to history
            entry.addBlockState(pos, oldState, newState);
        }
        
        return entry;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        return "Set " + blockData.getAsString();
    }
    
    @Override
    public int getVolume() {
        return region.getVolume();
    }
} 