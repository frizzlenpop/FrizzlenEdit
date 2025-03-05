package org.frizzlenpop.frizzlenEdit.history;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.utils.Logger;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

/**
 * Manages operation history for undo/redo.
 */
public class HistoryManager {
    private final FrizzlenEdit plugin;
    
    // Maps player UUIDs to their undo history
    private final Map<UUID, Deque<HistoryEntry>> undoHistory = new HashMap<>();
    
    // Maps player UUIDs to their redo history
    private final Map<UUID, Deque<HistoryEntry>> redoHistory = new HashMap<>();
    
    /**
     * Create a new history manager.
     * @param plugin The plugin instance
     */
    public HistoryManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Add an entry to a player's history.
     * @param entry The history entry
     */
    public void addEntry(HistoryEntry entry) {
        Player player = entry.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Get or create the player's undo history
        Deque<HistoryEntry> history = undoHistory.computeIfAbsent(uuid, k -> new LinkedList<>());
        
        // Add the entry to the history
        history.addFirst(entry);
        
        // Limit the history size
        int maxSize = plugin.getConfigManager().getUndoHistorySize();
        while (history.size() > maxSize) {
            history.removeLast();
        }
        
        // Clear the redo history
        redoHistory.computeIfAbsent(uuid, k -> new LinkedList<>()).clear();
        
        Logger.info("Added history entry for " + player.getName() + ": " + entry.getDescription() + " (" + entry.getSize() + " blocks)");
    }
    
    /**
     * Undo the last operation for a player.
     * @param player The player
     * @return True if an operation was undone
     */
    public boolean undo(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Get the player's undo history
        Deque<HistoryEntry> history = undoHistory.get(uuid);
        if (history == null || history.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nothing to undo.");
            return false;
        }
        
        // Get the last entry
        HistoryEntry entry = history.removeFirst();
        
        // Undo the operation
        boolean success = entry.undo();
        
        if (success) {
            // Add the entry to the redo history
            redoHistory.computeIfAbsent(uuid, k -> new LinkedList<>()).addFirst(entry);
            
            player.sendMessage(ChatColor.GREEN + "Undone: " + entry.getDescription());
            Logger.info("Player " + player.getName() + " undid " + entry.getDescription());
        } else {
            player.sendMessage(ChatColor.RED + "Failed to undo: " + entry.getDescription());
            Logger.warning("Player " + player.getName() + " failed to undo " + entry.getDescription());
        }
        
        return success;
    }
    
    /**
     * Redo the last undone operation for a player.
     * @param player The player
     * @return True if an operation was redone
     */
    public boolean redo(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Get the player's redo history
        Deque<HistoryEntry> history = redoHistory.get(uuid);
        if (history == null || history.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nothing to redo.");
            return false;
        }
        
        // Get the last entry
        HistoryEntry entry = history.removeFirst();
        
        // Redo the operation
        boolean success = entry.redo();
        
        if (success) {
            // Add the entry back to the undo history
            undoHistory.computeIfAbsent(uuid, k -> new LinkedList<>()).addFirst(entry);
            
            player.sendMessage(ChatColor.GREEN + "Redone: " + entry.getDescription());
            Logger.info("Player " + player.getName() + " redid " + entry.getDescription());
        } else {
            player.sendMessage(ChatColor.RED + "Failed to redo: " + entry.getDescription());
            Logger.warning("Player " + player.getName() + " failed to redo " + entry.getDescription());
        }
        
        return success;
    }
    
    /**
     * Clear a player's history.
     * @param player The player
     */
    public void clearHistory(Player player) {
        UUID uuid = player.getUniqueId();
        undoHistory.remove(uuid);
        redoHistory.remove(uuid);
    }
    
    /**
     * Get the number of operations in a player's undo history.
     * @param player The player
     * @return The number of operations
     */
    public int getUndoHistorySize(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<HistoryEntry> history = undoHistory.get(uuid);
        return history == null ? 0 : history.size();
    }
    
    /**
     * Get the number of operations in a player's redo history.
     * @param player The player
     * @return The number of operations
     */
    public int getRedoHistorySize(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<HistoryEntry> history = redoHistory.get(uuid);
        return history == null ? 0 : history.size();
    }
} 