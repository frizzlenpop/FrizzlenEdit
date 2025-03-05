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
                sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                break;
            case "version":
                sender.sendMessage(ChatColor.GREEN + "FrizzlenEdit version " + plugin.getDescription().getVersion());
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
        
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.GREEN + "=== Selection Commands ===");
            sender.sendMessage(ChatColor.GRAY + "//wand" + ChatColor.WHITE + " - Get a selection wand");
            sender.sendMessage(ChatColor.GRAY + "//pos1" + ChatColor.WHITE + " - Set position 1 to your current location");
            sender.sendMessage(ChatColor.GRAY + "//pos2" + ChatColor.WHITE + " - Set position 2 to your current location");
            sender.sendMessage(ChatColor.GRAY + "//expand <amount> [direction]" + ChatColor.WHITE + " - Expand the selection");
            sender.sendMessage(ChatColor.GRAY + "//contract <amount> [direction]" + ChatColor.WHITE + " - Contract the selection");
            sender.sendMessage(ChatColor.GRAY + "//size" + ChatColor.WHITE + " - Show the size of the selection");
            
            sender.sendMessage(ChatColor.GREEN + "=== Clipboard Commands ===");
            sender.sendMessage(ChatColor.GRAY + "//copy" + ChatColor.WHITE + " - Copy the selection to clipboard");
            sender.sendMessage(ChatColor.GRAY + "//cut" + ChatColor.WHITE + " - Cut the selection to clipboard");
            sender.sendMessage(ChatColor.GRAY + "//paste" + ChatColor.WHITE + " - Paste from clipboard");
            sender.sendMessage(ChatColor.GRAY + "//flip <direction>" + ChatColor.WHITE + " - Flip the clipboard");
            sender.sendMessage(ChatColor.GRAY + "//rotate <degrees>" + ChatColor.WHITE + " - Rotate the clipboard");
            
            sender.sendMessage(ChatColor.GREEN + "=== History Commands ===");
            sender.sendMessage(ChatColor.GRAY + "//undo" + ChatColor.WHITE + " - Undo the last operation");
            sender.sendMessage(ChatColor.GRAY + "//redo" + ChatColor.WHITE + " - Redo the last undone operation");
            
            sender.sendMessage(ChatColor.GREEN + "=== Block Commands ===");
            sender.sendMessage(ChatColor.GRAY + "//set <block>" + ChatColor.WHITE + " - Set all blocks in the selection");
            sender.sendMessage(ChatColor.GRAY + "//replace <from> <to>" + ChatColor.WHITE + " - Replace blocks in the selection");
            
            sender.sendMessage(ChatColor.GREEN + "=== Brush Commands ===");
            sender.sendMessage(ChatColor.GRAY + "//brush sphere <block> <radius>" + ChatColor.WHITE + " - Create a sphere brush");
            sender.sendMessage(ChatColor.GRAY + "//brush cylinder <block> <radius> [height]" + ChatColor.WHITE + " - Create a cylinder brush");
            sender.sendMessage(ChatColor.GRAY + "//brush smooth <radius>" + ChatColor.WHITE + " - Create a smooth brush");
            sender.sendMessage(ChatColor.GRAY + "//mask <block>" + ChatColor.WHITE + " - Set a brush mask");
            
            sender.sendMessage(ChatColor.GREEN + "=== Schematic Commands ===");
            sender.sendMessage(ChatColor.GRAY + "//schem save <name>" + ChatColor.WHITE + " - Save the selection as a schematic");
            sender.sendMessage(ChatColor.GRAY + "//schem load <name>" + ChatColor.WHITE + " - Load a schematic");
            sender.sendMessage(ChatColor.GRAY + "//schem list" + ChatColor.WHITE + " - List available schematics");
            sender.sendMessage(ChatColor.GRAY + "//schem delete <name>" + ChatColor.WHITE + " - Delete a schematic");
        }
    }
} 