package org.frizzlenpop.frizzlenEdit.selection;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a cuboid region in the world.
 */
public class Region implements Iterable<Vector3> {
    private final World world;
    private Vector3 pos1;
    private Vector3 pos2;
    private Vector3 min;
    private Vector3 max;
    
    /**
     * Create a new region with the given positions.
     * @param world The world
     * @param pos1 The first position
     * @param pos2 The second position
     */
    public Region(World world, Vector3 pos1, Vector3 pos2) {
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        recalculateMinMax();
    }
    
    /**
     * Recalculate the minimum and maximum corners of the region.
     */
    private void recalculateMinMax() {
        // Only calculate min and max if both positions are set
        if (pos1 != null && pos2 != null) {
            min = pos1.getMinimum(pos2);
            max = pos1.getMaximum(pos2);
        }
    }
    
    /**
     * Get the world of this region.
     * @return The world
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Get the first position of this region.
     * @return The first position
     */
    public Vector3 getPos1() {
        return pos1;
    }
    
    /**
     * Set the first position of this region.
     * @param pos1 The new position
     */
    public void setPos1(Vector3 pos1) {
        this.pos1 = pos1;
        recalculateMinMax();
    }
    
    /**
     * Get the second position of this region.
     * @return The second position
     */
    public Vector3 getPos2() {
        return pos2;
    }
    
    /**
     * Set the second position of this region.
     * @param pos2 The new position
     */
    public void setPos2(Vector3 pos2) {
        this.pos2 = pos2;
        recalculateMinMax();
    }
    
    /**
     * Get the minimum corner of this region.
     * @return The minimum corner
     */
    public Vector3 getMinimumPoint() {
        return min;
    }
    
    /**
     * Get the maximum corner of this region.
     * @return The maximum corner
     */
    public Vector3 getMaximumPoint() {
        return max;
    }
    
    /**
     * Get the width of this region.
     * @return The width (X-axis)
     */
    public int getWidth() {
        return max.getX() - min.getX() + 1;
    }
    
    /**
     * Get the height of this region.
     * @return The height (Y-axis)
     */
    public int getHeight() {
        return max.getY() - min.getY() + 1;
    }
    
    /**
     * Get the length of this region.
     * @return The length (Z-axis)
     */
    public int getLength() {
        return max.getZ() - min.getZ() + 1;
    }
    
    /**
     * Get the volume of this region.
     * @return The volume (number of blocks)
     */
    public int getVolume() {
        return getWidth() * getHeight() * getLength();
    }
    
    /**
     * Check if this region contains the given position.
     * @param position The position to check
     * @return True if the position is inside the region
     */
    public boolean contains(Vector3 position) {
        return position.getX() >= min.getX() && position.getX() <= max.getX()
            && position.getY() >= min.getY() && position.getY() <= max.getY()
            && position.getZ() >= min.getZ() && position.getZ() <= max.getZ();
    }
    
    /**
     * Expand the region in the given direction.
     * @param direction The direction (x, y, z)
     * @param amount The amount to expand
     */
    public void expand(Vector3 direction, int amount) {
        if (direction.getX() > 0) {
            max = new Vector3(max.getX() + amount, max.getY(), max.getZ());
        } else if (direction.getX() < 0) {
            min = new Vector3(min.getX() - amount, min.getY(), min.getZ());
        }
        
        if (direction.getY() > 0) {
            max = new Vector3(max.getX(), max.getY() + amount, max.getZ());
        } else if (direction.getY() < 0) {
            min = new Vector3(min.getX(), min.getY() - amount, min.getZ());
        }
        
        if (direction.getZ() > 0) {
            max = new Vector3(max.getX(), max.getY(), max.getZ() + amount);
        } else if (direction.getZ() < 0) {
            min = new Vector3(min.getX(), min.getY(), min.getZ() - amount);
        }
        
        // Update pos1 and pos2 to match the new min/max
        if (pos1.getX() <= pos2.getX()) {
            pos1 = new Vector3(min.getX(), pos1.getY(), pos1.getZ());
            pos2 = new Vector3(max.getX(), pos2.getY(), pos2.getZ());
        } else {
            pos1 = new Vector3(max.getX(), pos1.getY(), pos1.getZ());
            pos2 = new Vector3(min.getX(), pos2.getY(), pos2.getZ());
        }
        
        if (pos1.getY() <= pos2.getY()) {
            pos1 = new Vector3(pos1.getX(), min.getY(), pos1.getZ());
            pos2 = new Vector3(pos2.getX(), max.getY(), pos2.getZ());
        } else {
            pos1 = new Vector3(pos1.getX(), max.getY(), pos1.getZ());
            pos2 = new Vector3(pos2.getX(), min.getY(), pos2.getZ());
        }
        
        if (pos1.getZ() <= pos2.getZ()) {
            pos1 = new Vector3(pos1.getX(), pos1.getY(), min.getZ());
            pos2 = new Vector3(pos2.getX(), pos2.getY(), max.getZ());
        } else {
            pos1 = new Vector3(pos1.getX(), pos1.getY(), max.getZ());
            pos2 = new Vector3(pos2.getX(), pos2.getY(), min.getZ());
        }
    }
    
    /**
     * Contract the region in the given direction.
     * @param direction The direction (x, y, z)
     * @param amount The amount to contract
     */
    public void contract(Vector3 direction, int amount) {
        expand(new Vector3(-direction.getX(), -direction.getY(), -direction.getZ()), amount);
        
        // Make sure we haven't contracted too far
        int width = max.getX() - min.getX();
        int height = max.getY() - min.getY();
        int length = max.getZ() - min.getZ();
        
        if (width < 1 || height < 1 || length < 1) {
            // Reset to a 1x1x1 region
            int midX = (min.getX() + max.getX()) / 2;
            int midY = (min.getY() + max.getY()) / 2;
            int midZ = (min.getZ() + max.getZ()) / 2;
            
            min = new Vector3(midX, midY, midZ);
            max = new Vector3(midX, midY, midZ);
            
            pos1 = min;
            pos2 = max;
        }
    }
    
    /**
     * Get a block in this region.
     * @param position The position
     * @return The block at the position
     */
    public Block getBlock(Vector3 position) {
        return position.toBlock(world);
    }
    
    @Override
    public Iterator<Vector3> iterator() {
        return new RegionIterator();
    }
    
    /**
     * Iterator for iterating through all positions in the region.
     */
    private class RegionIterator implements Iterator<Vector3> {
        private int nextX;
        private int nextY;
        private int nextZ;
        
        public RegionIterator() {
            nextX = min.getX();
            nextY = min.getY();
            nextZ = min.getZ();
        }
        
        @Override
        public boolean hasNext() {
            return nextX <= max.getX() && nextY <= max.getY() && nextZ <= max.getZ();
        }
        
        @Override
        public Vector3 next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            Vector3 result = new Vector3(nextX, nextY, nextZ);
            
            // Advance to the next position
            nextX++;
            if (nextX > max.getX()) {
                nextX = min.getX();
                nextY++;
                if (nextY > max.getY()) {
                    nextY = min.getY();
                    nextZ++;
                }
            }
            
            return result;
        }
    }
} 