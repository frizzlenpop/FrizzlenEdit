package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.List;

/**
 * Handles schematic-related commands.
 */
public class SchematicCommands {
    
    /**
     * Command handler for the schematic save command.
     */
    public static class SaveCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public SaveCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.schematic.save")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic save <name>");
                return true;
            }
            
            String name = args[0];
            
            // Check if the player has a selection
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            // Get the selection
            Region region = plugin.getSelectionManager().getSelection(player);
            
            // Save the schematic
            plugin.getSchematicManager().saveSchematic(player, region, name);
            return true;
        }
    }
    
    /**
     * Command handler for the schematic load command.
     */
    public static class LoadCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public LoadCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.schematic.load")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic load <name>");
                return true;
            }
            
            String name = args[0];
            
            // Load the schematic
            plugin.getSchematicManager().loadSchematic(player, name);
            return true;
        }
    }
    
    /**
     * Command handler for the schematic delete command.
     */
    public static class DeleteCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public DeleteCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.schematic.delete")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic delete <name>");
                return true;
            }
            
            String name = args[0];
            
            // Delete the schematic
            plugin.getSchematicManager().deleteSchematic(player, name);
            return true;
        }
    }
    
    /**
     * Command handler for the schematic list command.
     */
    public static class ListCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public ListCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.schematic.list")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // List the schematics
            plugin.getSchematicManager().listSchematics(player);
            return true;
        }
    }
    
    /**
     * Command handler for the schematic formats command.
     */
    public static class FormatsCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public FormatsCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.schematic.formats")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // List the supported formats
            player.sendMessage(ChatColor.GREEN + "Supported schematic formats: .schem");
            return true;
        }
    }
    
    /**
     * Command handler for the paste command for schematics.
     * This command pastes a schematic directly at the player's location.
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
            
            if (!player.hasPermission("frizzlenedit.schematic.paste")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic paste <name> [noair]");
                return true;
            }
            
            String name = args[0];
            boolean ignoreAir = false;
            
            // Check for noair flag
            if (args.length > 1 && args[1].equalsIgnoreCase("noair")) {
                ignoreAir = true;
            }
            
            Vector3 position = Vector3.fromLocation(player.getLocation());
            plugin.getSchematicManager().pasteSchematic(player, name, position, ignoreAir);
            return true;
        }
    }
    
    /**
     * Command handler for the pastelarge command for schematics.
     * This command pastes schematics in batches to minimize server impact.
     * Now uses adaptive performance logic by default.
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
            
            if (!player.hasPermission("frizzlenedit.schematic.pastelarge")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic pastelarge <n> [noair] [batch <size>] [delay <ticks>] [noadaptive]");
                return true;
            }
            
            String name = args[0];
            boolean ignoreAir = false;
            int batchSize = plugin.getConfigManager().getBatchPasteSize(); // Default from config
            int tickDelay = plugin.getConfigManager().getBatchPasteDelay(); // Default from config
            boolean useAdaptive = true; // Use adaptive logic by default
            
            // Parse additional arguments
            for (int i = 1; i < args.length; i++) {
                String arg = args[i].toLowerCase();
                
                if (arg.equals("noair")) {
                    ignoreAir = true;
                } else if (arg.equals("batch") && i + 1 < args.length) {
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
                } else if (arg.equals("delay") && i + 1 < args.length) {
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
                } else if (arg.equals("noadaptive")) {
                    useAdaptive = false;
                }
            }
            
            Vector3 position = Vector3.fromLocation(player.getLocation());
            player.sendMessage(ChatColor.YELLOW + "Starting large schematic paste operation...");
            
            // Use adaptive paste method if enabled, otherwise use the fixed batch paste
            if (useAdaptive) {
                player.sendMessage(ChatColor.YELLOW + "Using adaptive performance optimization with initial batch size: " + 
                                 batchSize + ", delay: " + tickDelay);
                plugin.getSchematicManager().adaptivePasteSchematic(player, name, position, ignoreAir, batchSize, tickDelay);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Using fixed batch settings: size: " + 
                                 batchSize + ", delay: " + tickDelay);
                plugin.getSchematicManager().batchPasteSchematic(player, name, position, ignoreAir, batchSize, tickDelay);
            }
            
            return true;
        }
    }
    
    /**
     * Command to paste a schematic with adaptive performance.
     * This command automatically adjusts batch size and delay based on server TPS.
     */
    public static class AdaptivePasteCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public AdaptivePasteCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Check if the sender is a player
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check if the player has permission
            if (!player.hasPermission("frizzlenedit.schematic.adaptivepaste")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check for correct usage
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic adaptivepaste <n> [noair]");
                return true;
            }
            
            // Get the schematic name
            String name = args[0];
            
            // Check if we should ignore air
            boolean ignoreAir = false;
            if (args.length > 1 && args[1].equalsIgnoreCase("noair")) {
                ignoreAir = true;
            }
            
            // Get player position
            Vector3 position = Vector3.fromLocation(player.getLocation());
            
            // Adaptively paste the schematic
            plugin.getSchematicManager().adaptivePasteSchematic(player, name, position, ignoreAir);
            
            player.sendMessage(ChatColor.GREEN + "Adaptively pasting schematic " + name + " at your location.");
            player.sendMessage(ChatColor.YELLOW + "The paste operation will automatically adjust speed based on server performance.");
            
            return true;
        }
    }
} 