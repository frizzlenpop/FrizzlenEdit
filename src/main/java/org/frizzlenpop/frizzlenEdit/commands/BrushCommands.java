package org.frizzlenpop.frizzlenEdit.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.brushes.Brush;

/**
 * Handles brush-related commands.
 */
public class BrushCommands {
    
    /**
     * Command handler for the sphere brush command.
     */
    public static class SphereBrushCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public SphereBrushCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.brush.sphere")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: //brush sphere <material> <radius>");
                return true;
            }
            
            // Parse the material
            String materialName = args[0];
            Material material;
            try {
                material = Material.matchMaterial(materialName);
                if (material == null) {
                    player.sendMessage(ChatColor.RED + "Unknown material: " + materialName);
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
                return true;
            }
            
            // Parse the radius
            int radius;
            try {
                radius = Integer.parseInt(args[1]);
                if (radius <= 0) {
                    player.sendMessage(ChatColor.RED + "Radius must be greater than 0.");
                    return true;
                }
                
                int maxRadius = plugin.getConfigManager().getMaxBrushSize();
                if (radius > maxRadius) {
                    player.sendMessage(ChatColor.RED + "Radius too large. Maximum is " + maxRadius + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid radius: " + args[1]);
                return true;
            }
            
            // Create the brush
            plugin.getBrushManager().createSphereBrush(player, materialName, radius);
            return true;
        }
    }
    
    /**
     * Command handler for the cylinder brush command.
     */
    public static class CylinderBrushCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public CylinderBrushCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.brush.cylinder")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: //brush cylinder <material> <radius> <height>");
                return true;
            }
            
            // Parse the material
            String materialName = args[0];
            Material material;
            try {
                material = Material.matchMaterial(materialName);
                if (material == null) {
                    player.sendMessage(ChatColor.RED + "Unknown material: " + materialName);
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
                return true;
            }
            
            // Parse the radius
            int radius;
            try {
                radius = Integer.parseInt(args[1]);
                if (radius <= 0) {
                    player.sendMessage(ChatColor.RED + "Radius must be greater than 0.");
                    return true;
                }
                
                int maxRadius = plugin.getConfigManager().getMaxBrushSize();
                if (radius > maxRadius) {
                    player.sendMessage(ChatColor.RED + "Radius too large. Maximum is " + maxRadius + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid radius: " + args[1]);
                return true;
            }
            
            // Parse the height
            int height;
            try {
                height = Integer.parseInt(args[2]);
                if (height <= 0) {
                    player.sendMessage(ChatColor.RED + "Height must be greater than 0.");
                    return true;
                }
                
                int maxHeight = plugin.getConfigManager().getMaxBrushSize() * 2;
                if (height > maxHeight) {
                    player.sendMessage(ChatColor.RED + "Height too large. Maximum is " + maxHeight + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid height: " + args[2]);
                return true;
            }
            
            // Create the brush
            plugin.getBrushManager().createCylinderBrush(player, materialName, radius, height);
            return true;
        }
    }
    
    /**
     * Command handler for the smooth brush command.
     */
    public static class SmoothBrushCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public SmoothBrushCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.brush.smooth")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //brush smooth <radius> [iterations] [heightFactor]");
                player.sendMessage(ChatColor.GRAY + "  radius: The radius of the brush");
                player.sendMessage(ChatColor.GRAY + "  iterations: (Optional) Number of smoothing iterations (default: 4)");
                player.sendMessage(ChatColor.GRAY + "  heightFactor: (Optional) Height factor for terrain smoothing (default: 2.0)");
                return true;
            }
            
            // Parse the radius
            int radius;
            try {
                radius = Integer.parseInt(args[0]);
                if (radius <= 0) {
                    player.sendMessage(ChatColor.RED + "Radius must be greater than 0.");
                    return true;
                }
                
                int maxRadius = plugin.getConfigManager().getMaxBrushSize();
                if (radius > maxRadius) {
                    player.sendMessage(ChatColor.RED + "Radius too large. Maximum is " + maxRadius + ".");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid radius: " + args[0]);
                return true;
            }
            
            // Parse the iterations (optional)
            int iterations = 4; // Default value
            if (args.length >= 2) {
                try {
                    iterations = Integer.parseInt(args[1]);
                    if (iterations <= 0) {
                        player.sendMessage(ChatColor.RED + "Iterations must be greater than 0.");
                        return true;
                    }
                    if (iterations > 10) {
                        player.sendMessage(ChatColor.RED + "Iterations too large. Maximum is 10 to prevent lag.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid iterations: " + args[1]);
                    return true;
                }
            }
            
            // Parse the height factor (optional)
            double heightFactor = 2.0; // Default value
            if (args.length >= 3) {
                try {
                    heightFactor = Double.parseDouble(args[2]);
                    if (heightFactor <= 0.0) {
                        player.sendMessage(ChatColor.RED + "Height factor must be greater than 0.");
                        return true;
                    }
                    if (heightFactor > 5.0) {
                        player.sendMessage(ChatColor.RED + "Height factor too large. Maximum is 5.0.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid height factor: " + args[2]);
                    return true;
                }
            }
            
            // Create the brush
            plugin.getBrushManager().createSmoothBrush(player, radius, iterations, heightFactor);
            return true;
        }
    }
    
    /**
     * Command handler for the none brush command (removes the brush).
     */
    public static class NoneBrushCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public NoneBrushCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.brush.none")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            // Remove the brush
            plugin.getBrushManager().removeBrush(player);
            player.sendMessage(ChatColor.GREEN + "Brush removed.");
            return true;
        }
    }
    
    /**
     * Command handler for the mask command.
     */
    public static class MaskCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public MaskCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.brush.mask")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: //mask <material>");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("none")) {
                plugin.getBrushManager().setMask(player, null);
                player.sendMessage(ChatColor.GREEN + "Brush mask removed.");
                return true;
            }
            
            // Parse the material
            String materialName = args[0];
            Material material;
            try {
                material = Material.matchMaterial(materialName);
                if (material == null) {
                    player.sendMessage(ChatColor.RED + "Unknown material: " + materialName);
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
                return true;
            }
            
            // Set the mask
            plugin.getBrushManager().setMask(player, materialName);
            player.sendMessage(ChatColor.GREEN + "Brush mask set to " + material.name().toLowerCase() + ".");
            return true;
        }
    }
    
    /**
     * Command handler for giving a brush tool.
     */
    public static class BrushToolCommand implements CommandExecutor {
        private final FrizzlenEdit plugin;
        
        public BrushToolCommand(FrizzlenEdit plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("frizzlenedit.brush.tool")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            
            plugin.getBrushManager().giveBrushTool(player);
            return true;
        }
    }
} 