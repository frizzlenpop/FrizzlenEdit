package org.frizzlenpop.frizzlenEdit.brushes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * A brush that smooths terrain.
 */
public class SmoothBrush implements Brush {
    private final FrizzlenEdit plugin;
    private final int radius;
    
    /**
     * Create a new smooth brush.
     * @param plugin The plugin instance
     * @param radius The radius
     */
    public SmoothBrush(FrizzlenEdit plugin, int radius) {
        this.plugin = plugin;
        this.radius = radius;
    }
    
    @Override
    public void use(Player player, Vector3 position, String mask) {
        World world = player.getWorld();
        
        // Create a history entry
        HistoryEntry entry = new HistoryEntry(player, world, "Smooth brush");
        
        // Calculate the bounds of the sphere
        int radiusSquared = radius * radius;
        
        // First pass: collect block data
        Map<Vector3, BlockData> originalBlocks = new HashMap<>();
        
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
                    
                    // Store the original block data
                    originalBlocks.put(blockPos, block.getBlockData());
                }
            }
        }
        
        // Second pass: smooth the terrain
        for (Map.Entry<Vector3, BlockData> entry2 : originalBlocks.entrySet()) {
            Vector3 blockPos = entry2.getKey();
            Block block = blockPos.toBlock(world);
            
            // Skip air blocks
            if (block.getType().isAir()) {
                continue;
            }
            
            // Get the most common block type in the neighborhood
            BlockData newData = getMostCommonBlockData(blockPos, originalBlocks);
            
            // Skip if the block type is the same
            if (newData == null || newData.getMaterial() == block.getType()) {
                continue;
            }
            
            // Save the previous state for undo
            entry.addBlockState(blockPos, block.getState(), null);
            
            // Set the block
            block.setBlockData(newData);
            
            // Save the new state for redo
            entry.addBlockState(blockPos, null, block.getState());
        }
        
        // Add the entry to the history
        plugin.getHistoryManager().addEntry(entry);
        
        player.sendMessage(ChatColor.GREEN + "Smooth brush used at " + position + " with radius " + radius + ".");
    }
    
    /**
     * Get the most common block data in the neighborhood of a position.
     * @param position The position
     * @param blocks The map of positions to block data
     * @return The most common block data, or null if no blocks are found
     */
    private BlockData getMostCommonBlockData(Vector3 position, Map<Vector3, BlockData> blocks) {
        Map<Material, Integer> counts = new HashMap<>();
        
        // Count the occurrences of each material in the neighborhood
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Vector3 neighborPos = position.add(new Vector3(x, y, z));
                    BlockData data = blocks.get(neighborPos);
                    
                    if (data != null) {
                        Material material = data.getMaterial();
                        counts.put(material, counts.getOrDefault(material, 0) + 1);
                    }
                }
            }
        }
        
        // Find the most common material
        Material mostCommon = null;
        int maxCount = 0;
        
        for (Map.Entry<Material, Integer> entry : counts.entrySet()) {
            Material material = entry.getKey();
            int count = entry.getValue();
            
            // Skip air
            if (material.isAir()) {
                continue;
            }
            
            if (count > maxCount) {
                maxCount = count;
                mostCommon = material;
            }
        }
        
        // Return the block data for the most common material
        if (mostCommon != null) {
            final Material finalMostCommon = mostCommon;
            return blocks.values().stream()
                .filter(data -> data.getMaterial() == finalMostCommon)
                .findFirst()
                .orElse(null);
        }
        
        return null;
    }
    
    @Override
    public int getRadius() {
        return radius;
    }
    
    @Override
    public String getDescription() {
        return "Smooth brush, radius " + radius;
    }
} 