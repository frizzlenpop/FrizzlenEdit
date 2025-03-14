package org.frizzlenpop.frizzlenEdit.masks;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A mask that combines multiple masks using a logical operation.
 */
public class CompoundMask implements Mask {
    /**
     * The logical operation to use when combining masks.
     */
    public enum Operation {
        AND, // Both masks must match
        OR,  // At least one mask must match
        XOR  // Exactly one mask must match
    }
    
    private final List<Mask> masks;
    private final Operation operation;
    
    /**
     * Create a new compound mask with the AND operation.
     * @param masks The masks to combine
     */
    public CompoundMask(Mask... masks) {
        this(Operation.AND, Arrays.asList(masks));
    }
    
    /**
     * Create a new compound mask with a specific operation.
     * @param operation The logical operation to use
     * @param masks The masks to combine
     */
    public CompoundMask(Operation operation, Mask... masks) {
        this(operation, Arrays.asList(masks));
    }
    
    /**
     * Create a new compound mask with a specific operation.
     * @param operation The logical operation to use
     * @param masks The masks to combine
     */
    public CompoundMask(Operation operation, List<Mask> masks) {
        this.operation = operation;
        this.masks = new ArrayList<>(masks);
    }
    
    /**
     * Add a mask to this compound mask.
     * @param mask The mask to add
     */
    public void addMask(Mask mask) {
        masks.add(mask);
    }
    
    /**
     * Remove a mask from this compound mask.
     * @param mask The mask to remove
     * @return True if the mask was removed
     */
    public boolean removeMask(Mask mask) {
        return masks.remove(mask);
    }
    
    @Override
    public boolean matches(Block block) {
        if (masks.isEmpty()) {
            return false;
        }
        
        switch (operation) {
            case AND:
                // All masks must match
                for (Mask mask : masks) {
                    if (!mask.matches(block)) {
                        return false;
                    }
                }
                return true;
                
            case OR:
                // At least one mask must match
                for (Mask mask : masks) {
                    if (mask.matches(block)) {
                        return true;
                    }
                }
                return false;
                
            case XOR:
                // Exactly one mask must match
                int matches = 0;
                for (Mask mask : masks) {
                    if (mask.matches(block)) {
                        matches++;
                    }
                }
                return matches == 1;
                
            default:
                return false;
        }
    }
    
    @Override
    public String getDescription() {
        if (masks.isEmpty()) {
            return "Empty mask";
        }
        
        String op;
        switch (operation) {
            case AND:
                op = " & ";
                break;
            case OR:
                op = " | ";
                break;
            case XOR:
                op = " ^ ";
                break;
            default:
                op = " ? ";
                break;
        }
        
        return masks.stream()
                .map(Mask::getDescription)
                .collect(Collectors.joining(op, "(", ")"));
    }
    
    /**
     * Create a new compound mask that combines two masks with the AND operation.
     * @param a The first mask
     * @param b The second mask
     * @return A new compound mask
     */
    public static CompoundMask and(Mask a, Mask b) {
        return new CompoundMask(Operation.AND, a, b);
    }
    
    /**
     * Create a new compound mask that combines two masks with the OR operation.
     * @param a The first mask
     * @param b The second mask
     * @return A new compound mask
     */
    public static CompoundMask or(Mask a, Mask b) {
        return new CompoundMask(Operation.OR, a, b);
    }
    
    /**
     * Create a new compound mask that combines two masks with the XOR operation.
     * @param a The first mask
     * @param b The second mask
     * @return A new compound mask
     */
    public static CompoundMask xor(Mask a, Mask b) {
        return new CompoundMask(Operation.XOR, a, b);
    }
} 