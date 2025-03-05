package org.frizzlenpop.frizzlenEdit.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;
import org.frizzlenpop.frizzlenEdit.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages the plugin configuration settings.
 */
public class ConfigManager {
    private final FrizzlenEdit plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Default configuration values
    private static final int DEFAULT_MAX_SELECTION_BLOCKS = 10000000; // 10 million blocks
    private static final int DEFAULT_UNDO_HISTORY_SIZE = 25;
    private static final int DEFAULT_MAX_BRUSH_SIZE = 30;
    private static final int DEFAULT_CLIPBOARD_SIZE_LIMIT = 1000000; // 1 million blocks
    private static final int DEFAULT_BATCH_PASTE_SIZE = 1000; // 1000 blocks per batch
    private static final int DEFAULT_BATCH_PASTE_DELAY = 1; // 1 tick delay between batches
    
    // Config keys
    public static final String KEY_MAX_SELECTION_BLOCKS = "max-selection-blocks";
    public static final String KEY_UNDO_HISTORY_SIZE = "undo-history-size";
    public static final String KEY_MAX_BRUSH_SIZE = "max-brush-size";
    public static final String KEY_CLIPBOARD_SIZE_LIMIT = "clipboard-size-limit";
    public static final String KEY_STORAGE_TYPE = "storage-type"; // "file" or "mysql"
    public static final String KEY_BATCH_PASTE_SIZE = "batch-paste-size";
    public static final String KEY_BATCH_PASTE_DELAY = "batch-paste-delay";
    
    public ConfigManager(FrizzlenEdit plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }
    
    /**
     * Loads or creates the configuration file.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // Ensure default values
        ensureDefaults();
        
        // Save updated config with any new defaults
        plugin.saveConfig();
        
        Logger.info("Configuration loaded successfully");
    }
    
    /**
     * Ensure all default values exist in the config
     */
    private void ensureDefaults() {
        if (!config.contains(KEY_MAX_SELECTION_BLOCKS)) {
            config.set(KEY_MAX_SELECTION_BLOCKS, DEFAULT_MAX_SELECTION_BLOCKS);
        }
        
        if (!config.contains(KEY_UNDO_HISTORY_SIZE)) {
            config.set(KEY_UNDO_HISTORY_SIZE, DEFAULT_UNDO_HISTORY_SIZE);
        }
        
        if (!config.contains(KEY_MAX_BRUSH_SIZE)) {
            config.set(KEY_MAX_BRUSH_SIZE, DEFAULT_MAX_BRUSH_SIZE);
        }
        
        if (!config.contains(KEY_CLIPBOARD_SIZE_LIMIT)) {
            config.set(KEY_CLIPBOARD_SIZE_LIMIT, DEFAULT_CLIPBOARD_SIZE_LIMIT);
        }
        
        if (!config.contains(KEY_STORAGE_TYPE)) {
            config.set(KEY_STORAGE_TYPE, "file");
        }
        
        if (!config.contains(KEY_BATCH_PASTE_SIZE)) {
            config.set(KEY_BATCH_PASTE_SIZE, DEFAULT_BATCH_PASTE_SIZE);
        }
        
        if (!config.contains(KEY_BATCH_PASTE_DELAY)) {
            config.set(KEY_BATCH_PASTE_DELAY, DEFAULT_BATCH_PASTE_DELAY);
        }
    }
    
    /**
     * Save all configuration files.
     */
    public void saveAll() {
        plugin.saveConfig();
    }
    
    /**
     * Get the maximum number of blocks allowed in a selection.
     * @return The maximum number of blocks
     */
    public int getMaxSelectionBlocks() {
        return config.getInt(KEY_MAX_SELECTION_BLOCKS, DEFAULT_MAX_SELECTION_BLOCKS);
    }
    
    /**
     * Get the size of the undo history.
     * @return The number of operations to keep in history
     */
    public int getUndoHistorySize() {
        return config.getInt(KEY_UNDO_HISTORY_SIZE, DEFAULT_UNDO_HISTORY_SIZE);
    }
    
    /**
     * Get the maximum brush size.
     * @return The maximum radius for brushes
     */
    public int getMaxBrushSize() {
        return config.getInt(KEY_MAX_BRUSH_SIZE, DEFAULT_MAX_BRUSH_SIZE);
    }
    
    /**
     * Get the maximum clipboard size.
     * @return The maximum number of blocks in clipboard
     */
    public int getClipboardSizeLimit() {
        return config.getInt(KEY_CLIPBOARD_SIZE_LIMIT, DEFAULT_CLIPBOARD_SIZE_LIMIT);
    }
    
    /**
     * Get the storage type for schematics and history.
     * @return The storage type (file or mysql)
     */
    public String getStorageType() {
        return config.getString(KEY_STORAGE_TYPE, "file");
    }
    
    /**
     * Get the default batch size for large paste operations.
     * @return The default batch size
     */
    public int getBatchPasteSize() {
        return config.getInt(KEY_BATCH_PASTE_SIZE, DEFAULT_BATCH_PASTE_SIZE);
    }
    
    /**
     * Get the default tick delay between batches for large paste operations.
     * @return The default tick delay
     */
    public int getBatchPasteDelay() {
        return config.getInt(KEY_BATCH_PASTE_DELAY, DEFAULT_BATCH_PASTE_DELAY);
    }
} 