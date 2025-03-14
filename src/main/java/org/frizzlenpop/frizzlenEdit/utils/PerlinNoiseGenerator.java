package org.frizzlenpop.frizzlenEdit.utils;

import java.util.Random;

/**
 * Implementation of the Improved Perlin Noise algorithm.
 * Based on Ken Perlin's reference implementation.
 */
public class PerlinNoiseGenerator {
    
    private final int[] permutation;
    private final int[] p;
    
    /**
     * Create a new Perlin noise generator with the specified seed.
     * @param seed The seed for the random number generator
     */
    public PerlinNoiseGenerator(long seed) {
        Random random = new Random(seed);
        permutation = new int[512];
        p = new int[512];
        
        // Initialize the permutation array with values 0-255
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        
        // Shuffle the permutation array
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        
        // Duplicate the permutation array
        for (int i = 0; i < 256; i++) {
            p[i] = p[i + 256] = permutation[i];
        }
    }
    
    /**
     * Generate 3D Perlin noise for the specified coordinates.
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return Noise value between -1 and 1
     */
    public double noise(double x, double y, double z) {
        // Find unit cube that contains the point
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;
        
        // Find relative x, y, z of point in cube
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        
        // Compute fade curves for each coordinate
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        
        // Hash coordinates of the 8 cube corners
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;
        
        // Add blended results from 8 corners of cube
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                                     grad(p[BA], x - 1, y, z)),
                             lerp(u, grad(p[AB], x, y - 1, z),
                                     grad(p[BB], x - 1, y - 1, z))),
                     lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                                     grad(p[BA + 1], x - 1, y, z - 1)),
                             lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                                     grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }
    
    /**
     * Fade function as defined by Ken Perlin.
     * This eases coordinate values so that they will ease towards integral values.
     * @param t The input value
     * @return The faded value
     */
    private double fade(double t) {
        // 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    /**
     * Linear interpolation between a and b by amount t.
     * @param t The blend amount (between 0 and 1)
     * @param a The first value
     * @param b The second value
     * @return The interpolated value
     */
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
    
    /**
     * Compute the dot product of the gradient vector and the distance vector.
     * @param hash The hash value
     * @param x The x distance
     * @param y The y distance
     * @param z The z distance
     * @return The dot product
     */
    private double grad(int hash, double x, double y, double z) {
        // Convert low 4 bits of hash code into 12 gradient directions
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
} 