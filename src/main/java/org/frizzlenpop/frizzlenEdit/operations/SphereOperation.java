package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.masks.Mask;
import org.frizzlenpop.frizzlenEdit.patterns.Pattern;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * Operation that creates a sphere of blocks.
 */
public class SphereOperation extends AbstractOperation {
    private final Vector3 center;
    private final int radius;
    private final Pattern pattern;
    private final boolean hollow;
    
    /**
     * Creates a new sphere operation.
     * @param player The player executing the operation
     * @param center The center of the sphere
     * @param radius The radius of the sphere
     * @param pattern The pattern to use for the sphere
     * @param hollow Whether the sphere should be hollow
     */
    public SphereOperation(Player player, Vector3 center, int radius, Pattern pattern, boolean hollow) {
        super(player);
        this.center = center;
        this.radius = Math.max(1, radius); // Ensure radius is at least 1
        this.pattern = pattern;
        this.hollow = hollow;
    }
    
    /**
     * Creates a new sphere operation with a mask.
     * @param player The player executing the operation
     * @param center The center of the sphere
     * @param radius The radius of the sphere
     * @param pattern The pattern to use for the sphere
     * @param hollow Whether the sphere should be hollow
     * @param mask The mask to apply
     */
    public SphereOperation(Player player, Vector3 center, int radius, Pattern pattern, boolean hollow, Mask mask) {
        super(player, mask);
        this.center = center;
        this.radius = Math.max(1, radius); // Ensure radius is at least 1
        this.pattern = pattern;
        this.hollow = hollow;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        HistoryEntry entry = createHistoryEntry(world);
        int affected = 0;
        
        // Create a bounding box to iterate over
        int radiusSquared = radius * radius;
        int hollowRadiusSquared = (radius - 1) * (radius - 1);
        
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    // Calculate distance squared from center
                    double distanceSquared = 
                            Math.pow(x - center.getX(), 2) + 
                            Math.pow(y - center.getY(), 2) + 
                            Math.pow(z - center.getZ(), 2);
                    
                    // Check if the point is within the sphere
                    if (distanceSquared <= radiusSquared) {
                        // For hollow spheres, only points at the edge are included
                        if (!hollow || distanceSquared >= hollowRadiusSquared) {
                            Vector3 pos = new Vector3(x, y, z);
                            Block block = world.getBlockAt(x, y, z);
                            
                            // Check if the block matches the mask
                            if (!matchesMask(block)) {
                                continue;
                            }
                            
                            BlockState oldState = block.getState();
                            BlockData newData = pattern.getBlockData(block.getLocation());
                            
                            if (newData != null && !oldState.getBlockData().equals(newData)) {
                                // Save the old state for undo
                                entry.addBlockState(pos, oldState, null);
                                
                                // Set the new block data
                                block.setBlockData(newData);
                                
                                // Save the new state for redo
                                entry.addBlockState(pos, null, block.getState());
                                
                                affected++;
                            }
                        }
                    }
                }
            }
        }
        
        sendMessage("Created a " + (hollow ? "hollow " : "") + "sphere with " + affected + " blocks.");
        return entry;
    }
    
    @Override
    public String getDescription() {
        String desc = (hollow ? "Hollow " : "") + "Sphere (radius=" + radius + ", pattern=" + pattern.getDescription();
        if (mask != null) {
            desc += ", mask=" + mask.getDescription();
        }
        desc += ")";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // Approximate volume of the sphere
        if (hollow) {
            // For hollow spheres, subtract the volume of the inner sphere
            double outerVolume = (4.0/3.0) * Math.PI * Math.pow(radius, 3);
            double innerVolume = (4.0/3.0) * Math.PI * Math.pow(radius - 1, 3);
            return (int) (outerVolume - innerVolume);
        } else {
            return (int) ((4.0/3.0) * Math.PI * Math.pow(radius, 3));
        }
    }
} 