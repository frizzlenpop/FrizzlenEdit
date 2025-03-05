package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Handles selection-related commands.
 */
public class SelectionCommands {
    
    /**
     * Command handler for the wand command.
     */
    public static class WandCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public WandCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.selection.wand")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            plugin.getSelectionManager().giveSelectionWand(player);
            return true;
        }
    }
    
    /**
     * Command handler for the pos1 command.
     */
    public static class Pos1Command implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public Pos1Command(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.selection.pos")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            Vector3 position = Vector3.fromLocation(player.getLocation());
            plugin.getSelectionManager().setPosition1(player, position);
            return true;
        }
    }
    
    /**
     * Command handler for the pos2 command.
     */
    public static class Pos2Command implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public Pos2Command(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.selection.pos")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            Vector3 position = Vector3.fromLocation(player.getLocation());
            plugin.getSelectionManager().setPosition2(player, position);
            return true;
        }
    }
    
    /**
     * Command handler for the expand command.
     */
    public static class ExpandCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public ExpandCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.selection.expand")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //expand <amount> [direction]");
                return true;
            }
            
            // Parse the amount
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid amount: " + args[0]);
                return true;
            }
            
            // Parse the direction
            Vector3 direction;
            if (args.length > 1) {
                direction = plugin.getSelectionManager().parseDirection(args[1]);
                if (direction == null) {
                    player.sendMessage(ChatColor.RED + "Invalid direction: " + args[1]);
                    return true;
                }
            } else {
                // Use the player's facing direction
                org.bukkit.util.Vector bukkitVector = player.getLocation().getDirection();
                // Convert to unit vector with integer components
                int x = bukkitVector.getX() > 0 ? 1 : (bukkitVector.getX() < 0 ? -1 : 0);
                int y = bukkitVector.getY() > 0 ? 1 : (bukkitVector.getY() < 0 ? -1 : 0);
                int z = bukkitVector.getZ() > 0 ? 1 : (bukkitVector.getZ() < 0 ? -1 : 0);
                direction = new Vector3(x, y, z);
            }
            
            // Expand the selection
            plugin.getSelectionManager().expandSelection(player, direction, amount);
            return true;
        }
    }
    
    /**
     * Command handler for the contract command.
     */
    public static class ContractCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public ContractCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.selection.contract")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //contract <amount> [direction]");
                return true;
            }
            
            // Parse the amount
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid amount: " + args[0]);
                return true;
            }
            
            // Parse the direction
            Vector3 direction;
            if (args.length > 1) {
                direction = plugin.getSelectionManager().parseDirection(args[1]);
                if (direction == null) {
                    player.sendMessage(ChatColor.RED + "Invalid direction: " + args[1]);
                    return true;
                }
            } else {
                // Use the player's facing direction
                org.bukkit.util.Vector bukkitVector = player.getLocation().getDirection();
                // Convert to unit vector with integer components
                int x = bukkitVector.getX() > 0 ? 1 : (bukkitVector.getX() < 0 ? -1 : 0);
                int y = bukkitVector.getY() > 0 ? 1 : (bukkitVector.getY() < 0 ? -1 : 0);
                int z = bukkitVector.getZ() > 0 ? 1 : (bukkitVector.getZ() < 0 ? -1 : 0);
                direction = new Vector3(x, y, z);
            }
            
            // Contract the selection
            plugin.getSelectionManager().contractSelection(player, direction, amount);
            return true;
        }
    }
    
    /**
     * Command handler for the size command.
     */
    public static class SizeCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public SizeCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.selection.info")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            Region region = plugin.getSelectionManager().getSelection(player);
            plugin.getSelectionManager().sendSelectionInfo(player, region);
            return true;
        }
    }
} 