package org.frizzlenpop.frizzlenEdit.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for working with NBT data, particularly for schematic files.
 */
public class NBTUtils {
    
    // NBT tag types
    private static final byte TAG_END = 0;
    private static final byte TAG_BYTE = 1;
    private static final byte TAG_SHORT = 2;
    private static final byte TAG_INT = 3;
    private static final byte TAG_LONG = 4;
    private static final byte TAG_FLOAT = 5;
    private static final byte TAG_DOUBLE = 6;
    private static final byte TAG_BYTE_ARRAY = 7;
    private static final byte TAG_STRING = 8;
    private static final byte TAG_LIST = 9;
    private static final byte TAG_COMPOUND = 10;
    private static final byte TAG_INT_ARRAY = 11;
    private static final byte TAG_LONG_ARRAY = 12;
    
    /**
     * Read a schematic file and extract the necessary data.
     * 
     * @param file The schematic file to read
     * @return A map containing the extracted data
     * @throws IOException If an error occurs while reading the file
     */
    public static Map<String, Object> readSchematic(File file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             DataInputStream dis = new DataInputStream(gzis)) {
            
            // Read the root compound tag
            byte tagType = dis.readByte();
            if (tagType != TAG_COMPOUND) {
                throw new IOException("Invalid schematic file: Root tag is not a compound tag");
            }
            
            // Read the root tag name (should be "Schematic")
            String rootName = readString(dis);
            if (!rootName.equals("Schematic")) {
                throw new IOException("Invalid schematic file: Root tag name is not 'Schematic'");
            }
            
            // Read the compound tag
            readCompound(dis, result);
            
            // Verify version - more flexible check to support different schematic versions
            if (result.containsKey("Version")) {
                Object versionObj = result.get("Version");
                int version;
                
                // Handle different number types that might be used for Version
                if (versionObj instanceof Integer) {
                    version = (Integer) versionObj;
                } else if (versionObj instanceof Short) {
                    version = ((Short) versionObj).intValue();
                } else if (versionObj instanceof Byte) {
                    version = ((Byte) versionObj).intValue();
                } else {
                    version = 0; // Unknown format
                }
                
                // For now, we'll support both version 1 and 2 schematics
                if (version != 1 && version != 2) {
                    Logger.warning("Schematic version " + version + " may not be fully supported. Attempting to load anyway.");
                }
            }
            
            // Basic validation
            if (!result.containsKey("Width") || !result.containsKey("Height") || !result.containsKey("Length")) {
                throw new IOException("Invalid schematic file: Missing dimension data");
            }
            
            if (!result.containsKey("BlockData") || !result.containsKey("Palette")) {
                throw new IOException("Invalid schematic file: Missing block data or palette");
            }
        }
        
        return result;
    }
    
    /**
     * Read a compound tag from the input stream.
     * 
     * @param dis The data input stream
     * @param result The map to store the results in
     * @throws IOException If an error occurs while reading
     */
    private static void readCompound(DataInputStream dis, Map<String, Object> result) throws IOException {
        while (true) {
            byte tagType = dis.readByte();
            
            // Check for end of compound
            if (tagType == TAG_END) {
                break;
            }
            
            String name = readString(dis);
            Object value = readTag(dis, tagType);
            
            result.put(name, value);
        }
    }
    
    /**
     * Read a tag of the specified type from the input stream.
     * 
     * @param dis The data input stream
     * @param tagType The type of tag to read
     * @return The value of the tag
     * @throws IOException If an error occurs while reading
     */
    private static Object readTag(DataInputStream dis, byte tagType) throws IOException {
        switch (tagType) {
            case TAG_BYTE:
                return dis.readByte();
            
            case TAG_SHORT:
                return dis.readShort();
            
            case TAG_INT:
                return dis.readInt();
            
            case TAG_LONG:
                return dis.readLong();
            
            case TAG_FLOAT:
                return dis.readFloat();
            
            case TAG_DOUBLE:
                return dis.readDouble();
            
            case TAG_BYTE_ARRAY:
                int baLength = dis.readInt();
                byte[] bytes = new byte[baLength];
                dis.readFully(bytes);
                return bytes;
            
            case TAG_STRING:
                return readString(dis);
            
            case TAG_LIST:
                byte listType = dis.readByte();
                int listLength = dis.readInt();
                List<Object> list = new ArrayList<>(listLength);
                
                for (int i = 0; i < listLength; i++) {
                    list.add(readTag(dis, listType));
                }
                
                return list;
            
            case TAG_COMPOUND:
                Map<String, Object> compound = new HashMap<>();
                readCompound(dis, compound);
                return compound;
            
            case TAG_INT_ARRAY:
                int iaLength = dis.readInt();
                int[] ints = new int[iaLength];
                for (int i = 0; i < iaLength; i++) {
                    ints[i] = dis.readInt();
                }
                return ints;
            
            case TAG_LONG_ARRAY:
                int laLength = dis.readInt();
                long[] longs = new long[laLength];
                for (int i = 0; i < laLength; i++) {
                    longs[i] = dis.readLong();
                }
                return longs;
            
            default:
                throw new IOException("Unknown tag type: " + tagType);
        }
    }
    
    /**
     * Read a string from the input stream.
     * 
     * @param dis The data input stream
     * @return The string
     * @throws IOException If an error occurs while reading
     */
    private static String readString(DataInputStream dis) throws IOException {
        short length = dis.readShort();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new String(bytes, "UTF-8");
    }
    
    /**
     * Parse the block palette from the schematic data.
     * 
     * @param paletteMap The raw palette map from the schematic
     * @return A map of block state strings to palette indices
     */
    public static Map<String, Integer> parsePalette(Map<String, Object> paletteMap) {
        Map<String, Integer> result = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : paletteMap.entrySet()) {
            String blockState = entry.getKey();
            Object indexObj = entry.getValue();
            
            // Handle different number types
            int index;
            if (indexObj instanceof Integer) {
                index = (Integer) indexObj;
            } else if (indexObj instanceof Short) {
                index = ((Short) indexObj).intValue();
            } else if (indexObj instanceof Byte) {
                index = ((Byte) indexObj).intValue();
            } else if (indexObj instanceof Long) {
                index = ((Long) indexObj).intValue();
            } else {
                Logger.warning("Unexpected type in palette for " + blockState + ": " + 
                             indexObj.getClass().getName() + ". Attempting to convert to string and parse.");
                // Last resort - try to parse as a string
                try {
                    index = Integer.parseInt(indexObj.toString());
                } catch (NumberFormatException e) {
                    Logger.warning("Failed to parse palette index for " + blockState + ", skipping.");
                    continue;
                }
            }
            
            result.put(blockState, index);
        }
        
        return result;
    }
    
    /**
     * Convert a block state string to Bukkit BlockData.
     * 
     * @param blockState The block state string (e.g., "minecraft:stone[variant=andesite]")
     * @return The BlockData object, or stone if the block state is invalid
     */
    public static BlockData parseBlockState(String blockState) {
        try {
            // Convert from schematic format to Bukkit format if needed
            if (blockState.contains("[")) {
                String blockId = blockState.substring(0, blockState.indexOf("["));
                String properties = blockState.substring(blockState.indexOf("["));
                
                // Convert properties to Bukkit format if needed
                properties = properties.replace("variant=", "type=")
                                      .replace("axis=y", "axis=Y")
                                      .replace("axis=x", "axis=X")
                                      .replace("axis=z", "axis=Z");
                
                blockState = blockId + properties;
            }
            
            return Bukkit.createBlockData(blockState);
        } catch (Exception e) {
            // If we can't parse it, default to stone
            Logger.info("Failed to parse block state: " + blockState + " - " + e.getMessage());
            return Bukkit.createBlockData(Material.STONE);
        }
    }
    
    /**
     * Reverse a palette map to get block states from indices.
     * 
     * @param palette The palette map (block state -> index)
     * @return A map of indices to block states
     */
    public static Map<Integer, String> reversePalette(Map<String, Integer> palette) {
        Map<Integer, String> reversed = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : palette.entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }
        
        return reversed;
    }
} 