package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An operation that removes blocks near the player that match a specific type.
 */
public class RemoveNearOperation implements Operation {
    private final Player player;
    private final int radius;
    private final Material material;
    private final boolean useHandItem;
    
    /**
     * Create a new remove near operation using a specific material.
     * @param player The player
     * @param radius The radius around the player to search
     * @param material The material to remove
     */
    public RemoveNearOperation(Player player, int radius, Material material) {
        this.player = player;
        this.radius = radius;
        this.material = material;
        this.useHandItem = false;
    }
    
    /**
     * Create a new remove near operation using the item in the player's hand.
     * @param player The player
     * @param radius The radius around the player to search
     */
    public RemoveNearOperation(Player player, int radius) {
        this.player = player;
        this.radius = radius;
        this.material = null;
        this.useHandItem = true;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        Material targetMaterial;
        
        // Create a history entry
        String materialDescription;
        
        // Determine target material
        if (useHandItem) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage("You must hold an item in your hand to use this operation!");
                return null;
            }
            targetMaterial = handItem.getType();
            materialDescription = "hand item (" + targetMaterial.toString().toLowerCase() + ")";
        } else {
            targetMaterial = material;
            materialDescription = targetMaterial.toString().toLowerCase();
        }
        
        // Create the history entry with description
        HistoryEntry entry = new HistoryEntry(player, world, "Remove near for " + materialDescription + " in radius " + radius);
        
        // Calculate bounding box to search
        int minX = playerLoc.getBlockX() - radius;
        int minY = Math.max(playerLoc.getBlockY() - radius, 0);
        int minZ = playerLoc.getBlockZ() - radius;
        int maxX = playerLoc.getBlockX() + radius;
        int maxY = Math.min(playerLoc.getBlockY() + radius, world.getMaxHeight() - 1);
        int maxZ = playerLoc.getBlockZ() + radius;
        
        int count = 0;
        List<Block> blocksToRemove = new ArrayList<>();
        
        // First, find all blocks to remove
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    
                    // Calculate distance from player
                    double distanceSquared = loc.distanceSquared(playerLoc);
                    if (distanceSquared <= radius * radius) {
                        Block block = world.getBlockAt(loc);
                        
                        // Check if the block matches the target material
                        if (block.getType() == targetMaterial) {
                            blocksToRemove.add(block);
                            count++;
                        }
                    }
                }
            }
        }
        
        // Now remove the blocks (set to air)
        for (Block block : blocksToRemove) {
            Vector3 pos = Vector3.fromLocation(block.getLocation());
            
            // Save the previous state for undo
            BlockState oldState = block.getState();
            
            // Set the block to air
            block.setType(Material.AIR);
            
            // Save the new state for redo
            BlockState newState = block.getState();
            
            // Add to history
            entry.addBlockState(pos, oldState, newState);
        }
        
        player.sendMessage("Removed " + count + " blocks of " + materialDescription);
        return entry;
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public String getDescription() {
        String materialName = useHandItem ? 
            "hand item (" + player.getInventory().getItemInMainHand().getType().toString().toLowerCase() + ")" : 
            material.toString().toLowerCase();
        
        return "Remove near operation for " + materialName + " in radius " + radius;
    }
    
    @Override
    public int getVolume() {
        // Estimate the maximum volume as a sphere
        return (int) (4.0/3.0 * Math.PI * radius * radius * radius);
    }
} 