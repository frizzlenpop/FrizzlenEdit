package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.operations.Operation;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

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
    
    /**
     * Command handler for the smooth command.
     */
    public static class SmoothCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public SmoothCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.region.smooth")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            // Parse optional parameters
            int iterations = 4; // Default value
            double heightFactor = 2.0; // Default value
            boolean erodeSteepSlopes = true; // Default enabled
            boolean preserveTopLayer = true; // Default enabled
            double naturalVariation = 0.2; // Default value
            
            // Display usage
            if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
                player.sendMessage(ChatColor.GREEN + "Enhanced Smooth Command Usage:");
                player.sendMessage(ChatColor.GRAY + "//smooth [iterations] [heightFactor] [options]");
                player.sendMessage(ChatColor.GRAY + "  iterations: (Optional) Number of iterations (default: 4)");
                player.sendMessage(ChatColor.GRAY + "  heightFactor: (Optional) Height factor (default: 2.0)");
                player.sendMessage(ChatColor.GRAY + "  options: (Optional) Additional flags:");
                player.sendMessage(ChatColor.GRAY + "    -e/+e: Disable/Enable erosion simulation (default: enabled)");
                player.sendMessage(ChatColor.GRAY + "    -p/+p: Disable/Enable preserving surface layer (default: enabled)");
                player.sendMessage(ChatColor.GRAY + "    -v=N: Set natural variation (0.0-1.0, default: 0.2)");
                return true;
            }
            
            // Parse iterations
            if (args.length >= 1 && !args[0].startsWith("-") && !args[0].startsWith("+")) {
                try {
                    iterations = Integer.parseInt(args[0]);
                    if (iterations <= 0) {
                        player.sendMessage(ChatColor.RED + "Iterations must be greater than 0.");
                        return true;
                    }
                    if (iterations > 10) {
                        player.sendMessage(ChatColor.RED + "Iterations too large. Maximum is 10 to prevent lag.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    if (!args[0].equalsIgnoreCase("help")) {
                        player.sendMessage(ChatColor.RED + "Invalid iterations: " + args[0]);
                        player.sendMessage(ChatColor.YELLOW + "Usage: //smooth [iterations] [heightFactor] [options]");
                        player.sendMessage(ChatColor.YELLOW + "Try //smooth help for more information.");
                    }
                    return true;
                }
            }
            
            // Parse height factor
            if (args.length >= 2 && !args[1].startsWith("-") && !args[1].startsWith("+")) {
                try {
                    heightFactor = Double.parseDouble(args[1]);
                    if (heightFactor <= 0.0) {
                        player.sendMessage(ChatColor.RED + "Height factor must be greater than 0.");
                        return true;
                    }
                    if (heightFactor > 5.0) {
                        player.sendMessage(ChatColor.RED + "Height factor too large. Maximum is 5.0.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid height factor: " + args[1]);
                    player.sendMessage(ChatColor.YELLOW + "Usage: //smooth [iterations] [heightFactor] [options]");
                    return true;
                }
            }
            
            // Start parsing options from the appropriate argument index
            int optionStartIdx = 1;
            if (args.length >= 1 && !args[0].startsWith("-") && !args[0].startsWith("+")) optionStartIdx++;
            if (args.length >= 2 && !args[1].startsWith("-") && !args[1].startsWith("+")) optionStartIdx++;
            
            // Parse all option flags
            for (int i = optionStartIdx; i < args.length; i++) {
                String option = args[i].toLowerCase();
                
                if (option.equals("-e")) {
                    erodeSteepSlopes = false;
                } else if (option.equals("+e")) {
                    erodeSteepSlopes = true;
                } else if (option.equals("-p")) {
                    preserveTopLayer = false;
                } else if (option.equals("+p")) {
                    preserveTopLayer = true;
                } else if (option.startsWith("-v=")) {
                    try {
                        String valueStr = option.substring(3);
                        naturalVariation = Double.parseDouble(valueStr);
                        if (naturalVariation < 0.0 || naturalVariation > 1.0) {
                            player.sendMessage(ChatColor.RED + "Variation must be between 0.0 and 1.0.");
                            return true;
                        }
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        player.sendMessage(ChatColor.RED + "Invalid variation value: " + option);
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown option: " + option);
                    player.sendMessage(ChatColor.YELLOW + "Try //smooth help for valid options.");
                    return true;
                }
            }
            
            // Get the selection
            Region region = plugin.getSelectionManager().getSelection(player);
            
            // Create and execute the operation with all parameters
            Operation operation = plugin.getOperationManager().createSmoothOperation(
                player, region, iterations, heightFactor, erodeSteepSlopes, preserveTopLayer, naturalVariation);
            plugin.getOperationManager().execute(player, operation);
            
            return true;
        }
    }
    
    /**
     * Command handler for the drain command.
     */
    public static class DrainCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public DrainCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.region.drain")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (!plugin.getSelectionManager().hasSelection(player)) {
                player.sendMessage(ChatColor.RED + "You must make a selection first.");
                return true;
            }
            
            // Parse options
            boolean removeAllLiquids = false; // Default to water only
            
            if (args.length >= 1) {
                String option = args[0].toLowerCase();
                if (option.equals("all") || option.equals("a")) {
                    removeAllLiquids = true;
                    player.sendMessage(ChatColor.YELLOW + "Removing all liquids (water and lava)");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Removing water only. Use //drain all to remove all liquids.");
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Removing water only. Use //drain all to remove all liquids.");
            }
            
            // Get the selection
            Region region = plugin.getSelectionManager().getSelection(player);
            
            // Create and execute the operation
            Operation operation = plugin.getOperationManager().createDrainOperation(player, region, 0, removeAllLiquids);
            plugin.getOperationManager().execute(player, operation);
            
            return true;
        }
    }
    
    /**
     * Command handler for the cylinder command.
     */
    public static class CylinderCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public CylinderCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.block.cylinder")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: //cyl <block> <radius> <height> [hollow]");
                return true;
            }
            
            String blockType = args[0];
            int radius;
            int height;
            boolean hollow = false;
            
            try {
                radius = Integer.parseInt(args[1]);
                height = Integer.parseInt(args[2]);
                
                if (radius <= 0 || height <= 0) {
                    player.sendMessage(ChatColor.RED + "Radius and height must be positive numbers.");
                    return true;
                }
                
                if (args.length >= 4 && args[3].equalsIgnoreCase("hollow")) {
                    hollow = true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Radius and height must be valid numbers.");
                return true;
            }
            
            // Get the player's location as the center of the cylinder
            Vector3 center = Vector3.fromLocation(player.getLocation());
            
            // Create and execute the operation
            try {
                Material material = Material.matchMaterial(blockType);
                if (material == null) {
                    player.sendMessage(ChatColor.RED + "Invalid block type: " + blockType);
                    player.sendMessage(ChatColor.YELLOW + "Common material names: stone, dirt, grass_block, oak_planks, glass, etc.");
                    return true;
                }
                
                Operation operation = plugin.getOperationManager().createCylinderOperation(player, center, material, radius, height, hollow);
                plugin.getOperationManager().execute(player, operation);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
            }
            
            return true;
        }
    }
} 