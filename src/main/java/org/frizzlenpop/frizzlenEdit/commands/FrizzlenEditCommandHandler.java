package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;

/**
 * Handles the main plugin command.
 */
public class FrizzlenEditCommandHandler implements CommandExecutor {
    private final FrizzlenEdit plugin;
    
    /**
     * Create a new command handler.
     * @param plugin The plugin instance
     */
    public FrizzlenEditCommandHandler(FrizzlenEdit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show help
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
            case "reload":
                if (!sender.hasPermission("frizzlenedit.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                
                plugin.getConfigManager().loadConfig();
                plugin.getCommandPreprocessor().updateCommandPrefix();
                sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                break;
            case "version":
                sender.sendMessage(ChatColor.GREEN + "FrizzlenEdit version " + plugin.getDescription().getVersion());
                break;
            case "prefix":
                if (!sender.hasPermission("frizzlenedit.admin.prefix")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                
                String prefix = plugin.getConfigManager().getCommandPrefix();
                sender.sendMessage(ChatColor.GREEN + "Current command prefix is: " + prefix);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Type /fe help for help.");
                break;
        }
        
        return true;
    }
    
    /**
     * Show help to a command sender.
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== FrizzlenEdit Help ===");
        sender.sendMessage(ChatColor.GRAY + "/fe help" + ChatColor.WHITE + " - Show this help");
        sender.sendMessage(ChatColor.GRAY + "/fe reload" + ChatColor.WHITE + " - Reload the configuration");
        sender.sendMessage(ChatColor.GRAY + "/fe version" + ChatColor.WHITE + " - Show the plugin version");
        sender.sendMessage(ChatColor.GRAY + "/fe prefix" + ChatColor.WHITE + " - Show current command prefix");
        
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.GREEN + "=== Selection Commands ===");
            String prefix = plugin.getConfigManager().getCommandPrefix();
            sender.sendMessage(ChatColor.GRAY + prefix + "wand" + ChatColor.WHITE + " - Get a selection wand");
            sender.sendMessage(ChatColor.GRAY + prefix + "pos1" + ChatColor.WHITE + " - Set position 1 to your current location");
            sender.sendMessage(ChatColor.GRAY + prefix + "pos2" + ChatColor.WHITE + " - Set position 2 to your current location");
            sender.sendMessage(ChatColor.GRAY + prefix + "expand <amount> [direction]" + ChatColor.WHITE + " - Expand the selection");
            sender.sendMessage(ChatColor.GRAY + prefix + "contract <amount> [direction]" + ChatColor.WHITE + " - Contract the selection");
            sender.sendMessage(ChatColor.GRAY + prefix + "size" + ChatColor.WHITE + " - Show the size of the selection");
            
            sender.sendMessage(ChatColor.GREEN + "=== Clipboard Commands ===");
            sender.sendMessage(ChatColor.GRAY + prefix + "copy" + ChatColor.WHITE + " - Copy the selection to clipboard");
            sender.sendMessage(ChatColor.GRAY + prefix + "cut" + ChatColor.WHITE + " - Cut the selection to clipboard");
            sender.sendMessage(ChatColor.GRAY + prefix + "paste" + ChatColor.WHITE + " - Paste from clipboard");
            sender.sendMessage(ChatColor.GRAY + prefix + "flip <direction>" + ChatColor.WHITE + " - Flip the clipboard");
            sender.sendMessage(ChatColor.GRAY + prefix + "rotate <degrees>" + ChatColor.WHITE + " - Rotate the clipboard");
            
            sender.sendMessage(ChatColor.GREEN + "=== History Commands ===");
            sender.sendMessage(ChatColor.GRAY + prefix + "undo" + ChatColor.WHITE + " - Undo the last operation");
            sender.sendMessage(ChatColor.GRAY + prefix + "redo" + ChatColor.WHITE + " - Redo the last undone operation");
            
            sender.sendMessage(ChatColor.GREEN + "=== Block Commands ===");
            sender.sendMessage(ChatColor.GRAY + prefix + "set <block>" + ChatColor.WHITE + " - Set all blocks in the selection");
            sender.sendMessage(ChatColor.GRAY + prefix + "replace <from> <to>" + ChatColor.WHITE + " - Replace blocks in the selection");
            
            sender.sendMessage(ChatColor.GREEN + "=== Brush Commands ===");
            sender.sendMessage(ChatColor.GRAY + prefix + "brush sphere <block> <radius>" + ChatColor.WHITE + " - Create a sphere brush");
            sender.sendMessage(ChatColor.GRAY + prefix + "brush cylinder <block> <radius> [height]" + ChatColor.WHITE + " - Create a cylinder brush");
            sender.sendMessage(ChatColor.GRAY + prefix + "brush smooth <radius>" + ChatColor.WHITE + " - Create a smooth brush");
            sender.sendMessage(ChatColor.GRAY + prefix + "mask <block>" + ChatColor.WHITE + " - Set a brush mask");
            
            sender.sendMessage(ChatColor.GREEN + "=== Schematic Commands ===");
            sender.sendMessage(ChatColor.GRAY + prefix + "schem save <n>" + ChatColor.WHITE + " - Save the selection as a schematic");
            sender.sendMessage(ChatColor.GRAY + prefix + "schem load <n>" + ChatColor.WHITE + " - Load a schematic");
            sender.sendMessage(ChatColor.GRAY + prefix + "schem list" + ChatColor.WHITE + " - List available schematics");
            sender.sendMessage(ChatColor.GRAY + prefix + "schem delete <n>" + ChatColor.WHITE + " - Delete a schematic");
        }
    }
} 