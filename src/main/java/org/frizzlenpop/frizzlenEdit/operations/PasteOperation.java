package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.Map;

/**
 * An operation that pastes blocks from a clipboard.
 */
public class PasteOperation implements Operation {
    private final Player player;
    private final Vector3 position;
    private final Clipboard clipboard;
    private final boolean ignoreAir;
    private final World world;
    
    /**
     * Create a new paste operation.
     * @param player The player
     * @param position The position to paste at
     * @param clipboard The clipboard to paste from
     * @param ignoreAir Whether to ignore air blocks
     */
    public PasteOperation(Player player, Vector3 position, Clipboard clipboard, boolean ignoreAir) {
        this.player = player;
        this.position = position;
        this.clipboard = clipboard;
        this.ignoreAir = ignoreAir;
        this.world = player.getWorld();
    }
    
    @Override
    public HistoryEntry execute() {
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Paste");
        
        // Get the blocks from the clipboard
        Map<Vector3, BlockData> blocks = clipboard.getBlocks();
        
        // Paste each block
        for (Map.Entry<Vector3, BlockData> blockEntry : blocks.entrySet()) {
            Vector3 relPos = blockEntry.getKey();
            BlockData data = blockEntry.getValue();
            
            // Skip air blocks if requested
            if (ignoreAir && data.getMaterial().isAir()) {
                continue;
            }
            
            // Calculate the world position
            Vector3 worldPos = position.add(relPos);
            
            // Get the block
            Block block = worldPos.toBlock(world);
            
            // Save the previous state for undo
            BlockState oldState = block.getState();
            
            // Set the new block data
            block.setBlockData(data);
            
            // Save the new state for redo
            BlockState newState = block.getState();
            
            // Add to history
            entry.addBlockState(worldPos, oldState, newState);
        }
        
        return entry;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        return "Paste";
    }
    
    @Override
    public int getVolume() {
        if (ignoreAir) {
            // Count non-air blocks
            int count = 0;
            for (BlockData data : clipboard.getBlocks().values()) {
                if (!data.getMaterial().isAir()) {
                    count++;
                }
            }
            return count;
        } else {
            return clipboard.getVolume();
        }
    }
} 