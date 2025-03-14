package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.operations.Operation;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.patterns.PatternFactory;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.masks.MaskFactory;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

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
    
    /**
     * Command handler for the removenear command.
     */
    public static class RemoveNearCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public RemoveNearCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.block.removenear")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Default radius
            int radius = 5;
            boolean useHandItem = false;
            Material material = null;
            
            if (args.length >= 1) {
                // Check if the first argument is "hand"
                if (args[0].equalsIgnoreCase("hand")) {
                    useHandItem = true;
                    
                    // If there's a second argument, it's the radius
                    if (args.length >= 2) {
                        try {
                            radius = Integer.parseInt(args[1]);
                            if (radius <= 0 || radius > 50) {
                                player.sendMessage(ChatColor.RED + "Radius must be between 1 and 50.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid radius. Usage: //removenear hand [radius]");
                            return true;
                        }
                    }
                } else {
                    // First argument is either material or radius
                    try {
                        // Try to parse as radius
                        radius = Integer.parseInt(args[0]);
                        if (radius <= 0 || radius > 50) {
                            player.sendMessage(ChatColor.RED + "Radius must be between 1 and 50.");
                            return true;
                        }
                        
                        // If there's a second argument, it's the material
                        if (args.length >= 2) {
                            try {
                                material = Material.valueOf(args[1].toUpperCase());
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(ChatColor.RED + "Invalid material: " + args[1]);
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify a material. Usage: //removenear <radius> <material>");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        // First argument must be material
                        try {
                            material = Material.valueOf(args[0].toUpperCase());
                            
                            // If there's a second argument, it's the radius
                            if (args.length >= 2) {
                                try {
                                    radius = Integer.parseInt(args[1]);
                                    if (radius <= 0 || radius > 50) {
                                        player.sendMessage(ChatColor.RED + "Radius must be between 1 and 50.");
                                        return true;
                                    }
                                } catch (NumberFormatException ex) {
                                    player.sendMessage(ChatColor.RED + "Invalid radius. Usage: //removenear <material> [radius]");
                                    return true;
                                }
                            }
                        } catch (IllegalArgumentException ex) {
                            player.sendMessage(ChatColor.RED + "Invalid material: " + args[0]);
                            return true;
                        }
                    }
                }
            } else {
                // No arguments, show usage
                player.sendMessage(ChatColor.RED + "Usage:");
                player.sendMessage(ChatColor.RED + "  //removenear <radius> <material> - Remove blocks of a material");
                player.sendMessage(ChatColor.RED + "  //removenear <material> [radius] - Remove blocks of a material");
                player.sendMessage(ChatColor.RED + "  //removenear hand [radius] - Remove blocks matching item in hand");
                return true;
            }
            
            // Create and execute the operation
            Operation operation;
            if (useHandItem) {
                operation = plugin.getOperationManager().createRemoveNearOperation(player, radius);
            } else {
                operation = plugin.getOperationManager().createRemoveNearOperation(player, radius, material);
            }
            
            plugin.getOperationManager().execute(player, operation);
            
            return true;
        }
    }
    
    /**
     * Command for filling a region with a pattern.
     */
    public static class FillCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public FillCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.region.fill")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            // Check arguments
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "Usage: //fill <pattern> [mask:<mask>]");
                return true;
            }
            
            try {
                // Get the pattern
                String patternStr = args[0];
                Pattern pattern;
                
                try {
                    pattern = PatternFactory.parsePattern(player, patternStr);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return true;
                }
                
                // Parse mask if provided
                Mask mask = null;
                
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.startsWith("mask:")) {
                        // Extract the mask string after "mask:"
                        String maskStr = arg.substring(5);
                        try {
                            mask = MaskFactory.parseMask(player, maskStr);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                            return true;
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (mask != null) {
                    operation = plugin.getOperationManager().createFillOperation(player, region, pattern, mask);
                } else {
                    operation = plugin.getOperationManager().createFillOperation(player, region, pattern);
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for creating walls around a region.
     */
    public static class WallsCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public WallsCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.region.walls")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            // Check arguments
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "Usage: //walls <pattern> [mask:<mask>]");
                return true;
            }
            
            try {
                // Get the pattern
                String patternStr = args[0];
                Pattern pattern;
                
                try {
                    pattern = PatternFactory.parsePattern(player, patternStr);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return true;
                }
                
                // Parse mask if provided
                Mask mask = null;
                
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.startsWith("mask:")) {
                        // Extract the mask string after "mask:"
                        String maskStr = arg.substring(5);
                        try {
                            mask = MaskFactory.parseMask(player, maskStr);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                            return true;
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (mask != null) {
                    operation = plugin.getOperationManager().createWallsOperation(player, region, pattern, mask);
                } else {
                    operation = plugin.getOperationManager().createWallsOperation(player, region, pattern);
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for creating an outline around a region.
     */
    public static class OutlineCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public OutlineCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.region.outline")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            // Check arguments
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "Usage: //outline <pattern> [mask:<mask>]");
                return true;
            }
            
            try {
                // Get the pattern
                String patternStr = args[0];
                Pattern pattern;
                
                try {
                    pattern = PatternFactory.parsePattern(player, patternStr);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return true;
                }
                
                // Parse mask if provided
                Mask mask = null;
                
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.startsWith("mask:")) {
                        // Extract the mask string after "mask:"
                        String maskStr = arg.substring(5);
                        try {
                            mask = MaskFactory.parseMask(player, maskStr);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                            return true;
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (mask != null) {
                    operation = plugin.getOperationManager().createOutlineOperation(player, region, pattern, mask);
                } else {
                    operation = plugin.getOperationManager().createOutlineOperation(player, region, pattern);
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for making a region hollow.
     */
    public static class HollowCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public HollowCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.region.hollow")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            try {
                // Default values
                Pattern shellPattern = null;
                int thickness = 1;
                Mask mask = null;
                
                // Parse arguments
                if (args.length > 0) {
                    // First argument could be pattern or thickness
                    String firstArg = args[0];
                    if (firstArg.matches("\\d+")) {
                        // It's a thickness
                        thickness = Integer.parseInt(firstArg);
                        if (thickness <= 0) {
                            player.sendMessage(ChatColor.RED + "Thickness must be greater than 0.");
                            return true;
                        }
                    } else {
                        // It's a pattern
                        try {
                            shellPattern = PatternFactory.parsePattern(player, firstArg);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return true;
                        }
                        
                        // Second argument could be thickness
                        if (args.length > 1 && args[1].matches("\\d+")) {
                            thickness = Integer.parseInt(args[1]);
                            if (thickness <= 0) {
                                player.sendMessage(ChatColor.RED + "Thickness must be greater than 0.");
                                return true;
                            }
                        }
                    }
                    
                    // Check for mask
                    for (String arg : args) {
                        if (arg.startsWith("mask:")) {
                            // Extract the mask string after "mask:"
                            String maskStr = arg.substring(5);
                            try {
                                mask = MaskFactory.parseMask(player, maskStr);
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                                return true;
                            }
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (shellPattern != null) {
                    if (mask != null) {
                        operation = plugin.getOperationManager().createHollowOperation(player, region, shellPattern, thickness, mask);
                    } else {
                        operation = plugin.getOperationManager().createHollowOperation(player, region, shellPattern, thickness);
                    }
                } else {
                    if (mask != null) {
                        operation = plugin.getOperationManager().createHollowOperation(player, region, thickness, mask);
                    } else {
                        operation = plugin.getOperationManager().createHollowOperation(player, region, thickness);
                    }
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for naturalizing terrain.
     */
    public static class NaturalizeCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public NaturalizeCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.terraforming.naturalize")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            try {
                // Default value
                boolean preserveWater = true;
                Mask mask = null;
                
                // Parse arguments
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.equals("nowater")) {
                        preserveWater = false;
                    } else if (arg.startsWith("mask:")) {
                        // Extract the mask string after "mask:"
                        String maskStr = arg.substring(5);
                        try {
                            mask = MaskFactory.parseMask(player, maskStr);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                            return true;
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (mask != null) {
                    operation = plugin.getOperationManager().createNaturalizeOperation(player, region, preserveWater, mask);
                } else {
                    operation = plugin.getOperationManager().createNaturalizeOperation(player, region, preserveWater);
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for adding an overlay on top of terrain.
     */
    public static class OverlayCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public OverlayCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.terraforming.overlay")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            // Check arguments
            if (args.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "Usage: //overlay <pattern> [thickness] [ignorewater] [mask:<mask>]");
                return true;
            }
            
            try {
                // Get the pattern
                String patternStr = args[0];
                Pattern pattern;
                
                try {
                    pattern = PatternFactory.parsePattern(player, patternStr);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return true;
                }
                
                // Default values
                int thickness = 1;
                boolean ignoreWater = false;
                Mask mask = null;
                
                // Parse additional arguments
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.matches("\\d+")) {
                        // It's a thickness
                        thickness = Integer.parseInt(arg);
                        if (thickness <= 0) {
                            player.sendMessage(ChatColor.RED + "Thickness must be greater than 0.");
                            return true;
                        }
                    } else if (arg.equals("ignorewater")) {
                        ignoreWater = true;
                    } else if (arg.startsWith("mask:")) {
                        // Extract the mask string after "mask:"
                        String maskStr = arg.substring(5);
                        try {
                            mask = MaskFactory.parseMask(player, maskStr);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                            return true;
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (mask != null) {
                    operation = plugin.getOperationManager().createOverlayOperation(player, region, pattern, thickness, ignoreWater, mask);
                } else {
                    operation = plugin.getOperationManager().createOverlayOperation(player, region, pattern, thickness, ignoreWater);
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for generating caves.
     */
    public static class CavesCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public CavesCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.terraforming.caves")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            try {
                // Default values
                double threshold = 0.4;
                double scale = 0.03;
                boolean addOres = true;
                double oreFrequency = 0.1;
                Mask mask = null;
                
                // Parse arguments
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.startsWith("threshold:")) {
                        try {
                            threshold = Double.parseDouble(arg.substring(10));
                            if (threshold < 0.1 || threshold > 0.9) {
                                player.sendMessage(ChatColor.RED + "Threshold must be between 0.1 and 0.9.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid threshold value.");
                            return true;
                        }
                    } else if (arg.startsWith("scale:")) {
                        try {
                            scale = Double.parseDouble(arg.substring(6));
                            if (scale < 0.01 || scale > 0.1) {
                                player.sendMessage(ChatColor.RED + "Scale must be between 0.01 and 0.1.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid scale value.");
                            return true;
                        }
                    } else if (arg.equals("noores")) {
                        addOres = false;
                    } else if (arg.startsWith("ores:")) {
                        try {
                            oreFrequency = Double.parseDouble(arg.substring(5));
                            if (oreFrequency < 0.0 || oreFrequency > 0.5) {
                                player.sendMessage(ChatColor.RED + "Ore frequency must be between 0.0 and 0.5.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid ore frequency value.");
                            return true;
                        }
                    } else if (arg.startsWith("mask:")) {
                        // Extract the mask string after "mask:"
                        String maskStr = arg.substring(5);
                        try {
                            mask = MaskFactory.parseMask(player, maskStr);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid mask: " + e.getMessage());
                            return true;
                        }
                    }
                }
                
                // Create the operation
                Operation operation;
                if (mask != null) {
                    operation = plugin.getOperationManager().createCavesOperation(player, region, threshold, scale, addOres, oreFrequency, mask);
                } else {
                    operation = plugin.getOperationManager().createCavesOperation(player, region, threshold, scale, addOres, oreFrequency);
                }
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for regenerating chunks.
     */
    public static class RegenCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public RegenCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.chunk.regen")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            try {
                // Default values
                boolean keepEntities = false;
                boolean keepStructures = false;
                
                // Parse arguments
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    
                    if (arg.equals("keepentities")) {
                        keepEntities = true;
                    } else if (arg.equals("keepstructures")) {
                        keepStructures = true;
                    }
                }
                
                // Count chunks
                Vector3 min = region.getMinimumPoint();
                Vector3 max = region.getMaximumPoint();
                int minChunkX = min.getX() >> 4;
                int minChunkZ = min.getZ() >> 4;
                int maxChunkX = max.getX() >> 4;
                int maxChunkZ = max.getZ() >> 4;
                int chunkCount = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
                
                // Confirm with the user if many chunks are affected
                if (chunkCount > 9 && args.length == 0) {
                    player.sendMessage(ChatColor.YELLOW + "You are about to regenerate " + chunkCount + " chunks.");
                    player.sendMessage(ChatColor.YELLOW + "This may take a while and cannot be undone.");
                    player.sendMessage(ChatColor.YELLOW + "To confirm, use: //regen confirm");
                    return true;
                }
                
                // Check for confirmation
                if (chunkCount > 9 && !args[0].equalsIgnoreCase("confirm")) {
                    player.sendMessage(ChatColor.YELLOW + "You are about to regenerate " + chunkCount + " chunks.");
                    player.sendMessage(ChatColor.YELLOW + "This may take a while and cannot be undone.");
                    player.sendMessage(ChatColor.YELLOW + "To confirm, use: //regen confirm");
                    return true;
                }
                
                // Create the operation
                Operation operation = plugin.getOperationManager().createChunkRegenerationOperation(player, region, keepEntities, keepStructures);
                
                // Execute the operation
                plugin.getOperationManager().execute(player, operation);
                
                return true;
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }
    }
    
    /**
     * Command for showing chunk information.
     */
    public static class ShowChunkInfoCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public ShowChunkInfoCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Check permission
            if (!player.hasPermission("frizzlenedit.chunk.info")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Check if selection exists
            Region region = plugin.getSelectionManager().getSelection(player);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Please make a selection first.");
                return true;
            }
            
            return true;
        }
    }
} 