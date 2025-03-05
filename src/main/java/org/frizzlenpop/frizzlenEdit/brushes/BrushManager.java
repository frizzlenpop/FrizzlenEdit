package org.frizzlenpop.frizzlenEdit.brushes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
 * Manages player brushes.
 */
public class BrushManager implements Listener {
    private final FrizzlenEdit plugin;
    private final Map<UUID, Brush> brushes = new HashMap<>();
    private final Map<UUID, String> masks = new HashMap<>();
    
    // The brush tool material
    private static final Material BRUSH_MATERIAL = Material.BLAZE_ROD;
    
    /**
     * Create a new brush manager.
     * @param plugin The plugin instance
     */
    public BrushManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if a player has a brush.
     * @param player The player
     * @return True if the player has a brush
     */
    public boolean hasBrush(Player player) {
        return brushes.containsKey(player.getUniqueId());
    }
    
    /**
     * Get a player's brush.
     * @param player The player
     * @return The player's brush, or null if they don't have one
     */
    public Brush getBrush(Player player) {
        return brushes.get(player.getUniqueId());
    }
    
    /**
     * Set a player's brush.
     * @param player The player
     * @param brush The brush
     */
    public void setBrush(Player player, Brush brush) {
        brushes.put(player.getUniqueId(), brush);
    }
    
    /**
     * Remove a player's brush.
     * @param player The player
     */
    public void removeBrush(Player player) {
        brushes.remove(player.getUniqueId());
    }
    
    /**
     * Set a player's brush mask.
     * @param player The player
     * @param mask The mask
     */
    public void setMask(Player player, String mask) {
        masks.put(player.getUniqueId(), mask);
    }
    
    /**
     * Get a player's brush mask.
     * @param player The player
     * @return The player's mask, or null if they don't have one
     */
    public String getMask(Player player) {
        return masks.get(player.getUniqueId());
    }
    
    /**
     * Remove a player's brush mask.
     * @param player The player
     */
    public void removeMask(Player player) {
        masks.remove(player.getUniqueId());
    }
    
    /**
     * Create a sphere brush for a player.
     * @param player The player
     * @param material The material
     * @param radius The radius
     */
    public void createSphereBrush(Player player, String material, int radius) {
        // Check if the radius is too large
        int maxRadius = plugin.getConfigManager().getMaxBrushSize();
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Brush radius too large: " + radius + ". Maximum is " + maxRadius + ".");
            return;
        }
        
        // Create the brush
        SphereBrush brush = new SphereBrush(plugin, material, radius);
        setBrush(player, brush);
        
        player.sendMessage(ChatColor.GREEN + "Sphere brush created with material " + material + " and radius " + radius + ".");
    }
    
    /**
     * Create a cylinder brush for a player.
     * @param player The player
     * @param material The material
     * @param radius The radius
     * @param height The height
     */
    public void createCylinderBrush(Player player, String material, int radius, int height) {
        // Check if the radius is too large
        int maxRadius = plugin.getConfigManager().getMaxBrushSize();
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Brush radius too large: " + radius + ". Maximum is " + maxRadius + ".");
            return;
        }
        
        // Create the brush
        CylinderBrush brush = new CylinderBrush(plugin, material, radius, height);
        setBrush(player, brush);
        
        player.sendMessage(ChatColor.GREEN + "Cylinder brush created with material " + material + ", radius " + radius + ", and height " + height + ".");
    }
    
    /**
     * Create a smooth brush for a player.
     * @param player The player
     * @param radius The radius
     */
    public void createSmoothBrush(Player player, int radius) {
        // Check if the radius is too large
        int maxRadius = plugin.getConfigManager().getMaxBrushSize();
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Brush radius too large: " + radius + ". Maximum is " + maxRadius + ".");
            return;
        }
        
        // Create the brush
        SmoothBrush brush = new SmoothBrush(plugin, radius);
        setBrush(player, brush);
        
        player.sendMessage(ChatColor.GREEN + "Smooth brush created with radius " + radius + ".");
    }
    
    /**
     * Give a brush tool to a player.
     * @param player The player
     */
    public void giveBrushTool(Player player) {
        ItemStack tool = new ItemStack(BRUSH_MATERIAL);
        player.getInventory().addItem(tool);
        player.sendMessage(ChatColor.GREEN + "Brush tool given. " + 
                          ChatColor.GRAY + "Right click to use your brush.");
    }
    
    /**
     * Check if an item is a brush tool.
     * @param item The item
     * @return True if the item is a brush tool
     */
    public boolean isBrushTool(ItemStack item) {
        return item != null && item.getType() == BRUSH_MATERIAL;
    }
    
    /**
     * Handle player interactions with the brush tool.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if the player is using a brush tool
        if (!isBrushTool(item)) {
            return;
        }
        
        // Check if the player has a brush
        if (!hasBrush(player)) {
            player.sendMessage(ChatColor.RED + "You don't have a brush. Use //brush to create one.");
            return;
        }
        
        // Check if the player right-clicked a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        // Cancel the event to prevent block interaction
        event.setCancelled(true);
        
        // Get the clicked block position
        Block clickedBlock = event.getClickedBlock();
        Vector3 position = Vector3.fromBlock(clickedBlock);
        
        // Get the player's brush
        Brush brush = getBrush(player);
        
        if (brush == null) {
            player.sendMessage(ChatColor.RED + "Error: Your brush is null. Try setting a brush again with //brush.");
            return;
        }
        
        // Log brush usage for debugging
        plugin.getLogger().info("Player " + player.getName() + " using brush: " + brush.getDescription() + " at position " + position);
        
        // Get the player's mask
        String mask = getMask(player);
        
        // Inform the player that the brush is being used
        player.sendMessage(ChatColor.GREEN + "Using " + brush.getDescription() + " at (" + 
                          position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
        
        // Use the brush
        brush.use(player, position, mask);
    }
} 