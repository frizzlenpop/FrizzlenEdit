package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;

/**
 * Handles the main brush command and delegates to appropriate subcommands.
 */
public class BrushCommandHandler implements CommandExecutor {
    private final FrizzlenEdit plugin;
    private final BrushCommands.SphereBrushCommand sphereBrushCommand;
    private final BrushCommands.CylinderBrushCommand cylinderBrushCommand;
    private final BrushCommands.SmoothBrushCommand smoothBrushCommand;
    private final BrushCommands.NoneBrushCommand noneBrushCommand;
    
    /**
     * Create a new brush command handler.
     * @param plugin The plugin instance
     * @param sphereBrushCommand The sphere brush command
     * @param cylinderBrushCommand The cylinder brush command
     * @param smoothBrushCommand The smooth brush command
     * @param noneBrushCommand The none brush command
     */
    public BrushCommandHandler(FrizzlenEdit plugin, 
                              BrushCommands.SphereBrushCommand sphereBrushCommand,
                              BrushCommands.CylinderBrushCommand cylinderBrushCommand,
                              BrushCommands.SmoothBrushCommand smoothBrushCommand,
                              BrushCommands.NoneBrushCommand noneBrushCommand) {
        this.plugin = plugin;
        this.sphereBrushCommand = sphereBrushCommand;
        this.cylinderBrushCommand = cylinderBrushCommand;
        this.smoothBrushCommand = smoothBrushCommand;
        this.noneBrushCommand = noneBrushCommand;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("frizzlenedit.brush")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 1) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        
        switch (subCommand) {
            case "sphere":
                return sphereBrushCommand.onCommand(sender, command, label, subArgs);
            case "cylinder":
                return cylinderBrushCommand.onCommand(sender, command, label, subArgs);
            case "smooth":
                return smoothBrushCommand.onCommand(sender, command, label, subArgs);
            case "none":
                return noneBrushCommand.onCommand(sender, command, label, subArgs);
            default:
                showHelp(player);
                return true;
        }
    }
    
    /**
     * Show help for the brush command.
     * @param player The player to show help to
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.BLUE + "==== FrizzlenEdit Brush Commands ====");
        player.sendMessage(ChatColor.GRAY + "//brush sphere <material> <radius>" + ChatColor.WHITE + " - Create a sphere brush");
        player.sendMessage(ChatColor.GRAY + "//brush cylinder <material> <radius> <height>" + ChatColor.WHITE + " - Create a cylinder brush");
        player.sendMessage(ChatColor.GRAY + "//brush smooth <radius>" + ChatColor.WHITE + " - Create a smooth brush");
        player.sendMessage(ChatColor.GRAY + "//brush none" + ChatColor.WHITE + " - Remove the current brush");
        player.sendMessage(ChatColor.GRAY + "//mask <material>" + ChatColor.WHITE + " - Set a material mask for the brush");
        player.sendMessage(ChatColor.GRAY + "//mask none" + ChatColor.WHITE + " - Remove the current mask");
    }
} 