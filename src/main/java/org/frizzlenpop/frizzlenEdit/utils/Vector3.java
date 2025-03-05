package org.frizzlenpop.frizzlenEdit.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * Represents a 3D vector in the Minecraft world.
 * Immutable class for thread safety.
 */
public class Vector3 {
    private final int x;
    private final int y;
    private final int z;
    
    /**
     * Create a new vector with the given coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Create a vector from a Bukkit Block.
     * @param block The block
     * @return A new Vector3
     */
    public static Vector3 fromBlock(Block block) {
        return new Vector3(block.getX(), block.getY(), block.getZ());
    }
    
    /**
     * Create a vector from a Bukkit Location.
     * @param location The location
     * @return A new Vector3
     */
    public static Vector3 fromLocation(Location location) {
        return new Vector3(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Get the X coordinate.
     * @return X coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get the Y coordinate.
     * @return Y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Get the Z coordinate.
     * @return Z coordinate
     */
    public int getZ() {
        return z;
    }
    
    /**
     * Add another vector to this one.
     * @param other The other vector
     * @return A new Vector3 with the sum
     */
    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }
    
    /**
     * Subtract another vector from this one.
     * @param other The other vector
     * @return A new Vector3 with the difference
     */
    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }
    
    /**
     * Multiply this vector by a scalar.
     * @param scalar The scalar value
     * @return A new Vector3 with the product
     */
    public Vector3 multiply(int scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }
    
    /**
     * Get the block at this vector position in the given world.
     * @param world The world
     * @return The block at this position
     */
    public Block toBlock(World world) {
        return world.getBlockAt(x, y, z);
    }
    
    /**
     * Get a Bukkit Location at this vector position in the given world.
     * @param world The world
     * @return A new Location
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }
    
    /**
     * Convert to a Bukkit Vector.
     * @return A new Bukkit Vector
     */
    public Vector toBukkitVector() {
        return new Vector(x, y, z);
    }
    
    /**
     * Calculate the distance to another vector.
     * @param other The other vector
     * @return The distance
     */
    public double distance(Vector3 other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    /**
     * Calculate the squared distance to another vector.
     * This is faster than distance() because it avoids the square root.
     * @param other The other vector
     * @return The squared distance
     */
    public double distanceSquared(Vector3 other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return dx*dx + dy*dy + dz*dz;
    }
    
    /**
     * Get the minimum components of this vector and another.
     * @param other The other vector
     * @return A new Vector3 with the minimum components
     */
    public Vector3 getMinimum(Vector3 other) {
        return new Vector3(
            Math.min(this.x, other.x),
            Math.min(this.y, other.y),
            Math.min(this.z, other.z)
        );
    }
    
    /**
     * Get the maximum components of this vector and another.
     * @param other The other vector
     * @return A new Vector3 with the maximum components
     */
    public Vector3 getMaximum(Vector3 other) {
        return new Vector3(
            Math.max(this.x, other.x),
            Math.max(this.y, other.y),
            Math.max(this.z, other.z)
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Vector3 other = (Vector3) obj;
        return x == other.x && y == other.y && z == other.z;
    }
    
    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
    
    @Override
    public String toString() {
        return "Vector3{" + x + ", " + y + ", " + z + "}";
    }
} 