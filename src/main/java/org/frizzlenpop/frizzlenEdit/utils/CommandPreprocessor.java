package org.frizzlenpop.frizzlenEdit.utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.frizzlenpop.frizzlenEdit.FrizzlenEdit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles standardization of FrizzlenEdit command prefixes.
 * Ensures that commands use the standard "//" prefix.
 */
public class CommandPreprocessor implements Listener {
    
    private final FrizzlenEdit plugin;
    private final Set<String> editCommands;
    private String commandPrefix;
    
    public CommandPreprocessor(FrizzlenEdit plugin) {
        this.plugin = plugin;
        this.editCommands = new HashSet<>();
        this.commandPrefix = "//"; // Default prefix
        
        // Get the command prefix from config if available
        if (plugin.getConfigManager() != null) {
            this.commandPrefix = plugin.getConfigManager().getCommandPrefix();
            Logger.debug("Command prefix set to: " + commandPrefix);
        }
        
        // Add all commands from plugin.yml that should use the // prefix
        editCommands.add("wand");
        editCommands.add("pos1");
        editCommands.add("pos2");
        editCommands.add("expand");
        editCommands.add("contract");
        editCommands.add("size");
        editCommands.add("cut");
        editCommands.add("copy");
        editCommands.add("paste");
        editCommands.add("pastelarge");
        editCommands.add("flip");
        editCommands.add("rotate");
        editCommands.add("undo");
        editCommands.add("redo");
        editCommands.add("set");
        editCommands.add("replace");
        editCommands.add("fill");
        editCommands.add("walls");
        editCommands.add("outline");
        editCommands.add("hollow");
        editCommands.add("smooth");
        editCommands.add("brush");
        editCommands.add("brushtool");
        editCommands.add("mask");
        editCommands.add("schematic");
        editCommands.add("naturalize");
        editCommands.add("overlay");
        editCommands.add("caves");
        editCommands.add("regen");
        editCommands.add("chunkinfo");
        editCommands.add("removenear");
    }
    
    /**
     * Update the command prefix from the config.
     */
    public void updateCommandPrefix() {
        if (plugin.getConfigManager() != null) {
            this.commandPrefix = plugin.getConfigManager().getCommandPrefix();
            Logger.debug("Command prefix updated to: " + commandPrefix);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        String message = event.getMessage();
        
        // Handle ///command first (convert to //command) - this ensures triple slash commands work
        if (message.startsWith("///")) {
            String commandName = message.substring(3).split(" ")[0].toLowerCase();
            
            // Check if this is one of our edit commands
            if (editCommands.contains(commandName)) {
                // Convert ///command to //command (or whatever prefix is configured)
                String newMessage = "/" + commandPrefix + message.substring(3);
                Logger.debug("Converted command from: " + message + " to: " + newMessage);
                event.setMessage(newMessage);
                return;
            }
        }
        
        // Standard format is //command (or whatever prefix is configured)
        if (message.startsWith("/" + commandPrefix)) {
            // Already in the correct format, do nothing
            return;
        }
        
        // Handle /command (this would incorrectly map to the plugin command)
        if (message.startsWith("/") && !message.startsWith("//") && !message.startsWith("///")) {
            String commandName = message.substring(1).split(" ")[0].toLowerCase();
            
            // Check if this is one of our edit commands
            if (editCommands.contains(commandName)) {
                // Convert /command to //command (or whatever prefix is configured)
                String newMessage = "/" + commandPrefix + message.substring(1);
                Logger.debug("Converted command from: " + message + " to: " + newMessage);
                event.setMessage(newMessage);
                return;
            }
        }
    }
} 