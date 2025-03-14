package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Operation that creates a pyramid of blocks.
 */
public class PyramidOperation extends AbstractOperation {
    private final Vector3 base;
    private final int size;
    private final Pattern pattern;
    private final boolean hollow;
    
    /**
     * Creates a new pyramid operation.
     * @param player The player executing the operation
     * @param base The base center point of the pyramid
     * @param size The size (width) of the pyramid
     * @param pattern The pattern to use for the pyramid
     * @param hollow Whether the pyramid should be hollow
     */
    public PyramidOperation(Player player, Vector3 base, int size, Pattern pattern, boolean hollow) {
        super(player);
        this.base = base;
        this.size = Math.max(1, size); // Ensure size is at least 1
        this.pattern = pattern;
        this.hollow = hollow;
    }
    
    /**
     * Creates a new pyramid operation with a mask.
     * @param player The player executing the operation
     * @param base The base center point of the pyramid
     * @param size The size (width) of the pyramid
     * @param pattern The pattern to use for the pyramid
     * @param hollow Whether the pyramid should be hollow
     * @param mask The mask to apply
     */
    public PyramidOperation(Player player, Vector3 base, int size, Pattern pattern, boolean hollow, Mask mask) {
        super(player, mask);
        this.base = base;
        this.size = Math.max(1, size); // Ensure size is at least 1
        this.pattern = pattern;
        this.hollow = hollow;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Calculate the half-size (pyramid goes from base-half to base+half)
        int half = size / 2;
        
        // Calculate bounds for the pyramid
        int minX = base.getX() - half;
        int maxX = base.getX() + half;
        int minZ = base.getZ() - half;
        int maxZ = base.getZ() + half;
        int maxY = base.getY() + size; // Pyramid height equals its width
        
        // Start from the top of the pyramid and work downwards
        for (int y = base.getY(); y < maxY; y++) {
            // Calculate the level width based on pyramid height
            int level = y - base.getY();
            int levelSize = size - (level * 2);
            
            // Skip levels that would be too small (can happen with even-sized pyramids)
            if (levelSize <= 0) {
                break;
            }
            
            // Calculate the bounds for this level
            int levelMinX = base.getX() - levelSize / 2;
            int levelMaxX = levelMinX + levelSize - 1;
            int levelMinZ = base.getZ() - levelSize / 2;
            int levelMaxZ = levelMinZ + levelSize - 1;
            
            // Check if we need to create a hollow level
            boolean isHollowLevel = hollow && level > 0 && level < size - 1 && levelSize > 2;
            
            // Iterate through the blocks in this level
            for (int x = levelMinX; x <= levelMaxX; x++) {
                for (int z = levelMinZ; z <= levelMaxZ; z++) {
                    // For hollow pyramids, skip inner blocks
                    if (isHollowLevel && x > levelMinX && x < levelMaxX && z > levelMinZ && z < levelMaxZ) {
                        continue;
                    }
                    
                    Vector3 pos = new Vector3(x, y, z);
                    Block block = world.getBlockAt(x, y, z);
                    
                    // Check if the block matches the mask
                    if (!matchesMask(block)) {
                        continue;
                    }
                    
                    BlockState oldState = block.getState();
                    BlockData newData = pattern.getBlockData(block.getLocation());
                    
                    if (newData != null && !oldState.getBlockData().equals(newData)) {
                        // Save the old state for undo
                        entry.addBlockState(pos, oldState, null);
                        
                        // Set the new block data
                        block.setBlockData(newData);
                        
                        // Save the new state for redo
                        entry.addBlockState(pos, null, block.getState());
                        
                        affected++;
                    }
                }
            }
        }
        
        sendMessage("Created a " + (hollow ? "hollow " : "") + "pyramid with " + affected + " blocks.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = (hollow ? "Hollow " : "") + "Pyramid (size=" + size + ", pattern=" + pattern.getDescription();
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Calculate the volume of the pyramid
        // For a square pyramid, volume = (1/3) * base area * height
        // Base area = size^2, height = size
        int totalVolume = (size * size * size) / 3;
        
        if (hollow) {
            // For hollow pyramids, subtract the volume of the inner pyramid
            // Inner pyramid has a base that's 2 blocks smaller and a height that's 1 block shorter
            int innerSize = Math.max(0, size - 2);
            int innerVolume = innerSize > 0 ? (innerSize * innerSize * innerSize) / 3 : 0;
            return totalVolume - innerVolume;
        } else {
            return totalVolume;
        }
    }
} 