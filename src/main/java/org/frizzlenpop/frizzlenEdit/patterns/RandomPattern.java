package org.frizzlenpop.frizzlenEdit.patterns;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A pattern that returns random block data based on weighted probabilities.
 */
public class RandomPattern implements Pattern {
    
    private final Map<Pattern, Double> patterns;
    private final List<Pattern> patternList;
    private final List<Double> thresholds;
    private final Random random;
    private double totalWeight;
    
    /**
     * Create a new random pattern.
     */
    public RandomPattern() {
        this.patterns = new HashMap<>();
        this.patternList = new ArrayList<>();
        this.thresholds = new ArrayList<>();
        this.random = new Random();
        this.totalWeight = 0;
    }
    
    /**
     * Add a pattern with a specific weight.
     * @param pattern The pattern to add
     * @param weight The weight of the pattern
     * @return This pattern, for chaining
     */
    public RandomPattern add(Pattern pattern, double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }
        
        // Add or update the pattern
        Double oldWeight = patterns.put(pattern, weight);
        
        // If this pattern was already in the map, subtract its old weight
        if (oldWeight != null) {
            totalWeight -= oldWeight;
            
            // Remove it from the lists
            int index = patternList.indexOf(pattern);
            if (index >= 0) {
                patternList.remove(index);
                thresholds.remove(index);
            }
        }
        
        // Add the new weight
        totalWeight += weight;
        
        // Add to the lists
        patternList.add(pattern);
        
        // Rebuild thresholds
        buildThresholds();
        
        return this;
    }
    
    /**
     * Rebuild the threshold list for efficient random selection.
     */
    private void buildThresholds() {
        thresholds.clear();
        double currentThreshold = 0;
        
        for (Pattern pattern : patternList) {
            double weight = patterns.get(pattern);
            currentThreshold += weight / totalWeight;
            thresholds.add(currentThreshold);
        }
    }
    
    @Override
    public BlockData getBlockData(Location location) {
        if (patternList.isEmpty()) {
            throw new IllegalStateException("No patterns added to RandomPattern");
        }
        
        // Get a random number between 0 and 1
        double value = random.nextDouble();
        
        // Find the pattern based on the random value
        for (int i = 0; i < thresholds.size(); i++) {
            if (value <= thresholds.get(i)) {
                return patternList.get(i).getBlockData(location);
            }
        }
        
        // Fallback to the last pattern
        return patternList.get(patternList.size() - 1).getBlockData(location);
    }
    
    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder("Random pattern [");
        
        for (int i = 0; i < patternList.size(); i++) {
            Pattern pattern = patternList.get(i);
            double weight = patterns.get(pattern);
            double percentage = (weight / totalWeight) * 100;
            
            description.append(pattern.getDescription())
                      .append(" (")
                      .append(String.format("%.1f", percentage))
                      .append("%)");
            
            if (i < patternList.size() - 1) {
                description.append(", ");
            }
        }
        
        description.append("]");
        return description.toString();
    }
    
    /**
     * Check if this pattern is empty.
     * @return True if no patterns have been added
     */
    public boolean isEmpty() {
        return patternList.isEmpty();
    }
    
    /**
     * Get the number of patterns in this random pattern.
     * @return The number of patterns
     */
    public int size() {
        return patternList.size();
    }
} 