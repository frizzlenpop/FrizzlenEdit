package org.frizzlenpop.frizzlenEdit.utils;

import org.bukkit.plugin.Plugin;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;

import java.util.logging.Level;

/**
 * Utility class to handle logging with a consistent format.
 */
public class Logger {
    private static Plugin plugin;
    private static final String LOG_PREFIX = "[FrizzlenEdit] ";
    private static boolean debugMode = false;

    /**
     * Initialize the logger with the plugin instance.
     * @param plugin The plugin instance
     */
    public static void init(Plugin plugin) {
        Logger.plugin = plugin;
        
        // Check for debug mode in config if available
        if (plugin instanceof FrizzlenEdit) {
            FrizzlenEdit frizzlenEdit = (FrizzlenEdit) plugin;
            if (frizzlenEdit.getConfigManager() != null) {
                try {
                    debugMode = frizzlenEdit.getConfigManager().isDebugModeEnabled();
                } catch (Exception e) {
                    // Config not loaded yet, will default to false
                }
            }
        }
    }
    
    /**
     * Update debug mode from config
     * @param enabled Whether debug mode is enabled
     */
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        if (debugMode) {
            info("Debug mode enabled");
        }
    }

    /**
     * Log a message with the specified level.
     * @param level The log level
     * @param message The message to log
     */
    public static void log(Level level, String message) {
        if (plugin != null) {
            plugin.getLogger().log(level, LOG_PREFIX + message);
        }
    }

    /**
     * Log an info message.
     * @param message The message to log
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Log a warning message.
     * @param message The message to log
     */
    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Log a severe message.
     * @param message The message to log
     */
    public static void severe(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Log a debug message (only if debug is enabled).
     * @param message The message to log
     */
    public static void debug(String message) {
        if (debugMode) {
            log(Level.INFO, "[DEBUG] " + message);
        } else {
            // Still log as FINE for server logs that capture everything
            log(Level.FINE, "[DEBUG] " + message);
        }
    }
} 