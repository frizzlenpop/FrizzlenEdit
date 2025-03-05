package org.frizzlenpop.frizzlenEdit.brushes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * A brush that creates cylinders.
 */
public class CylinderBrush implements Brush {
    private final FrizzlenEdit plugin;
    private final BlockData blockData;
    private final int radius;
    private final int height;
    
    /**
     * Create a new cylinder brush.
     * @param plugin The plugin instance
     * @param material The material
     * @param radius The radius
     * @param height The height
     */
    public CylinderBrush(FrizzlenEdit plugin, String material, int radius, int height) {
        this.plugin = plugin;
        this.blockData = Bukkit.createBlockData(material);
        this.radius = radius;
        this.height = height;
    }
    
    @Override
    public void use(Player player, Vector3 position, String mask) {
        World world = player.getWorld();
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Cylinder brush: " + blockData.getAsString());
        
        // Calculate the bounds of the cylinder
        int radiusSquared = radius * radius;
        int halfHeight = height / 2;
        
        // Iterate through all blocks in the cylinder
        for (int x = -radius; x <= radius; x++) {
            for (int y = -halfHeight; y <= halfHeight; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Calculate the distance squared from the center (ignoring y)
                    int distanceSquared = x*x + z*z;
                    
                    // Skip blocks outside the cylinder
                    if (distanceSquared > radiusSquared) {
                        continue;
                    }
                    
                    // Calculate the position of this block
                    Vector3 blockPos = position.add(new Vector3(x, y, z));
                    
                    // Get the block
                    Block block = blockPos.toBlock(world);
                    
                    // Check the mask
                    if (mask != null) {
                        Material maskMaterial = Material.matchMaterial(mask);
                        if (maskMaterial != null && block.getType() != maskMaterial) {
                            continue;
                        }
                    }
                    
                    // Save the previous state for undo
                    entry.addBlockState(blockPos, block.getState(), null);
                    
                    // Set the block
                    block.setBlockData(blockData);
                    
                    // Save the new state for redo
                    entry.addBlockState(blockPos, null, block.getState());
                }
            }
        }
        
        // Add the entry to the history
        plugin.getHistoryManager().addEntry(entry);
        
        player.sendMessage(ChatColor.GREEN + "Cylinder brush used at " + position + " with radius " + radius + " and height " + height + ".");
    }
    
    @Override
    public int getRadius() {
        return radius;
    }
    
    @Override
    public String getDescription() {
        return "Cylinder brush: " + blockData.getAsString() + ", radius " + radius + ", height " + height;
    }
} 