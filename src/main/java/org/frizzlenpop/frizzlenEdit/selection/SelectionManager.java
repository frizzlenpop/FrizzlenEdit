package org.frizzlenpop.frizzlenEdit.selection;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player selections and selection wands.
 */
public class SelectionManager implements Listener {
    private final FrizzlenEdit plugin;
    private final Map<UUID, Region> selections = new HashMap<>();
    
    // The selection wand material
    private static final Material WAND_MATERIAL = Material.WOODEN_AXE;
    
    public SelectionManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if a player has a selection.
     * @param player The player
     * @return True if the player has a selection
     */
    public boolean hasSelection(Player player) {
        Region region = selections.get(player.getUniqueId());
        return region != null && region.getPos1() != null && region.getPos2() != null;
    }
    
    /**
     * Get a player's selection.
     * @param player The player
     * @return The player's selection, or null if they don't have one
     */
    public Region getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }
    
    /**
     * Create a new selection for a player.
     * @param player The player
     * @param world The world for the selection
     * @return The new selection
     */
    public Region createSelection(Player player, World world) {
        // Create a new region with null positions
        Region region = new Region(world, null, null);
        
        // Store the selection
        selections.put(player.getUniqueId(), region);
        
        return region;
    }
    
    /**
     * Set position 1 of a player's selection.
     * @param player The player
     * @param position The position
     */
    public void setPosition1(Player player, Vector3 position) {
        Region region = selections.get(player.getUniqueId());
        
        if (region == null || region.getWorld() != player.getWorld()) {
            region = createSelection(player, player.getWorld());
        }
        
        region.setPos1(position);
        
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Position 1 set to (" + 
                           position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
        
        // If both positions are set, show the selection size
        if (region.getPos1() != null && region.getPos2() != null) {
            sendSelectionInfo(player, region);
        }
    }
    
    /**
     * Set position 2 of a player's selection.
     * @param player The player
     * @param position The position
     */
    public void setPosition2(Player player, Vector3 position) {
        Region region = selections.get(player.getUniqueId());
        
        if (region == null || region.getWorld() != player.getWorld()) {
            region = createSelection(player, player.getWorld());
        }
        
        region.setPos2(position);
        
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Position 2 set to (" + 
                           position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
        
        // If both positions are set, show the selection size
        if (region.getPos1() != null && region.getPos2() != null) {
            sendSelectionInfo(player, region);
        }
    }
    
    /**
     * Send information about a selection to a player.
     * @param player The player
     * @param region The region
     */
    public void sendSelectionInfo(Player player, Region region) {
        if (region.getPos1() != null && region.getPos2() != null) {
            player.sendMessage(ChatColor.BLUE + "Selection: " + 
                              region.getWidth() + "x" + region.getHeight() + "x" + region.getLength() + 
                              " (" + region.getVolume() + " blocks)");
        }
    }
    
    /**
     * Give a selection wand to a player.
     * @param player The player
     */
    public void giveSelectionWand(Player player) {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "Selection wand given. " + 
                          ChatColor.GRAY + "Left click to set position 1, right click to set position 2.");
    }
    
    /**
     * Check if an item is a selection wand.
     * @param item The item
     * @return True if the item is a selection wand
     */
    public boolean isSelectionWand(ItemStack item) {
        return item != null && item.getType() == WAND_MATERIAL;
    }
    
    /**
     * Handle player interactions with the selection wand.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if the player is using a selection wand
        if (!isSelectionWand(item)) {
            return;
        }
        
        // Check if the player clicked a block
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        // Cancel the event to prevent breaking/placing blocks
        event.setCancelled(true);
        
        // Get the clicked block position
        Vector3 position = Vector3.fromBlock(event.getClickedBlock());
        
        // Set the appropriate position
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            setPosition1(player, position);
        } else {
            setPosition2(player, position);
        }
    }
    
    /**
     * Expand a player's selection in a direction.
     * @param player The player
     * @param direction The direction
     * @param amount The amount to expand
     * @return True if the expansion was successful
     */
    public boolean expandSelection(Player player, Vector3 direction, int amount) {
        if (!hasSelection(player)) {
            player.sendMessage(ChatColor.RED + "You must make a selection first.");
            return false;
        }
        
        Region region = getSelection(player);
        region.expand(direction, amount);
        
        player.sendMessage(ChatColor.GREEN + "Selection expanded in direction " + 
                           formatDirection(direction) + " by " + amount + " blocks.");
        sendSelectionInfo(player, region);
        
        return true;
    }
    
    /**
     * Contract a player's selection in a direction.
     * @param player The player
     * @param direction The direction
     * @param amount The amount to contract
     * @return True if the contraction was successful
     */
    public boolean contractSelection(Player player, Vector3 direction, int amount) {
        if (!hasSelection(player)) {
            player.sendMessage(ChatColor.RED + "You must make a selection first.");
            return false;
        }
        
        Region region = getSelection(player);
        region.contract(direction, amount);
        
        player.sendMessage(ChatColor.GREEN + "Selection contracted in direction " + 
                           formatDirection(direction) + " by " + amount + " blocks.");
        sendSelectionInfo(player, region);
        
        return true;
    }
    
    /**
     * Format a direction vector for display.
     * @param direction The direction
     * @return The formatted direction
     */
    private String formatDirection(Vector3 direction) {
        if (direction.getX() > 0) return "east";
        if (direction.getX() < 0) return "west";
        if (direction.getY() > 0) return "up";
        if (direction.getY() < 0) return "down";
        if (direction.getZ() > 0) return "south";
        if (direction.getZ() < 0) return "north";
        return "unknown";
    }
    
    /**
     * Parse a direction string into a vector.
     * @param directionStr The direction string
     * @return The direction vector
     */
    public Vector3 parseDirection(String directionStr) {
        switch (directionStr.toLowerCase()) {
            case "n":
            case "north":
                return new Vector3(0, 0, -1);
            case "s":
            case "south":
                return new Vector3(0, 0, 1);
            case "e":
            case "east":
                return new Vector3(1, 0, 0);
            case "w":
            case "west":
                return new Vector3(-1, 0, 0);
            case "u":
            case "up":
                return new Vector3(0, 1, 0);
            case "d":
            case "down":
                return new Vector3(0, -1, 0);
            default:
                return null;
        }
    }
} 