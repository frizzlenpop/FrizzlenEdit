package org.frizzlenpop.frizzlenEdit;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenEdit.commands.*;
import org.frizzlenpop.frizzlenEdit.config.ConfigManager;
import org.frizzlenpop.frizzlenEdit.history.HistoryManager;
import org.frizzlenpop.frizzlenEdit.selection.SelectionManager;
import org.frizzlenpop.frizzlenEdit.clipboard.ClipboardManager;
import org.frizzlenpop.frizzlenEdit.schematic.SchematicManager;
import org.frizzlenpop.frizzlenEdit.operations.OperationManager;
import org.frizzlenpop.frizzlenEdit.brushes.BrushManager;
import org.frizzlenpop.frizzlenEdit.utils.Logger;
import org.frizzlenpop.frizzlenEdit.utils.ServerPerformanceMonitor;
import org.frizzlenpop.frizzlenEdit.utils.CommandPreprocessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public final class FrizzlenEdit extends JavaPlugin {
    
    private static FrizzlenEdit instance;
    private ConfigManager configManager;
    private SelectionManager selectionManager;
    private ClipboardManager clipboardManager;
    private HistoryManager historyManager;
    private SchematicManager schematicManager;
    private OperationManager operationManager;
    private BrushManager brushManager;
    private ExecutorService asyncExecutor;
    private CommandPreprocessor commandPreprocessor;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize logger
        Logger.init(this);
        Logger.log(Level.INFO, "Initializing FrizzlenEdit...");
        
        // Create thread pool for async operations
        int threadCount = Runtime.getRuntime().availableProcessors();
        asyncExecutor = Executors.newWorkStealingPool(threadCount);
        Logger.log(Level.INFO, "Created thread pool with " + threadCount + " threads");
        
        // Initialize managers
        configManager = new ConfigManager(this);
        selectionManager = new SelectionManager(this);
        clipboardManager = new ClipboardManager(this);
        historyManager = new HistoryManager(this);
        schematicManager = new SchematicManager(this);
        operationManager = new OperationManager(this);
        brushManager = new BrushManager(this);
        commandPreprocessor = new CommandPreprocessor(this);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(selectionManager, this);
        getServer().getPluginManager().registerEvents(brushManager, this);
        getServer().getPluginManager().registerEvents(commandPreprocessor, this);
        
        // Initialize server performance monitor
        ServerPerformanceMonitor.getInstance(this);
        Logger.log(Level.INFO, "Server performance monitor initialized");
        
        // Register commands
        registerCommands();
        
        Logger.log(Level.INFO, "FrizzlenEdit has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        Logger.log(Level.INFO, "Shutting down FrizzlenEdit...");
        
        // Shutdown async executor
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
        
        // Shutdown performance monitor
        ServerPerformanceMonitor monitor = ServerPerformanceMonitor.getInstance();
        if (monitor != null) {
            monitor.shutdown();
        }
        
        // Save any necessary data
        if (configManager != null) {
            configManager.saveAll();
        }
        
        if (schematicManager != null) {
            schematicManager.shutdown();
        }
        
        Logger.log(Level.INFO, "FrizzlenEdit has been disabled.");
    }
    
    private void registerCommands() {
        // Main command handler that will dispatch to appropriate subcommands
        FrizzlenEditCommandHandler mainHandler = new FrizzlenEditCommandHandler(this);
        
        // Register the main command
        getCommand("frizzlenedit").setExecutor(mainHandler);
        
        // Register selection commands
        getCommand("wand").setExecutor(new SelectionCommands.WandCommand(this));
        getCommand("pos1").setExecutor(new SelectionCommands.Pos1Command(this));
        getCommand("pos2").setExecutor(new SelectionCommands.Pos2Command(this));
        getCommand("expand").setExecutor(new SelectionCommands.ExpandCommand(this));
        getCommand("contract").setExecutor(new SelectionCommands.ContractCommand(this));
        getCommand("size").setExecutor(new SelectionCommands.SizeCommand(this));
        
        // Register clipboard commands
        ClipboardCommands.CopyCommand copyCommand = new ClipboardCommands.CopyCommand(this);
        ClipboardCommands.CutCommand cutCommand = new ClipboardCommands.CutCommand(this);
        ClipboardCommands.PasteCommand pasteCommand = new ClipboardCommands.PasteCommand(this);
        ClipboardCommands.PasteLargeCommand pasteLargeCommand = new ClipboardCommands.PasteLargeCommand(this);
        ClipboardCommands.RotateCommand rotateCommand = new ClipboardCommands.RotateCommand(this);
        ClipboardCommands.FlipCommand flipCommand = new ClipboardCommands.FlipCommand(this);
        
        getCommand("copy").setExecutor(copyCommand);
        getCommand("cut").setExecutor(cutCommand);
        getCommand("paste").setExecutor(pasteCommand);
        getCommand("pastelarge").setExecutor(pasteLargeCommand);
        getCommand("rotate").setExecutor(rotateCommand);
        getCommand("flip").setExecutor(flipCommand);
        
        // Register block operation commands
        getCommand("set").setExecutor(new BlockCommands.SetCommand(this));
        getCommand("replace").setExecutor(new BlockCommands.ReplaceCommand(this));
        getCommand("smooth").setExecutor(new BlockCommands.SmoothCommand(this));
        getCommand("drain").setExecutor(new BlockCommands.DrainCommand(this));
        getCommand("cyl").setExecutor(new BlockCommands.CylinderCommand(this));
        getCommand("removenear").setExecutor(new BlockCommands.RemoveNearCommand(this));
        
        // Register history commands
        getCommand("undo").setExecutor(new BlockCommands.UndoCommand(this));
        getCommand("redo").setExecutor(new BlockCommands.RedoCommand(this));
        
        // Register brush commands
        BrushCommands.SphereBrushCommand sphereBrushCommand = new BrushCommands.SphereBrushCommand(this);
        BrushCommands.CylinderBrushCommand cylinderBrushCommand = new BrushCommands.CylinderBrushCommand(this);
        BrushCommands.SmoothBrushCommand smoothBrushCommand = new BrushCommands.SmoothBrushCommand(this);
        BrushCommands.NoneBrushCommand noneBrushCommand = new BrushCommands.NoneBrushCommand(this);
        
        // Create a brush command handler that will delegate to the appropriate brush subcommand
        BrushCommandHandler brushHandler = new BrushCommandHandler(this, 
            sphereBrushCommand, cylinderBrushCommand, smoothBrushCommand, noneBrushCommand);
        getCommand("brush").setExecutor(brushHandler);
        
        // Register mask command
        getCommand("mask").setExecutor(new BrushCommands.MaskCommand(this));
        
        // Register brush tool command
        getCommand("brushtool").setExecutor(new BrushCommands.BrushToolCommand(this));
        
        // Register schematic commands
        SchematicCommands.SaveCommand saveCommand = new SchematicCommands.SaveCommand(this);
        SchematicCommands.LoadCommand loadCommand = new SchematicCommands.LoadCommand(this);
        SchematicCommands.DeleteCommand deleteCommand = new SchematicCommands.DeleteCommand(this);
        SchematicCommands.ListCommand listCommand = new SchematicCommands.ListCommand(this);
        SchematicCommands.FormatsCommand formatsCommand = new SchematicCommands.FormatsCommand(this);
        SchematicCommands.PasteCommand schemPasteCommand = new SchematicCommands.PasteCommand(this);
        SchematicCommands.PasteLargeCommand schemPasteLargeCommand = new SchematicCommands.PasteLargeCommand(this);
        SchematicCommands.AdaptivePasteCommand adaptivePasteCommand = new SchematicCommands.AdaptivePasteCommand(this);
        
        SchematicCommandHandler schematicCommandHandler = new SchematicCommandHandler(
            this, saveCommand, loadCommand, deleteCommand, listCommand, formatsCommand, 
            schemPasteCommand, schemPasteLargeCommand, adaptivePasteCommand
        );
        
        getCommand("schematic").setExecutor(schematicCommandHandler);
        
        Logger.info("Registered all commands");
    }
    
    // Submit a task to be run asynchronously
    public void runAsync(Runnable task) {
        asyncExecutor.submit(task);
    }
    
    // Static getter for easy access
    public static FrizzlenEdit getInstance() {
        return instance;
    }
    
    // Getters for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    public ClipboardManager getClipboardManager() {
        return clipboardManager;
    }
    
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
    
    public SchematicManager getSchematicManager() {
        return schematicManager;
    }
    
    public OperationManager getOperationManager() {
        return operationManager;
    }
    
    public BrushManager getBrushManager() {
        return brushManager;
    }
    
    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }
    
    /**
     * Get the command preprocessor.
     * @return The command preprocessor
     */
    public CommandPreprocessor getCommandPreprocessor() {
        return commandPreprocessor;
    }
    
    /**
     * Get the server performance monitor.
     * @return The server performance monitor
     */
    public ServerPerformanceMonitor getServerPerformanceMonitor() {
        return ServerPerformanceMonitor.getInstance();
    }
}
