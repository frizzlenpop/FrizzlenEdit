package org.frizzlenpop.frizzlenEdit.schematic;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.block.data.BlockData;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.clipboard.Clipboard;
import org.frizzlenpop.frizzlenEdit.operations.PasteOperation;
import org.frizzlenpop.frizzlenEdit.operations.BatchPasteOperation;
import org.frizzlenpop.frizzlenEdit.selection.Region;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.Vector3;
import org.frizzlenpop.frizzlenEdit.utils.NBTUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.Map;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.util.HashMap;

/**
 * Manages schematics (saving, loading, listing, etc.).
 */
public class SchematicManager {
    private final FrizzlenEdit plugin;
    private final File schematicsDir;
    
    /**
     * Create a new schematic manager.
     * @param plugin The plugin instance
     */
    public SchematicManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
        
        // Create the schematics directory
        schematicsDir = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        }
    }
    
    /**
     * Save a region as a schematic.
     * @param player The player
     * @param region The region to save
     * @param name The name of the schematic
     */
    public void saveSchematic(Player player, Region region, String name) {
        // Check if the region is too large
        int maxSize = plugin.getConfigManager().getClipboardSizeLimit();
        int volume = region.getVolume();
        
        if (volume > maxSize) {
            player.sendMessage(ChatColor.RED + "Selection too large: " + volume + " blocks. Maximum is " + maxSize + ".");
            return;
        }
        
        // Create a clipboard from the region
        Vector3 origin = Vector3.fromLocation(player.getLocation());
        Clipboard clipboard = new Clipboard(region, origin);
        
        // Copy the blocks asynchronously
        plugin.runAsync(() -> {
            try {
                Logger.info("Player " + player.getName() + " saving schematic " + name + " (" + volume + " blocks)");
                
                // Copy the blocks to the clipboard
                clipboard.copy(region, region.getWorld());
                
                // Save the schematic
                File file = new File(schematicsDir, name + ".schem");
                SchematicFormat.save(clipboard, file);
                
                // Notify the player
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Saved schematic " + name + " (" + volume + " blocks).");
                });
            } catch (Exception e) {
                Logger.severe("Error saving schematic: " + e.getMessage());
                e.printStackTrace();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error saving schematic: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Load a schematic into a player's clipboard.
     * @param player The player
     * @param name The name of the schematic
     */
    public void loadSchematic(Player player, String name) {
        // Check if the schematic exists
        File file = new File(schematicsDir, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic " + name + " does not exist.");
            return;
        }
        
        // Load the schematic asynchronously
        plugin.runAsync(() -> {
            try {
                Logger.info("Player " + player.getName() + " loading schematic " + name);
                
                // Load the schematic
                Clipboard clipboard = SchematicFormat.load(file);
                
                // Set the player's clipboard
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getClipboardManager().setClipboard(player, clipboard);
                    player.sendMessage(ChatColor.GREEN + "Loaded schematic " + name + " (" + clipboard.getVolume() + " blocks).");
                });
            } catch (Exception e) {
                Logger.severe("Error loading schematic: " + e.getMessage());
                e.printStackTrace();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error loading schematic: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * List all available schematics.
     * @param player The player
     */
    public void listSchematics(Player player) {
        // Get all schematic files
        File[] files = schematicsDir.listFiles((dir, name) -> name.endsWith(".schem"));
        
        if (files == null || files.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "No schematics found.");
            return;
        }
        
        // List the schematics
        player.sendMessage(ChatColor.BLUE + "Available schematics:");
        for (File file : files) {
            String name = file.getName().replace(".schem", "");
            player.sendMessage(ChatColor.GRAY + "- " + name);
        }
    }
    
    /**
     * Delete a schematic.
     * @param player The player
     * @param name The name of the schematic
     */
    public void deleteSchematic(Player player, String name) {
        // Check if the schematic exists
        File file = new File(schematicsDir, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic " + name + " does not exist.");
            return;
        }
        
        // Delete the schematic
        if (file.delete()) {
            player.sendMessage(ChatColor.GREEN + "Deleted schematic " + name + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to delete schematic " + name + ".");
        }
    }
    
    /**
     * Get a list of all available schematics.
     * @return A list of schematic names
     */
    public List<String> getSchematicList() {
        List<String> result = new ArrayList<>();
        
        // Get all schematic files
        File[] files = schematicsDir.listFiles((dir, name) -> name.endsWith(".schem"));
        
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".schem", "");
                result.add(name);
            }
        }
        
        return result;
    }
    
    /**
     * Shutdown the schematic manager.
     */
    public void shutdown() {
        // Nothing to do here for now
    }
    
    /**
     * Paste a schematic at a specific location.
     * @param player The player
     * @param name The name of the schematic
     * @param position The position to paste at
     * @param ignoreAir Whether to ignore air blocks
     */
    public void pasteSchematic(Player player, String name, Vector3 position, boolean ignoreAir) {
        // Check if the schematic exists
        File file = new File(schematicsDir, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic " + name + " does not exist.");
            return;
        }
        
        // Load and paste the schematic asynchronously
        plugin.runAsync(() -> {
            try {
                Logger.info("Player " + player.getName() + " pasting schematic " + name);
                
                // Load the schematic
                Clipboard clipboard = SchematicFormat.load(file);
                
                // Create and execute a paste operation on the main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        // Create a paste operation
                        PasteOperation operation = new PasteOperation(player, position, clipboard, ignoreAir);
                        
                        // Execute the operation
                        plugin.getOperationManager().execute(player, operation);
                        
                        player.sendMessage(ChatColor.GREEN + "Pasted schematic " + name + " at your location.");
                    } catch (Exception e) {
                        Logger.severe("Error pasting schematic: " + e.getMessage());
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Error pasting schematic: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Logger.severe("Error loading schematic for paste: " + e.getMessage());
                e.printStackTrace();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error loading schematic for paste: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Paste a schematic at a specific location using batch processing for large schematics.
     * This will split the operation into multiple batches to minimize server impact.
     * 
     * @param player The player
     * @param name The name of the schematic
     * @param position The position to paste at
     * @param ignoreAir Whether to ignore air blocks
     * @param batchSize The number of blocks to process in each batch
     * @param tickDelay The number of server ticks to wait between batches
     */
    public void batchPasteSchematic(Player player, String name, Vector3 position, 
                                   boolean ignoreAir, int batchSize, int tickDelay) {
        // Check if the schematic exists
        File file = new File(schematicsDir, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic " + name + " does not exist.");
            return;
        }
        
        // Load and paste the schematic asynchronously
        plugin.runAsync(() -> {
            try {
                Logger.info("Player " + player.getName() + " batch pasting schematic " + name);
                
                // Load the schematic
                Clipboard clipboard = SchematicFormat.load(file);
                
                // Create and execute a batch paste operation on the main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        // Create a batch paste operation
                        BatchPasteOperation operation = new BatchPasteOperation(
                            plugin, player, position, clipboard, ignoreAir, batchSize, tickDelay, "Batch Paste Schematic"
                        );
                        
                        // Execute the operation
                        plugin.getOperationManager().execute(player, operation);
                        
                    } catch (Exception e) {
                        Logger.severe("Error batch pasting schematic: " + e.getMessage());
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Error batch pasting schematic: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Logger.severe("Error loading schematic for batch paste: " + e.getMessage());
                e.printStackTrace();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error loading schematic for batch paste: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Paste a schematic at a specific location using batch processing with automatic
     * performance optimization based on server TPS.
     * 
     * The batch size and tick delay will automatically adjust based on server performance
     * to maintain optimal TPS while pasting as quickly as possible.
     * 
     * @param player The player
     * @param name The name of the schematic
     * @param position The position to paste at
     * @param ignoreAir Whether to ignore air blocks
     * @param initialBatchSize Initial batch size (optional, defaults to config value)
     * @param initialDelay Initial tick delay (optional, defaults to config value)
     */
    public void adaptivePasteSchematic(Player player, String name, Vector3 position, boolean ignoreAir) {
        adaptivePasteSchematic(player, name, position, ignoreAir, 
                              plugin.getConfigManager().getBatchPasteSize(),
                              plugin.getConfigManager().getBatchPasteDelay());
    }
    
    /**
     * Paste a schematic at a specific location using batch processing with automatic
     * performance optimization based on server TPS.
     * 
     * The batch size and tick delay will automatically adjust based on server performance
     * to maintain optimal TPS while pasting as quickly as possible.
     * 
     * @param player The player
     * @param name The name of the schematic
     * @param position The position to paste at
     * @param ignoreAir Whether to ignore air blocks
     * @param initialBatchSize Initial batch size
     * @param initialDelay Initial tick delay
     */
    public void adaptivePasteSchematic(Player player, String name, Vector3 position, 
                                      boolean ignoreAir, int initialBatchSize, int initialDelay) {
        // Check if the schematic exists
        File file = new File(schematicsDir, name + ".schem");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic " + name + " does not exist.");
            return;
        }
        
        // Load and paste the schematic asynchronously
        plugin.runAsync(() -> {
            try {
                Logger.info("Player " + player.getName() + " adaptive pasting schematic " + name);
                
                // Load the schematic
                Clipboard clipboard = SchematicFormat.load(file);
                
                // Create and execute a batch paste operation on the main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        // Create a batch paste operation
                        BatchPasteOperation operation = new BatchPasteOperation(
                            plugin, player, position, clipboard, ignoreAir, initialBatchSize, initialDelay, 
                            "Adaptive Paste Schematic"
                        );
                        
                        // Execute the operation
                        plugin.getOperationManager().execute(player, operation);
                        
                    } catch (Exception e) {
                        Logger.severe("Error adaptive pasting schematic: " + e.getMessage());
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Error adaptive pasting schematic: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Logger.severe("Error loading schematic for adaptive paste: " + e.getMessage());
                e.printStackTrace();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error loading schematic for adaptive paste: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Static class for schematic file format handling.
     */
    private static class SchematicFormat {
        /**
         * Save a clipboard to a file.
         * @param clipboard The clipboard
         * @param file The file
         * @throws IOException If an error occurs
         */
        public static void save(Clipboard clipboard, File file) throws IOException {
            // Implementation of the Sponge Schematic format (version 2)
            // https://github.com/SpongePowered/Schematic-Specification
            
            try (FileOutputStream fos = new FileOutputStream(file);
                 GZIPOutputStream gzos = new GZIPOutputStream(fos);
                 DataOutputStream dos = new DataOutputStream(gzos)) {
                
                // Write the root compound tag
                dos.writeByte(10); // TAG_COMPOUND
                writeString(dos, "Schematic"); // Root tag name
                
                // Write the schematic version
                writeByte(dos, "Version", (byte) 2);
                
                // Write the data version (Minecraft 1.16.5 = 2586)
                writeInt(dos, "DataVersion", 2586);
                
                // Write the dimensions
                writeShort(dos, "Width", (short) clipboard.getWidth());
                writeShort(dos, "Height", (short) clipboard.getHeight());
                writeShort(dos, "Length", (short) clipboard.getLength());
                
                // Build a palette of all unique block states
                Map<BlockData, Integer> palette = new java.util.HashMap<>();
                Map<String, Integer> blockStatePalette = new java.util.HashMap<>();
                int nextId = 0;
                
                for (Map.Entry<Vector3, BlockData> entry : clipboard.getBlocks().entrySet()) {
                    BlockData data = entry.getValue();
                    if (!palette.containsKey(data)) {
                        String blockState = data.getAsString();
                        palette.put(data, nextId);
                        blockStatePalette.put(blockState, nextId);
                        nextId++;
                    }
                }
                
                // Write the palette
                writeCompoundStart(dos, "Palette");
                for (Map.Entry<String, Integer> entry : blockStatePalette.entrySet()) {
                    writeInt(dos, entry.getKey(), entry.getValue());
                }
                dos.writeByte(0); // TAG_END
                
                // Prepare the block data array
                int totalBlocks = clipboard.getWidth() * clipboard.getHeight() * clipboard.getLength();
                ByteArrayOutputStream baos = new ByteArrayOutputStream(totalBlocks);
                DataOutputStream blockDataOut = new DataOutputStream(baos);
                
                // Write block data using VarInt encoding
                for (int y = 0; y < clipboard.getHeight(); y++) {
                    for (int z = 0; z < clipboard.getLength(); z++) {
                        for (int x = 0; x < clipboard.getWidth(); x++) {
                            Vector3 pos = new Vector3(x, y, z);
                            BlockData data = clipboard.getBlock(pos);
                            
                            int paletteId = 0; // Default to air
                            if (data != null) {
                                paletteId = palette.getOrDefault(data, 0);
                            }
                            
                            // Write the palette ID as a VarInt
                            writeVarInt(blockDataOut, paletteId);
                        }
                    }
                }
                
                // Write the block data
                byte[] blockData = baos.toByteArray();
                writeByteArray(dos, "BlockData", blockData);
                
                // End the root compound
                dos.writeByte(0); // TAG_END
                
                Logger.info("Saved schematic with dimensions " + clipboard.getWidth() + "x" + 
                           clipboard.getHeight() + "x" + clipboard.getLength() + 
                           " (" + clipboard.getVolume() + " blocks)");
            }
        }
        
        // Helper methods for writing NBT data
        
        private static void writeString(DataOutputStream dos, String value) throws IOException {
            byte[] bytes = value.getBytes("UTF-8");
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        
        private static void writeByte(DataOutputStream dos, String name, byte value) throws IOException {
            dos.writeByte(1); // TAG_BYTE
            writeString(dos, name);
            dos.writeByte(value);
        }
        
        private static void writeShort(DataOutputStream dos, String name, short value) throws IOException {
            dos.writeByte(2); // TAG_SHORT
            writeString(dos, name);
            dos.writeShort(value);
        }
        
        private static void writeInt(DataOutputStream dos, String name, int value) throws IOException {
            dos.writeByte(3); // TAG_INT
            writeString(dos, name);
            dos.writeInt(value);
        }
        
        private static void writeByteArray(DataOutputStream dos, String name, byte[] value) throws IOException {
            dos.writeByte(7); // TAG_BYTE_ARRAY
            writeString(dos, name);
            dos.writeInt(value.length);
            dos.write(value);
        }
        
        private static void writeCompoundStart(DataOutputStream dos, String name) throws IOException {
            dos.writeByte(10); // TAG_COMPOUND
            writeString(dos, name);
        }
        
        private static void writeVarInt(DataOutputStream dos, int value) throws IOException {
            while (true) {
                if ((value & ~0x7F) == 0) {
                    dos.writeByte(value);
                    return;
                }
                
                dos.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
        
        /**
         * Load a clipboard from a file.
         * @param file The file
         * @return The clipboard
         * @throws IOException If an error occurs
         */
        public static Clipboard load(File file) throws IOException {
            // Read the schematic file using the NBT parser
            Map<String, Object> schematicData = NBTUtils.readSchematic(file);
            
            // Extract dimensions with type checking
            int width = getIntValue(schematicData, "Width");
            int height = getIntValue(schematicData, "Height");
            int length = getIntValue(schematicData, "Length");
            
            // Get the palette (maps block states to indices)
            @SuppressWarnings("unchecked")
            Map<String, Object> rawPalette = (Map<String, Object>) schematicData.get("Palette");
            Map<String, Integer> palette = NBTUtils.parsePalette(rawPalette);
            
            // Create a reversed palette (maps indices to block states)
            Map<Integer, String> reversedPalette = NBTUtils.reversePalette(palette);
            
            // Extract the block data (stored as a byte array of indices)
            byte[] blockData = (byte[]) schematicData.get("BlockData");
            
            // Create a clipboard with the dimensions
            Vector3 origin = new Vector3(0, 0, 0);
            Clipboard clipboard = new Clipboard(origin, width, height, length);
            
            // Process block data and add blocks to the clipboard
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int blockId = blockData[index] & 0xFF;
                        
                        // Handle VarInt encoding for block IDs
                        int varIntShift = 7;
                        while ((blockData[index] & 0x80) != 0) {
                            index++;
                            blockId |= (blockData[index] & 0x7F) << varIntShift;
                            varIntShift += 7;
                        }
                        
                        index++;
                        
                        // Get the block state from the palette
                        String blockState = reversedPalette.get(blockId);
                        if (blockState != null) {
                            // Convert the block state to Bukkit BlockData
                            BlockData data = NBTUtils.parseBlockState(blockState);
                            
                            // Add the block to the clipboard
                            clipboard.setBlock(new Vector3(x, y, z), data);
                        }
                    }
                }
            }
            
            Logger.info("Loaded schematic with dimensions " + width + "x" + height + "x" + length + 
                       " (" + clipboard.getVolume() + " blocks)");
            
            return clipboard;
        }
        
        /**
         * Helper method to get an int value from a map, handling different number types.
         * 
         * @param map The map containing the data
         * @param key The key to look up
         * @return The int value
         * @throws IOException If the key does not exist or is not a number
         */
        private static int getIntValue(Map<String, Object> map, String key) throws IOException {
            if (!map.containsKey(key)) {
                throw new IOException("Missing required field: " + key);
            }
            
            Object value = map.get(key);
            
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Short) {
                return ((Short) value).intValue();
            } else if (value instanceof Byte) {
                return ((Byte) value).intValue();
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else {
                throw new IOException("Field " + key + " is not a number");
            }
        }
    }
} 