package org.frizzlenpop.frizzlenEdit.clipboard;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a clipboard that can store a copied region of blocks.
 */
public class Clipboard {
    // Origin point from where the copy was made
    private final Vector3 origin;
    
    // Dimensions of the clipboard
    private int width;
    private int height;
    private int length;
    
    // Stored blocks (relative coordinates -> block data)
    private final Map<Vector3, BlockData> blocks = new HashMap<>();
    
    /**
     * Create a new clipboard from a region.
     * @param region The region to copy
     * @param origin The origin point
     */
    public Clipboard(Region region, Vector3 origin) {
        this.origin = origin;
        
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        this.width = max.getX() - min.getX() + 1;
        this.height = max.getY() - min.getY() + 1;
        this.length = max.getZ() - min.getZ() + 1;
    }
    
    /**
     * Create a new empty clipboard with specific dimensions.
     * This constructor is useful for schematic loading.
     * 
     * @param origin The origin point
     * @param width The width of the clipboard
     * @param height The height of the clipboard
     * @param length The length of the clipboard
     */
    public Clipboard(Vector3 origin, int width, int height, int length) {
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.length = length;
    }
    
    /**
     * Get the origin point of this clipboard.
     * @return The origin point
     */
    public Vector3 getOrigin() {
        return origin;
    }
    
    /**
     * Get the width of this clipboard.
     * @return The width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Get the height of this clipboard.
     * @return The height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Get the length of this clipboard.
     * @return The length
     */
    public int getLength() {
        return length;
    }
    
    /**
     * Get the volume of this clipboard.
     * @return The volume
     */
    public int getVolume() {
        return width * height * length;
    }
    
    /**
     * Add a block to this clipboard.
     * @param position The position (relative to the minimum corner)
     * @param data The block data
     */
    public void setBlock(Vector3 position, BlockData data) {
        blocks.put(position, data);
    }
    
    /**
     * Get a block from this clipboard.
     * @param position The position (relative to the minimum corner)
     * @return The block data, or null if no block exists at that position
     */
    public BlockData getBlock(Vector3 position) {
        return blocks.get(position);
    }
    
    /**
     * Check if this clipboard contains a block at the given position.
     * @param position The position
     * @return True if a block exists at the position
     */
    public boolean containsBlock(Vector3 position) {
        return blocks.containsKey(position);
    }
    
    /**
     * Get all blocks in this clipboard.
     * @return A map of positions to block data
     */
    public Map<Vector3, BlockData> getBlocks() {
        return blocks;
    }
    
    /**
     * Rotate the clipboard around the Y axis.
     * @param degrees The degrees to rotate (must be a multiple of 90)
     */
    public void rotate(int degrees) {
        // Normalize degrees to 0, 90, 180, or 270
        degrees = ((degrees % 360) + 360) % 360;
        if (degrees % 90 != 0) {
            throw new IllegalArgumentException("Rotation must be a multiple of 90 degrees");
        }
        
        // No rotation needed
        if (degrees == 0) {
            return;
        }
        
        Map<Vector3, BlockData> rotatedBlocks = new HashMap<>();
        
        for (Map.Entry<Vector3, BlockData> entry : blocks.entrySet()) {
            Vector3 oldPos = entry.getKey();
            BlockData data = entry.getValue();
            
            // Calculate the new position after rotation
            Vector3 newPos;
            switch (degrees) {
                case 90:
                    newPos = new Vector3(length - 1 - oldPos.getZ(), oldPos.getY(), oldPos.getX());
                    break;
                case 180:
                    newPos = new Vector3(width - 1 - oldPos.getX(), oldPos.getY(), length - 1 - oldPos.getZ());
                    break;
                case 270:
                    newPos = new Vector3(oldPos.getZ(), oldPos.getY(), width - 1 - oldPos.getX());
                    break;
                default:
                    // Should never happen
                    newPos = oldPos;
            }
            
            // TODO: Rotate the block data as well
            // This requires handling block-specific rotation which we'll implement later
            
            rotatedBlocks.put(newPos, data);
        }
        
        // Clear the old blocks and add the rotated ones
        blocks.clear();
        blocks.putAll(rotatedBlocks);
        
        // Update dimensions if needed (90 or 270 degrees)
        if (degrees == 90 || degrees == 270) {
            // width and length are swapped
            int temp = width;
            width = length;
            length = temp;
        }
    }
    
    /**
     * Flip the clipboard along an axis.
     * @param direction The direction to flip (x, y, or z)
     */
    public void flip(char direction) {
        Map<Vector3, BlockData> flippedBlocks = new HashMap<>();
        
        for (Map.Entry<Vector3, BlockData> entry : blocks.entrySet()) {
            Vector3 oldPos = entry.getKey();
            BlockData data = entry.getValue();
            
            // Calculate the new position after flipping
            Vector3 newPos;
            switch (Character.toLowerCase(direction)) {
                case 'x':
                    newPos = new Vector3(width - 1 - oldPos.getX(), oldPos.getY(), oldPos.getZ());
                    break;
                case 'y':
                    newPos = new Vector3(oldPos.getX(), height - 1 - oldPos.getY(), oldPos.getZ());
                    break;
                case 'z':
                    newPos = new Vector3(oldPos.getX(), oldPos.getY(), length - 1 - oldPos.getZ());
                    break;
                default:
                    throw new IllegalArgumentException("Direction must be x, y, or z");
            }
            
            // TODO: Flip the block data as well
            // This requires handling block-specific flipping which we'll implement later
            
            flippedBlocks.put(newPos, data);
        }
        
        // Clear the old blocks and add the flipped ones
        blocks.clear();
        blocks.putAll(flippedBlocks);
    }
    
    /**
     * Copy blocks from a region to this clipboard.
     * @param region The region to copy
     * @param world The world
     */
    public void copy(Region region, World world) {
        Vector3 min = region.getMinimumPoint();
        
        // Clear existing blocks
        blocks.clear();
        
        // Copy each block
        for (Vector3 pos : region) {
            // Get the block at this world position
            Block block = pos.toBlock(world);
            
            // Calculate the relative position
            Vector3 relativePos = pos.subtract(min);
            
            // Store the block data
            blocks.put(relativePos, block.getBlockData().clone());
        }
    }
    
    /**
     * Paste this clipboard at a location.
     * @param position The position to paste at
     * @param world The world
     * @param ignoreAir Whether to ignore air blocks
     */
    public void paste(Vector3 position, World world, boolean ignoreAir) {
        for (Map.Entry<Vector3, BlockData> entry : blocks.entrySet()) {
            Vector3 relPos = entry.getKey();
            BlockData data = entry.getValue();
            
            // Skip air blocks if requested
            if (ignoreAir && data.getMaterial().isAir()) {
                continue;
            }
            
            // Calculate the world position
            Vector3 worldPos = position.add(relPos);
            
            // Set the block
            Block block = worldPos.toBlock(world);
            block.setBlockData(data);
        }
    }
} 