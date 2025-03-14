package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating Mask objects from string descriptors.
 */
public class MaskFactory {
    
    private static final Pattern COMPOUND_MASK_PATTERN = Pattern.compile("\\s*([^&|^!]+|!\\([^)]+\\)|\\([^)]+\\))\\s*([&|^])?\\s*(.*)");
    private static FrizzlenEdit plugin;
    
    /**
     * Initialize the factory with the plugin instance.
     * @param plugin The plugin instance
     */
    public static void init(FrizzlenEdit pluginInstance) {
        plugin = pluginInstance;
    }
    
    /**
     * Parse a mask string into a Mask object.
     * 
     * Valid formats:
     * - "stone,dirt" - blocks of those types
     * - "#solid" - solid blocks (non-air)
     * - "#air" - air blocks
     * - "!stone" - all blocks except stone
     * - "stone&dirt" - blocks that are both stone and dirt (impossible)
     * - "stone|dirt" - blocks that are either stone or dirt
     * - "stone^dirt" - blocks that are either stone or dirt, but not both
     * - "(stone,dirt)&!sand" - blocks that are stone or dirt, but not sand
     * 
     * @param player The player for context
     * @param input The input string
     * @return The created mask
     * @throws IllegalArgumentException If the input format is invalid
     */
    public static Mask parseMask(Player player, String input) throws IllegalArgumentException {
        input = input.trim();
        
        // Handle empty input
        if (input.isEmpty()) {
            return new SolidMask(); // Default to solid mask
        }
        
        // Handle inverse masks
        if (input.startsWith("!")) {
            if (input.startsWith("!(") && input.endsWith(")")) {
                // Handle inverse of a compound mask: !(mask)
                String innerMask = input.substring(2, input.length() - 1);
                return parseMask(player, innerMask).inverse();
            } else {
                // Handle inverse of a simple mask: !mask
                String innerMask = input.substring(1);
                return parseMask(player, innerMask).inverse();
            }
        }
        
        // Handle compound masks with operations
        Matcher compoundMatcher = COMPOUND_MASK_PATTERN.matcher(input);
        if (compoundMatcher.matches()) {
            String firstPart = compoundMatcher.group(1);
            String operation = compoundMatcher.group(2);
            String remainder = compoundMatcher.group(3);
            
            // If there's no operation, just parse the first part
            if (operation == null || operation.isEmpty()) {
                return parseSingleMask(player, firstPart);
            }
            
            // Parse the first part and the remainder
            Mask firstMask = parseSingleMask(player, firstPart);
            Mask remainderMask = parseMask(player, remainder);
            
            // Combine using the appropriate operation
            switch (operation) {
                case "&":
                    return CompoundMask.and(firstMask, remainderMask);
                case "|":
                    return CompoundMask.or(firstMask, remainderMask);
                case "^":
                    return CompoundMask.xor(firstMask, remainderMask);
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
        }
        
        // If we get here, it's a single mask
        return parseSingleMask(player, input);
    }
    
    /**
     * Parse a single mask (no compound operations).
     * @param player The player for context
     * @param input The input string
     * @return The created mask
     * @throws IllegalArgumentException If the input format is invalid
     */
    private static Mask parseSingleMask(Player player, String input) throws IllegalArgumentException {
        input = input.trim();
        
        // Handle special mask types
        if (input.equals("#solid")) {
            return new SolidMask();
        } else if (input.equals("#air")) {
            return new AirMask();
        }
        
        // Handle parentheses (grouping)
        if (input.startsWith("(") && input.endsWith(")")) {
            return parseMask(player, input.substring(1, input.length() - 1));
        }
        
        // Handle block type mask
        try {
            // Multiple block types separated by commas
            if (input.contains(",")) {
                String[] types = input.split(",");
                List<Material> materials = new ArrayList<>();
                
                for (String type : types) {
                    Material material = Material.matchMaterial(type.trim());
                    if (material == null) {
                        throw new IllegalArgumentException("Unknown material: " + type.trim());
                    }
                    materials.add(material);
                }
                
                return new BlockTypeMask(materials.toArray(new Material[0]));
            } else {
                // Single block type
                Material material = Material.matchMaterial(input);
                if (material == null) {
                    throw new IllegalArgumentException("Unknown material: " + input);
                }
                return new BlockTypeMask(material);
            }
        } catch (Exception e) {
            Logger.log(Level.WARNING, "Error parsing mask: " + e.getMessage());
            throw new IllegalArgumentException("Invalid mask: " + input);
        }
    }
} 