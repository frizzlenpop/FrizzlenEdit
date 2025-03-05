package org.frizzlenpop.frizzlenEdit.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Utility class for monitoring server performance.
 * Keeps track of TPS (Ticks Per Second) to help optimize operations.
 */
public class ServerPerformanceMonitor {
    private static final int SAMPLE_WINDOW = 10; // Sample window in seconds
    private static final double IDEAL_TPS = 20.0; // Minecraft runs at 20 TPS ideally
    private static final double PERFORMANCE_THRESHOLD_HIGH = 19.0; // High performance threshold
    private static final double PERFORMANCE_THRESHOLD_MEDIUM = 17.0; // Medium performance threshold
    private static final double PERFORMANCE_THRESHOLD_LOW = 14.0; // Low performance threshold
    
    private static ServerPerformanceMonitor instance;
    private final Plugin plugin;
    private BukkitTask monitorTask;
    
    // TPS tracking
    private final long[] tickTimes = new long[SAMPLE_WINDOW];
    private int tickIndex = 0;
    private long lastTickTime = 0;
    private double currentTps = IDEAL_TPS;
    
    /**
     * Initialize the server performance monitor.
     * @param plugin The plugin instance
     */
    private ServerPerformanceMonitor(Plugin plugin) {
        this.plugin = plugin;
        this.lastTickTime = System.currentTimeMillis();
        startMonitoring();
    }
    
    /**
     * Get the instance of the server performance monitor.
     * @param plugin The plugin instance (only needed on first call)
     * @return The server performance monitor instance
     */
    public static synchronized ServerPerformanceMonitor getInstance(Plugin plugin) {
        if (instance == null) {
            if (plugin == null) {
                throw new IllegalArgumentException("Plugin cannot be null on first initialization");
            }
            instance = new ServerPerformanceMonitor(plugin);
        }
        return instance;
    }
    
    /**
     * Get the instance of the server performance monitor.
     * @return The server performance monitor instance, or null if not initialized
     */
    public static ServerPerformanceMonitor getInstance() {
        return instance;
    }
    
    /**
     * Start monitoring the server TPS.
     */
    private void startMonitoring() {
        // Initialize tick times
        for (int i = 0; i < SAMPLE_WINDOW; i++) {
            tickTimes[i] = 50; // Default to 50ms per tick (20 TPS)
        }
        
        // Schedule the monitoring task to run each tick
        monitorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTickTime;
            lastTickTime = now;
            
            // Sanity check - ignore abnormal values
            if (elapsed < 0 || elapsed > 1000) {
                return;
            }
            
            // Record this tick time
            tickTimes[tickIndex] = elapsed;
            tickIndex = (tickIndex + 1) % SAMPLE_WINDOW;
            
            // Calculate current TPS
            long total = 0;
            for (long time : tickTimes) {
                total += time;
            }
            
            // Calculate TPS from average tick time
            double averageTickTime = (double) total / SAMPLE_WINDOW;
            currentTps = Math.min(IDEAL_TPS, 1000.0 / averageTickTime);
            
        }, 1L, 1L);
    }
    
    /**
     * Stop monitoring the server TPS.
     */
    public void shutdown() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }
    
    /**
     * Get the current server TPS.
     * @return The current TPS (Ticks Per Second)
     */
    public double getCurrentTps() {
        return currentTps;
    }
    
    /**
     * Get the server performance factor. This is a value between 0.0 and 1.0,
     * where 1.0 indicates optimal performance (20 TPS).
     * @return The performance factor
     */
    public double getPerformanceFactor() {
        return Math.min(1.0, currentTps / IDEAL_TPS);
    }
    
    /**
     * Get the current performance level of the server.
     * @return The performance level as an enum value
     */
    public PerformanceLevel getPerformanceLevel() {
        if (currentTps >= PERFORMANCE_THRESHOLD_HIGH) {
            return PerformanceLevel.HIGH;
        } else if (currentTps >= PERFORMANCE_THRESHOLD_MEDIUM) {
            return PerformanceLevel.MEDIUM;
        } else if (currentTps >= PERFORMANCE_THRESHOLD_LOW) {
            return PerformanceLevel.LOW;
        } else {
            return PerformanceLevel.VERY_LOW;
        }
    }
    
    /**
     * Check if the server is performing optimally (near 20 TPS).
     * @return True if the server is performing well
     */
    public boolean isOptimalPerformance() {
        return currentTps >= PERFORMANCE_THRESHOLD_HIGH;
    }
    
    /**
     * Calculate an adaptive batch size based on current server performance.
     * @param baseBatchSize The base batch size
     * @return An adjusted batch size
     */
    public int getAdaptiveBatchSize(int baseBatchSize) {
        PerformanceLevel level = getPerformanceLevel();
        switch (level) {
            case HIGH:
                return (int) (baseBatchSize * 1.5); // 50% more when performance is high
            case MEDIUM:
                return baseBatchSize; // Normal batch size
            case LOW:
                return (int) (baseBatchSize * 0.75); // 25% less when performance is low
            case VERY_LOW:
                return (int) (baseBatchSize * 0.5); // 50% less when performance is very low
            default:
                return baseBatchSize;
        }
    }
    
    /**
     * Calculate an adaptive tick delay based on current server performance.
     * @param baseTickDelay The base tick delay
     * @return An adjusted tick delay
     */
    public int getAdaptiveTickDelay(int baseTickDelay) {
        PerformanceLevel level = getPerformanceLevel();
        switch (level) {
            case HIGH:
                return Math.max(1, baseTickDelay - 1); // Faster when performance is high
            case MEDIUM:
                return baseTickDelay; // Normal tick delay
            case LOW:
                return baseTickDelay + 1; // Slower when performance is low
            case VERY_LOW:
                return baseTickDelay + 2; // Much slower when performance is very low
            default:
                return baseTickDelay;
        }
    }
    
    /**
     * Performance level of the server.
     */
    public enum PerformanceLevel {
        HIGH,      // 19-20 TPS
        MEDIUM,    // 17-19 TPS
        LOW,       // 14-17 TPS
        VERY_LOW   // <14 TPS
    }
} 