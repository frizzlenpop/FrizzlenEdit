package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.operations.Operation;
import org.frizzlenpop.frizzlenEdit.operations.PasteOperation;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Handles clipboard-related commands.
 */
public class ClipboardCommands {
    
    /**
     * Command handler for the copy command.
     */
    public static class CopyCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public CopyCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.clipboard.copy")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            Region region = plugin.getSelectionManager().getSelection(player);
            Vector3 origin = Vector3.fromLocation(player.getLocation());
            plugin.getClipboardManager().copy(player, region, origin);
            player.sendMessage(ChatColor.GREEN + "Copied " + region.getVolume() + " blocks to clipboard.");
            return true;
        }
    }
    
    /**
     * Command handler for the cut command.
     */
    public static class CutCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public CutCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.clipboard.cut")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            Region region = plugin.getSelectionManager().getSelection(player);
            Vector3 origin = Vector3.fromLocation(player.getLocation());
            plugin.getClipboardManager().cut(player, region, origin);
            player.sendMessage(ChatColor.GREEN + "Cut " + region.getVolume() + " blocks to clipboard.");
            return true;
        }
    }
    
    /**
     * Command handler for the paste command.
     */
    public static class PasteCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public PasteCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.clipboard.paste")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            Clipboard clipboard = plugin.getClipboardManager().getClipboard(player);
            if (clipboard == null) {
                player.sendMessage(ChatColor.RED + "Your clipboard is empty.");
                return true;
            }
            
            boolean ignoreAir = false;
            if (args.length > 0 && args[0].equalsIgnoreCase("-a")) {
                ignoreAir = true;
            }
            
            Vector3 position = Vector3.fromLocation(player.getLocation());
            PasteOperation operation = new PasteOperation(player, position, clipboard, ignoreAir);
            plugin.getOperationManager().execute(player, operation);
            return true;
        }
    }
    
    /**
     * Command handler for the rotate command.
     */
    public static class RotateCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public RotateCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.clipboard.rotate")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            Clipboard clipboard = plugin.getClipboardManager().getClipboard(player);
            if (clipboard == null) {
                player.sendMessage(ChatColor.RED + "Your clipboard is empty.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //rotate <angle>");
                return true;
            }
            
            int angle;
            try {
                angle = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid angle: " + args[0]);
                return true;
            }
            
            // Normalize angle to 0, 90, 180, or 270
            angle = (angle % 360 + 360) % 360;
            if (angle % 90 != 0) {
                player.sendMessage(ChatColor.RED + "Angle must be a multiple of 90 degrees.");
                return true;
            }
            
            plugin.getClipboardManager().rotate(player, angle);
            player.sendMessage(ChatColor.GREEN + "Rotated clipboard by " + angle + " degrees.");
            return true;
        }
    }
    
    /**
     * Command handler for the flip command.
     */
    public static class FlipCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public FlipCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.clipboard.flip")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            Clipboard clipboard = plugin.getClipboardManager().getClipboard(player);
            if (clipboard == null) {
                player.sendMessage(ChatColor.RED + "Your clipboard is empty.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //flip <direction>");
                return true;
            }
            
            String dirStr = args[0].toLowerCase();
            if (!dirStr.equals("x") && !dirStr.equals("y") && !dirStr.equals("z")) {
                player.sendMessage(ChatColor.RED + "Invalid direction: " + args[0] + ". Use x, y, or z.");
                return true;
            }
            
            // Convert string to char
            char direction = dirStr.charAt(0);
            plugin.getClipboardManager().flip(player, direction);
            player.sendMessage(ChatColor.GREEN + "Flipped clipboard along the " + direction + " axis.");
            return true;
        }
    }
    
    /**
     * Command handler for the pastelarge command.
     * This command pastes clipboard contents in batches to minimize server impact.
     */
    public static class PasteLargeCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public PasteLargeCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.clipboard.pastelarge")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            Clipboard clipboard = plugin.getClipboardManager().getClipboard(player);
            if (clipboard == null) {
                player.sendMessage(ChatColor.RED + "Your clipboard is empty.");
                return true;
            }
            
            boolean ignoreAir = false;
            int batchSize = plugin.getConfigManager().getBatchPasteSize(); // Default from config
            int tickDelay = plugin.getConfigManager().getBatchPasteDelay(); // Default from config
            
            // Parse arguments
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].toLowerCase();
                
                if (arg.equals("-a") || arg.equals("noair")) {
                    ignoreAir = true;
                } else if (arg.equals("-b") || arg.equals("batch") && i + 1 < args.length) {
                    try {
                        batchSize = Integer.parseInt(args[++i]);
                        if (batchSize <= 0) {
                            player.sendMessage(ChatColor.RED + "Batch size must be positive.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid batch size: " + args[i]);
                        return true;
                    }
                } else if (arg.equals("-d") || arg.equals("delay") && i + 1 < args.length) {
                    try {
                        tickDelay = Integer.parseInt(args[++i]);
                        if (tickDelay < 0) {
                            player.sendMessage(ChatColor.RED + "Delay must be non-negative.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid delay: " + args[i]);
                        return true;
                    }
                }
            }
            
            player.sendMessage(ChatColor.YELLOW + "Starting large paste operation...");
            plugin.getClipboardManager().batchPaste(player, ignoreAir, batchSize, tickDelay);
            return true;
        }
    }
} 