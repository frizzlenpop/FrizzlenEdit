package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.Mask;

/**
 * Base class for operations with common functionality.
 */
public abstract class AbstractOperation implements Operation {
    protected final Player player;
    protected Mask mask;
    
    /**
     * Create a new operation.
     * @param player The player executing the operation
     */
    public AbstractOperation(Player player) {
        this.player = player;
        this.mask = null; // No mask by default
    }
    
    /**
     * Create a new operation with a mask.
     * @param player The player executing the operation
     * @param mask The mask to apply
     */
    public AbstractOperation(Player player, Mask mask) {
        this.player = player;
        this.mask = mask;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Set the mask for this operation.
     * @param mask The mask to set
     */
    public void setMask(Mask mask) {
        this.mask = mask;
    }
    
    /**
     * Get the current mask for this operation.
     * @return The mask, or null if no mask is set
     */
    public Mask getMask() {
        return mask;
    }
    
    /**
     * Check if a block matches the mask for this operation.
     * @param block The block to check
     * @return True if the block should be affected by this operation
     */
    protected boolean matchesMask(Block block) {
        // If no mask is set, all blocks match
        if (mask == null) {
            return true;
        }
        
        // Otherwise, check the mask
        return mask.matches(block);
    }
    
    /**
     * Create a history entry for this operation.
     * @param world The world in which the operation is being executed
     * @return The history entry
     */
    protected HistoryEntry createHistoryEntry(World world) {
        return new HistoryEntry(player, world, getDescription());
    }
    
    /**
     * Send a message to the player.
     * @param message The message to send
     */
    protected void sendMessage(String message) {
        player.sendMessage(message);
    }
} 