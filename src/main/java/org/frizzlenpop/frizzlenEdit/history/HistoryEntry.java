package org.frizzlenpop.frizzlenEdit.history;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single entry in the history, containing information needed to undo/redo an operation.
 */
public class HistoryEntry {
    // The player who performed the operation
    private final Player player;
    
    // The world the operation was performed in
    private final World world;
    
    // A description of the operation
    private final String description;
    
    // The previous block states (for undo)
    private final Map<Vector3, BlockState> previousStates = new HashMap<>();
    
    // The new block states (for redo)
    private final Map<Vector3, BlockState> newStates = new HashMap<>();
    
    /**
     * Create a new history entry.
     * @param player The player who performed the operation
     * @param world The world the operation was performed in
     * @param description A description of the operation
     */
    public HistoryEntry(Player player, World world, String description) {
        this.player = player;
        this.world = world;
        this.description = description;
    }
    
    /**
     * Get the player who performed this operation.
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get the world this operation was performed in.
     * @return The world
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Get the description of this operation.
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Add a block state to this history entry.
     * @param position The position of the block
     * @param oldState The previous state of the block
     * @param newState The new state of the block
     */
    public void addBlockState(Vector3 position, BlockState oldState, BlockState newState) {
        previousStates.put(position, oldState);
        newStates.put(position, newState);
    }
    
    /**
     * Get the map of previous block states.
     * @return The previous block states
     */
    public Map<Vector3, BlockState> getPreviousStates() {
        return previousStates;
    }
    
    /**
     * Get the map of new block states.
     * @return The new block states
     */
    public Map<Vector3, BlockState> getNewStates() {
        return newStates;
    }
    
    /**
     * Get the number of blocks affected by this operation.
     * @return The number of blocks
     */
    public int getSize() {
        return previousStates.size();
    }
    
    /**
     * Undo this operation.
     * @return True if the undo was successful
     */
    public boolean undo() {
        try {
            for (Map.Entry<Vector3, BlockState> entry : previousStates.entrySet()) {
                Vector3 pos = entry.getKey();
                BlockState state = entry.getValue();
                
                // Restore the previous state
                state.update(true, false);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Redo this operation.
     * @return True if the redo was successful
     */
    public boolean redo() {
        try {
            for (Map.Entry<Vector3, BlockState> entry : newStates.entrySet()) {
                Vector3 pos = entry.getKey();
                BlockState state = entry.getValue();
                
                // Restore the new state
                state.update(true, false);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 