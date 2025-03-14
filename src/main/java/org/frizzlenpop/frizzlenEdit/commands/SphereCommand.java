package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.operations.Operation;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.patterns.PatternFactory;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.masks.MaskFactory;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Command for creating spheres.
 */
public class SphereCommand implements CommandExecutor {
    private final FrizzlenEdit plugin;
    
    public SphereCommand(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("frizzlenedit.sphere")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Check arguments
        if (args.length < 2) {
            showUsage(player);
            return true;
        }
        
        try {
            // Get the pattern or material
            String patternStr = args[0];
            Pattern pattern;
            
            try {
                // Try to parse as a pattern
                pattern = PatternFactory.parsePattern(player, patternStr);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
                return true;
            }
            
            // Get the radius
            int radius;
            try {
                radius = Integer.parseInt(args[1]);
                if (radius <= 0) {
                    player.sendMessage(ChatColor.RED + "Radius must be greater than 0.");
                    return true;
                }
                
                // Check maximum radius (configurable)
                int maxRadius = plugin.getConfigManager().getMaxBrushSize();
                if (radius > maxRadius) {
                    player.sendMessage(ChatColor.RED + "Radius cannot be greater than " + maxRadius + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid radius: " + args[1]);
                return true;
            }
            
            // Parse optional arguments
            boolean hollow = false;
            Mask mask = null;
            
            for (int i = 2; i < args.length; i++) {
                String arg = args[i].toLowerCase();
                
                if (arg.equals("hollow")) {
                    hollow = true;
                } else if (arg.startsWith("mask:")) {
                    // Extract the mask string after "mask:"
                    String maskStr = arg.substring(5);
                    try {
                        mask = MaskFactory.parseMask(player, maskStr);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                        return true;
                    }
                }
            }
            
            // Create the operation
            Vector3 center = new Vector3(player.getLocation().getBlockX(),
                                         player.getLocation().getBlockY(),
                                         player.getLocation().getBlockZ());
            
            Operation operation;
            if (mask != null) {
                operation = plugin.getOperationManager().createSphereOperation(
                        player, center, pattern, radius, hollow, mask);
            } else {
                operation = plugin.getOperationManager().createSphereOperation(
                        player, center, pattern, radius, hollow);
            }
            
            // Execute the operation
            plugin.getOperationManager().execute(player, operation);
            
            return true;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    private void showUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Usage: //sphere <pattern> <radius> [hollow] [mask:<mask>]");
        player.sendMessage(ChatColor.YELLOW + "Examples:");
        player.sendMessage(ChatColor.GRAY + "  //sphere stone 5" + ChatColor.WHITE + " - Creates a solid stone sphere with radius 5");
        player.sendMessage(ChatColor.GRAY + "  //sphere stone 5 hollow" + ChatColor.WHITE + " - Creates a hollow stone sphere with radius 5");
        player.sendMessage(ChatColor.GRAY + "  //sphere stone,dirt 5" + ChatColor.WHITE + " - Creates a sphere with random stone and dirt");
        player.sendMessage(ChatColor.GRAY + "  //sphere stone%75,dirt%25 5" + ChatColor.WHITE + " - Creates a sphere with 75% stone and 25% dirt");
        player.sendMessage(ChatColor.GRAY + "  //sphere noise(5,stone,dirt) 10" + ChatColor.WHITE + " - Creates a sphere with noise-based pattern");
        player.sendMessage(ChatColor.GRAY + "  //sphere stone 5 mask:#solid" + ChatColor.WHITE + " - Creates a sphere that only affects solid blocks");
    }
} 