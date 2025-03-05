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
 * A brush that creates spheres.
 */
public class SphereBrush implements Brush {
    private final FrizzlenEdit plugin;
    private final BlockData blockData;
    private final int radius;
    
    /**
     * Create a new sphere brush.
     * @param plugin The plugin instance
     * @param material The material
     * @param radius The radius
     */
    public SphereBrush(FrizzlenEdit plugin, String material, int radius) {
        this.plugin = plugin;
        this.blockData = Bukkit.createBlockData(material);
        this.radius = radius;
    }
    
    @Override
    public void use(Player player, Vector3 position, String mask) {
        World world = player.getWorld();
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Sphere brush: " + blockData.getAsString());
        
        // Calculate the bounds of the sphere
        int radiusSquared = radius * radius;
        
        // Iterate through all blocks in the cube around the position
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Calculate the distance squared from the center
                    int distanceSquared = x*x + y*y + z*z;
                    
                    // Skip blocks outside the sphere
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
        
        player.sendMessage(ChatColor.GREEN + "Sphere brush used at " + position + " with radius " + radius + ".");
    }
    
    @Override
    public int getRadius() {
        return radius;
    }
    
    @Override
    public String getDescription() {
        return "Sphere brush: " + blockData.getAsString() + ", radius " + radius;
    }
} 