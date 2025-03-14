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
                player.sendMessage(ChatColor.RED + "Usage: //schematic pastelarge <name> [noair] [batch <size>] [delay <ticks>] [noadaptive]");
                return true;
            }
            
            String name = args[0];
            boolean noAir = false;
            int batchSize = plugin.getConfig().getInt("paste.batch-size", 500);
            int delay = plugin.getConfig().getInt("paste.delay", 1);
            boolean useAdaptive = true;
            
            // Parse additional arguments
            for (int i = 1; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("noair")) {
                    noAir = true;
                } else if (args[i].equalsIgnoreCase("batch") && i + 1 < args.length) {
                    try {
                        batchSize = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid batch size: " + args[i + 1]);
                        return true;
                    }
                } else if (args[i].equalsIgnoreCase("delay") && i + 1 < args.length) {
                    try {
                        delay = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid delay: " + args[i + 1]);
                        return true;
                    }
                } else if (args[i].equalsIgnoreCase("noadaptive")) {
                    useAdaptive = false;
                }
            }
            
            // Now we'll use the optimized paste system regardless of whether adaptive is enabled
            // The optimization includes chunk-based processing, deferred physics, progressive adaptive sizing,
            // multi-threaded block processing, and block type prioritization
            plugin.getSchematicManager().optimizedPasteSchematic(player, name, noAir, batchSize, delay);
            
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
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.schematic.adaptivepaste")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //schematic adaptivepaste <name> [noair]");
                return true;
            }
            
            String name = args[0];
            boolean noAir = false;
            
            // Parse noair argument
            for (int i = 1; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("noair")) {
                    noAir = true;
                }
            }
            
            // Get default batch size and delay from config
            int batchSize = plugin.getConfig().getInt("paste.batch-size", 500);
            int delay = plugin.getConfig().getInt("paste.delay", 1);
            
            // Use our improved optimized system which combines all the optimizations
            plugin.getSchematicManager().optimizedPasteSchematic(player, name, noAir, batchSize, delay);
            
            return true;
        }
    }
} 