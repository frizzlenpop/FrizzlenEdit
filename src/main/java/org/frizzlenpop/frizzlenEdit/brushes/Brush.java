package org.frizzlenpop.frizzlenEdit.brushes;

import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Represents a brush tool that can be used to modify blocks.
 */
public interface Brush {
    /**
     * Use this brush at a position.
     * @param player The player using the brush
     * @param position The position to use the brush at
     * @param mask The mask to apply, or null for no mask
     */
    void use(Player player, Vector3 position, String mask);
    
    /**
     * Get the radius of this brush.
     * @return The radius
     */
    int getRadius();
    
    /**
     * Get a description of this brush.
     * @return The description
     */
    String getDescription();
} 