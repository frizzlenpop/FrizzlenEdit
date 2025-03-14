package org.frizzlenpop.frizzlenEdit.operations;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.history.HistoryEntry;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Operation that regenerates chunks back to their original biome state.
 */
public class ChunkRegenerationOperation extends AbstractOperation {
    private final Region region;
    private final boolean keepEntities;
    private final boolean keepStructures;
    private final FrizzlenEdit plugin;
    
    /**
     * Creates a new chunk regeneration operation.
     * @param player The player executing the operation
     * @param region The region containing chunks to regenerate
     * @param keepEntities Whether to preserve entities in the regenerated chunks
     * @param keepStructures Whether to preserve structures in the regenerated chunks
     * @param plugin The plugin instance
     */
    public ChunkRegenerationOperation(Player player, Region region, boolean keepEntities, boolean keepStructures, FrizzlenEdit plugin) {
        super(player);
        this.region = region;
        this.keepEntities = keepEntities;
        this.keepStructures = keepStructures;
        this.plugin = plugin;
    }
    
    @Override
    public HistoryEntry execute() {
        World world = player.getWorld();
        
        // Create a set to store chunk coordinates
        Set<ChunkCoord> chunkCoords = new HashSet<>();
        
        // Get region bounds
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        
        // Determine affected chunks
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                int chunkX = x >> 4; // Divide by 16
                int chunkZ = z >> 4; // Divide by 16
                chunkCoords.add(new ChunkCoord(chunkX, chunkZ));
            }
        }
        
        // Information message
        sendMessage("Regenerating " + chunkCoords.size() + " chunks...");
        
        // Creating a history entry for this operation is complicated since 
        // chunk regeneration affects a very large number of blocks.
        // We'll create a limited history entry capturing just the surface blocks.
        HistoryEntry entry = createHistoryEntry(world);
        
        // Capture surface states of important blocks
        captureSurfaceBlocks(world, chunkCoords, entry);
        
        // Store entities if needed
        List<StoredEntity> storedEntities = new ArrayList<>();
        if (keepEntities) {
            storeEntities(world, chunkCoords, storedEntities);
        }
        
        // Store structures not implemented (would require NBT data access)
        
        // Regenerate each chunk
        int regenerated = 0;
        for (ChunkCoord coord : chunkCoords) {
            try {
                Chunk chunk = world.getChunkAt(coord.x, coord.z);
                
                // Unload the chunk first
                boolean wasLoaded = chunk.isLoaded();
                if (wasLoaded) {
                    chunk.unload(true);
                }
                
                // Regenerate the chunk (Bukkit API will do generation)
                world.regenerateChunk(coord.x, coord.z);
                
                // Restore entities if needed
                if (keepEntities) {
                    restoreEntities(world, coord, storedEntities);
                }
                
                // Ensure chunk is in the same loaded state as before
                if (!wasLoaded) {
                    chunk.unload(true);
                }
                
                regenerated++;
            } catch (Exception e) {
                Logger.log(Level.WARNING, "Error regenerating chunk " + coord.x + "," + coord.z + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        sendMessage("Regenerated " + regenerated + " chunks.");
        
        return entry;
    }
    
    /**
     * Capture the surface blocks for a set of chunks.
     * @param world The world
     * @param chunkCoords The chunk coordinates
     * @param entry The history entry
     */
    private void captureSurfaceBlocks(World world, Set<ChunkCoord> chunkCoords, HistoryEntry entry) {
        // For each chunk, capture the top few blocks of certain types
        // that are important for players (chests, spawners, etc.)
        int capturedBlocks = 0;
        
        for (ChunkCoord coord : chunkCoords) {
            // Get the chunk
            Chunk chunk = world.getChunkAt(coord.x, coord.z);
            
            // Determine the chunk bounds
            int minX = coord.x << 4; // Multiply by 16
            int minZ = coord.z << 4; // Multiply by 16
            int maxX = minX + 15;
            int maxZ = minZ + 15;
            
            // Iterate through the surface of the chunk
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Find the highest block
                    int maxY = world.getHighestBlockYAt(x, z);
                    
                    // Capture a few blocks below the surface
                    for (int y = maxY; y > maxY - 5 && y > 0; y--) {
                        Block block = world.getBlockAt(x, y, z);
                        
                        // Only capture important blocks to avoid overloading the history
                        if (isImportantBlock(block)) {
                            BlockState oldState = block.getState();
                            Vector3 pos = new Vector3(x, y, z);
                            
                            // Save the old state for undo
                            entry.addBlockState(pos, oldState, null);
                            
                            capturedBlocks++;
                            
                            // Limit the number of blocks captured per chunk
                            if (capturedBlocks >= 1000) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        Logger.debug("Captured " + capturedBlocks + " important blocks for history.");
    }
    
    /**
     * Check if a block is important for the player.
     * @param block The block to check
     * @return True if the block is important
     */
    private boolean isImportantBlock(Block block) {
        switch (block.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
            case ENDER_CHEST:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
            case DISPENSER:
            case DROPPER:
            case HOPPER:
            case SPAWNER:
            case BEACON:
            case BREWING_STAND:
            case ENCHANTING_TABLE:
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case SHULKER_BOX:
            case BARREL:
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
            case LODESTONE:
            case RESPAWN_ANCHOR:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Store entities from chunks for later restoration.
     * @param world The world
     * @param chunkCoords The chunk coordinates
     * @param storedEntities List to store entities in
     */
    private void storeEntities(World world, Set<ChunkCoord> chunkCoords, List<StoredEntity> storedEntities) {
        for (ChunkCoord coord : chunkCoords) {
            Chunk chunk = world.getChunkAt(coord.x, coord.z);
            
            // Get all entities in the chunk
            for (Entity entity : chunk.getEntities()) {
                // Skip players
                if (entity instanceof Player) {
                    continue;
                }
                
                // Store relevant entity data
                storedEntities.add(new StoredEntity(
                    entity.getType(),
                    entity.getLocation(),
                    entity.getUniqueId(),
                    new ChunkCoord(coord.x, coord.z)
                ));
            }
        }
        
        Logger.debug("Stored " + storedEntities.size() + " entities for restoration.");
    }
    
    /**
     * Restore entities to a chunk after regeneration.
     * Note: This is a simplified implementation. A complete implementation would
     * need to use NMS code to copy all entity NBT data.
     * 
     * @param world The world
     * @param chunkCoord The chunk coordinates
     * @param storedEntities List of stored entities
     */
    private void restoreEntities(World world, ChunkCoord chunkCoord, List<StoredEntity> storedEntities) {
        // Filter entities for this chunk
        List<StoredEntity> chunkEntities = new ArrayList<>();
        for (StoredEntity entity : storedEntities) {
            if (entity.chunk.equals(chunkCoord)) {
                chunkEntities.add(entity);
            }
        }
        
        // Spawn filtered entities
        for (StoredEntity entity : chunkEntities) {
            try {
                world.spawnEntity(entity.location, entity.type);
            } catch (Exception e) {
                Logger.log(Level.WARNING, "Failed to restore entity: " + e.getMessage());
            }
        }
    }
    
    @Override
    public String getDescription() {
        String desc = "Chunk Regeneration (region=" + region.getVolume() + " blocks, ";
        
        if (keepEntities) {
            desc += "keeping entities, ";
        }
        
        if (keepStructures) {
            desc += "keeping structures, ";
        }
        
        // Count chunks
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        int minChunkX = min.getX() >> 4;
        int minChunkZ = min.getZ() >> 4;
        int maxChunkX = max.getX() >> 4;
        int maxChunkZ = max.getZ() >> 4;
        int chunkCount = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        
        desc += chunkCount + " chunks)";
        return desc;
    }
    
    @Override
    public int getVolume() {
        // This is an approximation, assuming chunks are 16x256x16
        Vector3 min = region.getMinimumPoint();
        Vector3 max = region.getMaximumPoint();
        int minChunkX = min.getX() >> 4;
        int minChunkZ = min.getZ() >> 4;
        int maxChunkX = max.getX() >> 4;
        int maxChunkZ = max.getZ() >> 4;
        int chunkCount = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        
        return chunkCount * 16 * 256 * 16;
    }
    
    /**
     * Simple class to store chunk coordinates.
     */
    private static class ChunkCoord {
        public final int x;
        public final int z;
        
        public ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ChunkCoord that = (ChunkCoord) obj;
            return x == that.x && z == that.z;
        }
        
        @Override
        public int hashCode() {
            return 31 * x + z;
        }
    }
    
    /**
     * Simple class to store entity data for restoration.
     */
    private static class StoredEntity {
        public final org.bukkit.entity.EntityType type;
        public final org.bukkit.Location location;
        public final java.util.UUID uuid;
        public final ChunkCoord chunk;
        
        public StoredEntity(org.bukkit.entity.EntityType type, org.bukkit.Location location, java.util.UUID uuid, ChunkCoord chunk) {
            this.type = type;
            this.location = location;
            this.uuid = uuid;
            this.chunk = chunk;
        }
    }
} 