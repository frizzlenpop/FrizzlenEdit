package org.frizzlenpop.frizzlenEdit.utils;

import java.util.Random;

/**
 * Utility class for generating Perlin-like noise for terrain operations.
 */
public class NoiseGenerator {
    private final Random random;
    private final long seed;
    private double persistence = 0.5;
    private double frequency = 1.0;
    private int octaves = 4;
    
    /**
     * Create a new noise generator with a random seed.
     */
    public NoiseGenerator() {
        this(new Random().nextLong());
    }
    
    /**
     * Create a new noise generator with a specified seed.
     * @param seed The seed for the noise generator
     */
    public NoiseGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    /**
     * Create a new noise generator with custom parameters.
     * @param seed The seed for the noise generator
     * @param persistence Controls how quickly the amplitudes diminish
     * @param frequency Controls how quickly the noise changes
     * @param octaves Number of octaves used to generate the noise
     */
    public NoiseGenerator(long seed, double persistence, double frequency, int octaves) {
        this.seed = seed;
        this.random = new Random(seed);
        this.persistence = persistence;
        this.frequency = frequency;
        this.octaves = octaves;
    }
    
    /**
     * Generate 2D noise at a position.
     * @param x X coordinate
     * @param y Y coordinate
     * @return A noise value between -1 and 1
     */
    public double noise(double x, double y) {
        return noise(x, y, 0);
    }
    
    /**
     * Generate 3D noise at a position.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return A noise value between -1 and 1
     */
    public double noise(double x, double y, double z) {
        double total = 0;
        double amplitude = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            // Add successively smaller noise components
            total += smoothNoise(x * frequency, y * frequency, z * frequency) * amplitude;
            
            maxValue += amplitude;
            amplitude *= persistence;
            x *= 2;
            y *= 2;
            z *= 2;
        }
        
        // Normalize the result
        return total / maxValue;
    }
    
    /**
     * Generate smooth noise at a position.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return A noise value between -1 and 1
     */
    private double smoothNoise(double x, double y, double z) {
        // Get integer parts of coordinates
        int intX = (int) Math.floor(x);
        int intY = (int) Math.floor(y);
        int intZ = (int) Math.floor(z);
        
        // Get fractional parts of coordinates
        double fracX = x - intX;
        double fracY = y - intY;
        double fracZ = z - intZ;
        
        // Smoothing function
        double sx = fade(fracX);
        double sy = fade(fracY);
        double sz = fade(fracZ);
        
        // Interpolate between grid point values
        return lerp(
                lerp(
                        lerp(getValue(intX, intY, intZ), getValue(intX + 1, intY, intZ), sx),
                        lerp(getValue(intX, intY + 1, intZ), getValue(intX + 1, intY + 1, intZ), sx),
                        sy),
                lerp(
                        lerp(getValue(intX, intY, intZ + 1), getValue(intX + 1, intY, intZ + 1), sx),
                        lerp(getValue(intX, intY + 1, intZ + 1), getValue(intX + 1, intY + 1, intZ + 1), sx),
                        sy),
                sz);
    }
    
    /**
     * Get a deterministic value for a position.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return A value between -1 and 1
     */
    private double getValue(int x, int y, int z) {
        // Combine the coordinates and seed for a deterministic hash
        long hash = (x * 73856093) ^ (y * 19349663) ^ (z * 83492791) ^ seed;
        
        // Deterministic random value
        Random valueRandom = new Random(hash);
        return valueRandom.nextDouble() * 2 - 1; // Range -1 to 1
    }
    
    /**
     * Smooth transition function.
     * @param t Input value (0 to 1)
     * @return Smoothed value
     */
    private double fade(double t) {
        // Improved smoothing function: 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    /**
     * Linear interpolation between two values.
     * @param a First value
     * @param b Second value
     * @param t Interpolation factor (0 to 1)
     * @return Interpolated value
     */
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    
    /**
     * Get the seed used by this generator.
     * @return The seed
     */
    public long getSeed() {
        return seed;
    }
} 