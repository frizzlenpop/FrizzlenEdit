package org.frizzlenpop.frizzlenEdit.patterns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

/**
 * A pattern that applies blocks from a clipboard.
 */
public class ClipboardPattern implements Pattern {
    private final Clipboard clipboard;
    private final Location pasteLocation;
    private final boolean useOrigin;
    
    /**
     * Create a new clipboard pattern.
     * @param clipboard The clipboard to use as a pattern source
     * @param pasteLocation The location to paste the clipboard at
     * @param useOrigin Whether to use the clipboard's origin for offset calculations
     */
    public ClipboardPattern(Clipboard clipboard, Location pasteLocation, boolean useOrigin) {
        this.clipboard = clipboard;
        this.pasteLocation = pasteLocation;
        this.useOrigin = useOrigin;
    }
    
    /**
     * Create a new clipboard pattern that uses the clipboard's origin.
     * @param clipboard The clipboard to use as a pattern source
     * @param pasteLocation The location to paste the clipboard at
     */
    public ClipboardPattern(Clipboard clipboard, Location pasteLocation) {
        this(clipboard, pasteLocation, true);
    }
    
    @Override
    public BlockData getBlockData(Location location) {
        // Calculate the relative position in the clipboard
        int relX, relY, relZ;
        
        if (useOrigin) {
            Vector3 origin = clipboard.getOrigin();
            relX = location.getBlockX() - pasteLocation.getBlockX() + origin.getX();
            relY = location.getBlockY() - pasteLocation.getBlockY() + origin.getY();
            relZ = location.getBlockZ() - pasteLocation.getBlockZ() + origin.getZ();
        } else {
            relX = location.getBlockX() - pasteLocation.getBlockX();
            relY = location.getBlockY() - pasteLocation.getBlockY();
            relZ = location.getBlockZ() - pasteLocation.getBlockZ();
        }
        
        // Check if the relative position is within the clipboard bounds
        if (relX < 0 || relY < 0 || relZ < 0 || 
            relX >= clipboard.getWidth() || 
            relY >= clipboard.getHeight() || 
            relZ >= clipboard.getLength()) {
            return null;
        }
        
        // Get the block data from the clipboard
        Vector3 position = new Vector3(relX, relY, relZ);
        BlockData blockData = clipboard.getBlock(position);
        
        return blockData;
    }
    
    @Override
    public boolean apply(Block block) {
        BlockData data = getBlockData(block.getLocation());
        if (data == null) {
            return false;
        }
        
        if (!block.getBlockData().equals(data)) {
            block.setBlockData(data);
            return true;
        }
        
        return false;
    }
    
    @Override
    public String getDescription() {
        return "Clipboard";
    }
} 