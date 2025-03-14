package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
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
 * Command for creating pyramids.
 */
public class PyramidCommand implements CommandExecutor {
    private final FrizzlenEdit plugin;
    
    public PyramidCommand(FrizzlenEdit plugin) {
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
        if (!player.hasPermission("frizzlenedit.pyramid")) {
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
            
            // Get the size
            int size;
            try {
                size = Integer.parseInt(args[1]);
                if (size <= 0) {
                    player.sendMessage(ChatColor.RED + "Size must be greater than 0.");
                    return true;
                }
                
                // Check maximum size (configurable)
                int maxSize = plugin.getConfigManager().getMaxBrushSize() * 2;
                if (size > maxSize) {
                    player.sendMessage(ChatColor.RED + "Size cannot be greater than " + maxSize + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid size: " + args[1]);
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
            Vector3 base = new Vector3(player.getLocation().getBlockX(),
                                       player.getLocation().getBlockY(),
                                       player.getLocation().getBlockZ());
            
            Operation operation;
            if (mask != null) {
                operation = plugin.getOperationManager().createPyramidOperation(
                        player, base, pattern, size, hollow, mask);
            } else {
                operation = plugin.getOperationManager().createPyramidOperation(
                        player, base, pattern, size, hollow);
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
        player.sendMessage(ChatColor.YELLOW + "Usage: //pyramid <pattern> <size> [hollow] [mask:<mask>]");
        player.sendMessage(ChatColor.YELLOW + "Examples:");
        player.sendMessage(ChatColor.GRAY + "  //pyramid stone 5" + ChatColor.WHITE + " - Creates a solid stone pyramid with size 5");
        player.sendMessage(ChatColor.GRAY + "  //pyramid stone 5 hollow" + ChatColor.WHITE + " - Creates a hollow stone pyramid with size 5");
        player.sendMessage(ChatColor.GRAY + "  //pyramid stone,dirt 5" + ChatColor.WHITE + " - Creates a pyramid with random stone and dirt");
        player.sendMessage(ChatColor.GRAY + "  //pyramid stone%75,dirt%25 5" + ChatColor.WHITE + " - Creates a pyramid with 75% stone and 25% dirt");
        player.sendMessage(ChatColor.GRAY + "  //pyramid noise(5,stone,dirt) 10" + ChatColor.WHITE + " - Creates a pyramid with noise-based pattern");
        player.sendMessage(ChatColor.GRAY + "  //pyramid stone 5 mask:#solid" + ChatColor.WHITE + " - Creates a pyramid that only affects solid blocks");
    }
} 