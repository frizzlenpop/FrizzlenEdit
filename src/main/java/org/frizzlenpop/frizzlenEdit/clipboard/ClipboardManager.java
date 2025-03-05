package org.frizzlenpop.frizzlenEdit.clipboard;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.operations.Operation;
import org.frizzlenpop.frizzlenEdit.operations.PasteOperation;
import org.frizzlenpop.frizzlenEdit.operations.BatchPasteOperation;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages player clipboards.
 */
public class ClipboardManager {
    private final FrizzlenEdit plugin;
    private final Map<UUID, Clipboard> clipboards = new HashMap<>();
    
    public ClipboardManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if a player has a clipboard.
     * @param player The player
     * @return True if the player has a clipboard
     */
    public boolean hasClipboard(Player player) {
        return clipboards.containsKey(player.getUniqueId());
    }
    
    /**
     * Get a player's clipboard.
     * @param player The player
     * @return The player's clipboard, or null if they don't have one
     */
    public Clipboard getClipboard(Player player) {
        return clipboards.get(player.getUniqueId());
    }
    
    /**
     * Set a player's clipboard.
     * @param player The player
     * @param clipboard The clipboard
     */
    public void setClipboard(Player player, Clipboard clipboard) {
        clipboards.put(player.getUniqueId(), clipboard);
    }
    
    /**
     * Copy a region to a player's clipboard.
     * @param player The player
     * @param region The region to copy
     * @param origin The origin point (usually player's position)
     */
    public void copy(Player player, Region region, Vector3 origin) {
        // Check if the region is too large
        int maxSize = plugin.getConfigManager().getClipboardSizeLimit();
        int volume = region.getVolume();
        
        if (volume > maxSize) {
            player.sendMessage(ChatColor.RED + "Selection too large: " + volume + " blocks. Maximum is " + maxSize + ".");
            return;
        }
        
        // Create a new clipboard
        Clipboard clipboard = new Clipboard(region, origin);
        
        // Copy the blocks asynchronously
        plugin.runAsync(() -> {
            try {
                Logger.info("Player " + player.getName() + " copying " + volume + " blocks");
                clipboard.copy(region, region.getWorld());
                
                // Store the clipboard on the main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    setClipboard(player, clipboard);
                    player.sendMessage(ChatColor.GREEN + "Copied " + volume + " blocks to clipboard.");
                });
            } catch (Exception e) {
                Logger.severe("Error copying blocks: " + e.getMessage());
                e.printStackTrace();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error copying blocks: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Cut blocks from a region to a player's clipboard.
     * @param player The player
     * @param region The region to cut
     * @param origin The origin point
     */
    public void cut(Player player, Region region, Vector3 origin) {
        // First copy the region
        copy(player, region, origin);
        
        // Then create an operation to set the region to air
        Operation setAirOperation = plugin.getOperationManager().createSetOperation(
            player, region, "minecraft:air");
            
        // Execute the operation
        plugin.getOperationManager().execute(player, setAirOperation);
    }
    
    /**
     * Paste a player's clipboard at their current position.
     * @param player The player
     * @param ignoreAir Whether to ignore air blocks
     */
    public void paste(Player player, boolean ignoreAir) {
        if (!hasClipboard(player)) {
            player.sendMessage(ChatColor.RED + "You don't have a clipboard.");
            return;
        }
        
        Clipboard clipboard = getClipboard(player);
        Vector3 position = Vector3.fromLocation(player.getLocation());
        
        // Create a paste operation
        PasteOperation operation = new PasteOperation(player, position, clipboard, ignoreAir);
        
        // Execute the operation
        plugin.getOperationManager().execute(player, operation);
    }
    
    /**
     * Paste a player's clipboard at their current position using batch processing for large pastes.
     * This will split the operation into multiple batches to minimize server impact.
     * 
     * @param player The player
     * @param ignoreAir Whether to ignore air blocks
     * @param batchSize The number of blocks to process in each batch
     * @param tickDelay The number of server ticks to wait between batches
     */
    public void batchPaste(Player player, boolean ignoreAir, int batchSize, int tickDelay) {
        if (!hasClipboard(player)) {
            player.sendMessage(ChatColor.RED + "You don't have a clipboard.");
            return;
        }
        
        Clipboard clipboard = getClipboard(player);
        Vector3 position = Vector3.fromLocation(player.getLocation());
        
        // Create a batch paste operation
        BatchPasteOperation operation = new BatchPasteOperation(
            plugin, player, position, clipboard, ignoreAir, batchSize, tickDelay, "Batch Clipboard Paste"
        );
        
        // Execute the operation
        plugin.getOperationManager().execute(player, operation);
    }
    
    /**
     * Rotate a player's clipboard.
     * @param player The player
     * @param degrees The degrees to rotate
     */
    public void rotate(Player player, int degrees) {
        if (!hasClipboard(player)) {
            player.sendMessage(ChatColor.RED + "You don't have a clipboard.");
            return;
        }
        
        Clipboard clipboard = getClipboard(player);
        
        try {
            clipboard.rotate(degrees);
            player.sendMessage(ChatColor.GREEN + "Clipboard rotated by " + degrees + " degrees.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
    
    /**
     * Flip a player's clipboard.
     * @param player The player
     * @param direction The direction to flip (x, y, or z)
     */
    public void flip(Player player, char direction) {
        if (!hasClipboard(player)) {
            player.sendMessage(ChatColor.RED + "You don't have a clipboard.");
            return;
        }
        
        Clipboard clipboard = getClipboard(player);
        
        try {
            clipboard.flip(direction);
            player.sendMessage(ChatColor.GREEN + "Clipboard flipped along the " + direction + " axis.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
} 