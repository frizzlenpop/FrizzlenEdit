# History System

The FrizzlenEdit History System provides robust undo and redo functionality, allowing players to track changes and revert them if needed. This system is essential for risk-free world editing.

## Core Features

### Tracked Operations

The History System automatically tracks:
- All block operations (set, replace, etc.)
- Brush actions
- Paste operations
- Terrain modifications
- Any other operation that changes blocks in the world

### Per-Player History

- Each player has their own independent history
- Players can only undo/redo their own actions
- History persists across login sessions (until the server restarts)

## Commands

### Undo Command

The `//undo` command reverts recent operations.

**Usage:**
```
//undo [count]
```

**Parameters:**
- `[count]`: Optional parameter specifying how many operations to undo (default: 1)

**Examples:**
- `//undo` - Undo the most recent operation
- `//undo 3` - Undo the three most recent operations

**Permission:** `frizzlenedit.history.undo`

### Redo Command

The `//redo` command redoes previously undone operations.

**Usage:**
```
//redo [count]
```

**Parameters:**
- `[count]`: Optional parameter specifying how many operations to redo (default: 1)

**Examples:**
- `//redo` - Redo the most recently undone operation
- `//redo 5` - Redo the five most recently undone operations

**Permission:** `frizzlenedit.history.redo`

### Clear History Command

The `//clearhistory` command clears a player's history stack.

**Usage:**
```
//clearhistory
```

**Permission:** `frizzlenedit.history.clear`

## Technical Details

### History Entry Structure

Each history entry contains:
1. The player who performed the operation
2. The world in which the operation was performed
3. A description of the operation
4. Previous block states for undo
5. New block states for redo

### History Stack Size

FrizzlenEdit maintains a limited history stack to prevent excessive memory usage. By default, this is set to 25 operations per player, but can be configured in the plugin's settings.

When the history stack is full, the oldest entries are automatically removed to make room for new ones.

### Memory Management

To optimize memory usage, the history system:
- Only stores block states that actually changed
- Uses efficient data structures to minimize memory footprint
- Compresses large history entries
- Implements automatic cleanup for old entries

## Performance Considerations

### Large Operations

For very large operations (affecting hundreds of thousands of blocks):
- History entries may consume significant memory
- Undo/redo operations may take longer to process
- Server performance might temporarily decrease during undo/redo

### Optimizations

To improve performance when working with history:
- Perform several smaller operations instead of one massive operation
- Clear history when it's no longer needed
- Consider disabling history for certain operations if memory is a concern

## Best Practices

### History Management

- Regularly clear your history if you're performing many operations
- Check the operation volume before performing operations on very large areas
- Use `//clearhistory` before disconnecting if you've performed many large operations

### Server Administration

For server administrators:
- Monitor memory usage when many players are using world editing features
- Adjust history size limits in configuration if needed
- Consider using a server with more RAM if world editing is a primary feature

## Advanced Usage

### Operation Descriptions

Each operation in the history has a description, which is shown when using undo/redo commands. These descriptions help identify which operation is being undone or redone.

For example:
- "Set operation (stone) - 1,024 blocks"
- "Enhanced smooth operation with erosion and surface preservation (4 iterations, height factor 2.0)"
- "Cylinder operation (glass, radius 10, height 20)"

### History Inspection

Advanced users can use the operation descriptions to understand what has been done in an area, even if they weren't present when the edits were made.

## Troubleshooting

**Issue**: "Nothing to undo" message when trying to undo  
**Solution**: You have either reached the beginning of your history stack or have not performed any operations yet

**Issue**: "Nothing to redo" message when trying to redo  
**Solution**: You have either reached the end of your redo stack or have not undone any operations yet

**Issue**: Unable to undo an operation that was just performed  
**Solution**: Some operations (typically those affecting very large areas) might be configured to skip history tracking for performance reasons

**Issue**: Server lag when undoing/redoing large operations  
**Solution**: Be patient, or consider canceling the undo/redo if it's taking too long

## Examples

### Basic Workflow

1. Perform an operation: `//set stone`
2. Realize it was a mistake
3. Undo the operation: `//undo`
4. Change your mind again
5. Redo the operation: `//redo`

### Sequential Editing

1. Create a basic structure: `//set stone`
2. Add decorative elements: `//replace stone stonebrick 20%`
3. Decide the decoration doesn't look right: `//undo`
4. Try a different approach: `//replace stone cobblestone 15%`
5. Decide to revert to the original: `//undo` (twice)

### Batch Undoing

1. Perform multiple operations to create a complex structure
2. Realize the entire structure is in the wrong location
3. Undo all operations at once: `//undo 10`
4. Start over in the correct location

[Return to Main Documentation](../README.md) 