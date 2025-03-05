package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;

/**
 * Represents an operation that can be executed and undone.
 */
public interface Operation {
    /**
     * Execute this operation.
     * @return A history entry that can be used to undo the operation
     */
    HistoryEntry execute();
    
    /**
     * Get the player who initiated this operation.
     * @return The player
     */
    Player getPlayer();
    
    /**
     * Get a description of this operation for display.
     * @return The description
     */
    String getDescription();
    
    /**
     * Get the volume of blocks affected by this operation.
     * @return The volume
     */
    int getVolume();
} 