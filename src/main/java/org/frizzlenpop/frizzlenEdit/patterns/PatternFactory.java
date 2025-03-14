package org.frizzlenpop.frizzlenEdit.patterns;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.clipboard.ClipboardManager;
import org.frizzlenpop.frizzlenEdit.utils.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;

/**
 * Factory for creating Pattern objects from string descriptors.
 */
public class PatternFactory {
    
    private static final Pattern PERCENT_PATTERN = Pattern.compile("([^,]+?)%([\\d.]+)");
    private static final Pattern NOISE_PATTERN = Pattern.compile("noise\\(([\\d.]+),(.+)\\)");
    private static FrizzlenEdit plugin;
    
    /**
     * Initialize the factory with the plugin instance
     * @param plugin The plugin instance
     */
    public static void init(FrizzlenEdit pluginInstance) {
        plugin = pluginInstance;
    }
    
    /**
     * Parse a pattern string into a Pattern object.
     * 
     * Valid formats:
     * - "stone" - single block pattern
     * - "stone,dirt" - random pattern with equal weights
     * - "stone%75,dirt%25" - random pattern with specified weights
     * - "noise(5,stone,dirt)" - noise pattern with scale 5 and stone/dirt
     * - "clipboard" - clipboard pattern using player's current clipboard
     * 
     * @param player The player for context (clipboard, etc.)
     * @param input The input string
     * @return The created pattern
     * @throws IllegalArgumentException If the input format is invalid
     */
    public static org.frizzlenpop.frizzlenEdit.patterns.Pattern parsePattern(Player player, String input) throws IllegalArgumentException {
        input = input.trim().toLowerCase();
        
        // Handle clipboard pattern
        if (input.equals("clipboard")) {
            ClipboardManager clipboardManager = plugin.getClipboardManager();
            Clipboard clipboard = clipboardManager.getClipboard(player);
            
            if (clipboard == null) {
                throw new IllegalArgumentException("You don't have a clipboard. Copy something first.");
            }
            
            return new ClipboardPattern(clipboard, player.getLocation());
        }
        
        // Check for noise pattern
        Matcher noiseMatcher = NOISE_PATTERN.matcher(input);
        if (noiseMatcher.matches()) {
            double scale = Double.parseDouble(noiseMatcher.group(1));
            String patternString = noiseMatcher.group(2);
            
            NoisePattern noisePattern = new NoisePattern(scale);
            
            // Split the pattern string by commas and add each as a pattern
            String[] parts = patternString.split(",");
            for (String part : parts) {
                org.frizzlenpop.frizzlenEdit.patterns.Pattern subPattern = parsePattern(player, part);
                noisePattern.add(subPattern);
            }
            
            return noisePattern;
        }
        
        // Check if it's a comma-separated list (random pattern)
        if (input.contains(",")) {
            String[] parts = input.split(",");
            
            // If we have multiple parts, create a random pattern
            if (parts.length > 1) {
                RandomPattern randomPattern = new RandomPattern();
                
                for (String part : parts) {
                    part = part.trim();
                    Matcher percentMatcher = PERCENT_PATTERN.matcher(part);
                    
                    if (percentMatcher.matches()) {
                        // Pattern with weight percentage
                        String pattern = percentMatcher.group(1);
                        double percentage = Double.parseDouble(percentMatcher.group(2));
                        
                        randomPattern.add(parsePattern(player, pattern), percentage / 100.0);
                    } else {
                        // Equal weight for each pattern
                        randomPattern.add(parsePattern(player, part), 1.0);
                    }
                }
                
                // Normalize weights
                return randomPattern;
            }
        }
        
        // Default case: single block pattern
        try {
            Material material = Material.matchMaterial(input);
            if (material == null) {
                throw new IllegalArgumentException("Unknown material: " + input);
            }
            
            if (!material.isBlock()) {
                throw new IllegalArgumentException(input + " is not a valid block material");
            }
            
            return new SingleBlockPattern(material.name().toLowerCase());
        } catch (Exception e) {
            Logger.log(Level.WARNING, "Error parsing pattern: " + e.getMessage());
            throw new IllegalArgumentException("Invalid pattern: " + input);
        }
    }
    
    /**
     * Parse a pattern string with additional context options.
     * @param player The player for context
     * @param input The input string
     * @param location Optional location context
     * @return The created pattern
     * @throws IllegalArgumentException If the input format is invalid
     */
    public static org.frizzlenpop.frizzlenEdit.patterns.Pattern parsePattern(Player player, String input, Location location) throws IllegalArgumentException {
        // For now, this just delegates to the main method.
        // In the future, we can use the location for context-specific patterns.
        return parsePattern(player, input);
    }
} 