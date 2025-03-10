package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;

/**
 * Handles the main schematic command and delegates to appropriate subcommands.
 */
public class SchematicCommandHandler implements CommandExecutor {
    private final FrizzlenEdit plugin;
    private final SchematicCommands.SaveCommand saveCommand;
    private final SchematicCommands.LoadCommand loadCommand;
    private final SchematicCommands.DeleteCommand deleteCommand;
    private final SchematicCommands.ListCommand listCommand;
    private final SchematicCommands.FormatsCommand formatsCommand;
    private final SchematicCommands.PasteCommand pasteCommand;
    private final SchematicCommands.PasteLargeCommand pasteLargeCommand;
    private final SchematicCommands.AdaptivePasteCommand adaptivePasteCommand;
    
    /**
     * Create a new schematic command handler.
     * @param plugin The plugin
     * @param saveCommand The save command
     * @param loadCommand The load command
     * @param deleteCommand The delete command
     * @param listCommand The list command
     * @param formatsCommand The formats command
     * @param pasteCommand The paste command
     * @param pasteLargeCommand The pastelarge command
     * @param adaptivePasteCommand The adaptivepaste command
     */
    public SchematicCommandHandler(FrizzlenEdit plugin, 
                                  SchematicCommands.SaveCommand saveCommand,
                                  SchematicCommands.LoadCommand loadCommand,
                                  SchematicCommands.DeleteCommand deleteCommand,
                                  SchematicCommands.ListCommand listCommand,
                                  SchematicCommands.FormatsCommand formatsCommand,
                                  SchematicCommands.PasteCommand pasteCommand,
                                  SchematicCommands.PasteLargeCommand pasteLargeCommand,
                                  SchematicCommands.AdaptivePasteCommand adaptivePasteCommand) {
        this.plugin = plugin;
        this.saveCommand = saveCommand;
        this.loadCommand = loadCommand;
        this.deleteCommand = deleteCommand;
        this.listCommand = listCommand;
        this.formatsCommand = formatsCommand;
        this.pasteCommand = pasteCommand;
        this.pasteLargeCommand = pasteLargeCommand;
        this.adaptivePasteCommand = adaptivePasteCommand;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("frizzlenedit.schematic")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            
            switch (subCommand) {
                case "save":
                    return saveCommand.onCommand(sender, command, label, subArgs);
                case "load":
                    return loadCommand.onCommand(sender, command, label, subArgs);
                case "delete":
                    return deleteCommand.onCommand(sender, command, label, subArgs);
                case "list":
                    return listCommand.onCommand(sender, command, label, subArgs);
                case "formats":
                    return formatsCommand.onCommand(sender, command, label, subArgs);
                case "paste":
                    return pasteCommand.onCommand(sender, command, label, subArgs);
                case "pastelarge":
                    return pasteLargeCommand.onCommand(sender, command, label, subArgs);
                case "adaptivepaste":
                    return adaptivePasteCommand.onCommand(sender, command, label, subArgs);
                default:
                    player.sendMessage(ChatColor.RED + "Unknown schematic command: " + subCommand);
                    showHelp(player);
                    return true;
            }
        } else {
            showHelp(player);
        }
        
        return true;
    }
    
    /**
     * Show help for the schematic command.
     * @param player The player to show help to
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== Schematic Commands ===");
        player.sendMessage(ChatColor.GOLD + "/schematic save <name>" + ChatColor.WHITE + " - Save your selection as a schematic");
        player.sendMessage(ChatColor.GOLD + "/schematic load <name>" + ChatColor.WHITE + " - Load a schematic to your clipboard");
        player.sendMessage(ChatColor.GOLD + "/schematic delete <name>" + ChatColor.WHITE + " - Delete a schematic");
        player.sendMessage(ChatColor.GOLD + "/schematic list" + ChatColor.WHITE + " - List available schematics");
        player.sendMessage(ChatColor.GOLD + "/schematic formats" + ChatColor.WHITE + " - List supported schematic formats");
        player.sendMessage(ChatColor.GOLD + "/schematic paste <name> [noair]" + ChatColor.WHITE + " - Paste a schematic at your location");
        player.sendMessage(ChatColor.GOLD + "/schematic pastelarge <name> [noair] [batch <size>] [delay <ticks>] [noadaptive]" + 
                            ChatColor.WHITE + " - Paste a large schematic with optimized performance");
        player.sendMessage(ChatColor.GOLD + "/schematic adaptivepaste <name> [noair]" + 
                            ChatColor.WHITE + " - Paste with automatic performance optimization");
    }
} 