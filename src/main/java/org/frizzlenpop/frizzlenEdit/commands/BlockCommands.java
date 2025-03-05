package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.operations.Operation;
import org.frizzlenpop.frizzlenEdit.selection.Region;

/**
 * Handles block manipulation commands.
 */
public class BlockCommands {
    
    /**
     * Command handler for the set command.
     */
    public static class SetCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public SetCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.block.set")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //set <block>");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            Region region = plugin.getSelectionManager().getSelection(player);
            String blockType = args[0];
            
            // Create and execute the operation
            try {
                Operation operation = plugin.getOperationManager().createSetOperation(player, region, blockType);
                plugin.getOperationManager().execute(player, operation);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid block type: " + blockType);
                
                // Suggest some common material names
                player.sendMessage(ChatColor.YELLOW + "Common material names: stone, dirt, grass_block, oak_planks, glass, etc.");
                player.sendMessage(ChatColor.YELLOW + "Use vanilla Minecraft material names without spaces or underscores.");
            }
            
            return true;
        }
    }
    
    /**
     * Command handler for the replace command.
     */
    public static class ReplaceCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public ReplaceCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.block.replace")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: //replace <from> <to>");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            Region region = plugin.getSelectionManager().getSelection(player);
            String fromType = args[0];
            String toType = args[1];
            
            // Create and execute the operation
            try {
                Operation operation = plugin.getOperationManager().createReplaceOperation(player, region, fromType, toType);
                plugin.getOperationManager().execute(player, operation);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
                
                // Suggest some common material names
                player.sendMessage(ChatColor.YELLOW + "Common material names: stone, dirt, grass_block, oak_planks, glass, etc.");
                player.sendMessage(ChatColor.YELLOW + "Do not use spaces or underscores. For example, use 'stone' not 'stone_block'.");
            }
            
            return true;
        }
    }
    
    /**
     * Command handler for the undo command.
     */
    public static class UndoCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public UndoCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.history.undo")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Parse the number of operations to undo
            int count = 1;
            if (args.length > 0) {
                try {
                    count = Integer.parseInt(args[0]);
                    if (count < 1) {
                        player.sendMessage(ChatColor.RED + "Count must be at least 1.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid count: " + args[0]);
                    return true;
                }
            }
            
            // Undo the operations
            for (int i = 0; i < count; i++) {
                if (!plugin.getHistoryManager().undo(player)) {
                    if (i == 0) {
                        player.sendMessage(ChatColor.RED + "Nothing to undo.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Undid " + i + " operations.");
                    }
                    break;
                }
            }
            
            return true;
        }
    }
    
    /**
     * Command handler for the redo command.
     */
    public static class RedoCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public RedoCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.history.redo")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Parse the number of operations to redo
            int count = 1;
            if (args.length > 0) {
                try {
                    count = Integer.parseInt(args[0]);
                    if (count < 1) {
                        player.sendMessage(ChatColor.RED + "Count must be at least 1.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid count: " + args[0]);
                    return true;
                }
            }
            
            // Redo the operations
            for (int i = 0; i < count; i++) {
                if (!plugin.getHistoryManager().redo(player)) {
                    if (i == 0) {
                        player.sendMessage(ChatColor.RED + "Nothing to redo.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Redid " + i + " operations.");
                    }
                    break;
                }
            }
            
            return true;
        }
    }
    
    /**
     * Command handler for the clearhistory command.
     */
    public static class ClearHistoryCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public ClearHistoryCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.history.clear")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            plugin.getHistoryManager().clearHistory(player);
            player.sendMessage(ChatColor.GREEN + "History cleared.");
            return true;
        }
    }
} 