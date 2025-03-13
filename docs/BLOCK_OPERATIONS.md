# Block Operations

FrizzlenEdit provides a robust set of commands for manipulating blocks within selections, allowing for efficient terrain modification, structure creation, and world editing.

## Core Block Commands

### Set Command

The `//set` command fills an entire selection with a specified block type.

**Usage:**
```
//set <block>
```

**Parameters:**
- `<block>`: The block type to fill the selection with (e.g., "stone", "oak_planks", "grass_block")

**Examples:**
- `//set stone` - Fill the selection with stone blocks
- `//set oak_planks` - Fill the selection with oak planks
- `//set grass_block` - Cover the selection with grass blocks

**Permission:** `frizzlenedit.block.set`

### Replace Command

The `//replace` command replaces specific block types within a selection with another block type.

**Usage:**
```
//replace <from> <to>
```

**Parameters:**
- `<from>`: The block type to replace
- `<to>`: The block type to replace with

**Examples:**
- `//replace stone dirt` - Replace all stone blocks with dirt
- `//replace grass_block stone` - Replace all grass blocks with stone
- `//replace oak_planks spruce_planks` - Replace oak planks with spruce planks

**Permission:** `frizzlenedit.block.replace`

### Cylinder Command

The `//cyl` command creates cylindrical shapes at your location.

**Usage:**
```
//cyl <block> <radius> <height> [hollow]
```

**Parameters:**
- `<block>`: The block type for the cylinder
- `<radius>`: The radius of the cylinder
- `<height>`: The height of the cylinder
- `[hollow]`: Optional parameter to create a hollow cylinder

**Examples:**
- `//cyl stone 5 10` - Create a solid stone cylinder with radius 5 and height 10
- `//cyl glass 8 4 hollow` - Create a hollow glass cylinder with radius 8 and height 4

**Permission:** `frizzlenedit.block.cylinder`

## Advanced Block Operations

### Smooth Command

The `//smooth` command smooths terrain within a selection using the enhanced smoothing system.

**Usage:**
```
//smooth [iterations] [heightFactor] [options]
```

**Parameters:**
- `iterations`: Number of smoothing passes (default: 4)
- `heightFactor`: Controls vertical vs horizontal smoothing (default: 2.0)
- `options`: 
  - `-e` / `+e`: Disable/Enable erosion simulation
  - `-p` / `+p`: Disable/Enable preserving surface layer
  - `-v=N`: Set natural variation (0.0-1.0)

For detailed information, see the [Enhanced Smoothing documentation](ENHANCED_SMOOTHING.md).

**Permission:** `frizzlenedit.region.smooth`

### Drain Command

The `//drain` command removes water (or all liquids) from a selected area.

**Usage:**
```
//drain [all]
```

**Parameters:**
- `[all]`: Optional parameter to remove all liquids (including lava) instead of just water

**Examples:**
- `//drain` - Remove only water from the selection
- `//drain all` - Remove all liquids (water and lava) from the selection

**Permission:** `frizzlenedit.region.drain`

## Using Block Operations Effectively

### Working with Large Selections

For large operations, FrizzlenEdit automatically optimizes performance by:
1. Processing blocks in batches
2. Precomputing necessary data
3. Tracking block states efficiently for history

When working with extremely large areas, consider:
- Breaking operations into smaller sections
- Using async operations where supported
- Monitoring server performance

### Block Type Specification

Block types can be specified in several formats:
- Simple name: `stone`, `dirt`, `glass`
- Full name with namespace: `minecraft:oak_planks`
- Legacy data values for backward compatibility

### Understanding Operation Volume

Each operation's volume (number of blocks affected) is limited by the server configuration to prevent performance issues. By default, this is typically set to 500,000 blocks. Operations exceeding this limit will be rejected with an error message.

## History Integration

All block operations are automatically recorded in the history system, allowing you to:
- Undo operations with `//undo`
- Redo operations with `//redo`
- Clear history with `//clearhistory`

Each block operation records:
- Previous block states for undo
- New block states for redo
- Operation type and description

## Tips and Best Practices

- **Performance**: For massive operations, consider using multiple smaller operations instead of one large one
- **Precision**: Use the selection system to precisely define the area before performing block operations
- **Compatibility**: When working with blocks that have block states (like stairs, doors, etc.), be aware that replacement operations preserve block states when possible
- **Visualization**: Use `//outline` or similar commands to visualize a selection before performing a major operation

## Common Issues and Solutions

**Issue**: "Operation too large" error message  
**Solution**: Reduce the size of your selection or increase the operation limit in the configuration

**Issue**: Blocks with unexpected states or orientations  
**Solution**: Use more specific block types or adjust block states manually

**Issue**: Performance degradation during large operations  
**Solution**: Break the operation into smaller chunks or use async operations

## Examples

### Creating a Simple Building Foundation

1. Select the area for your foundation: `//pos1` and `//pos2`
2. Create a stone foundation: `//set stone`
3. Add a wooden floor on top:
   - Move the selection up: `//shift up 1`
   - Add wooden floor: `//set oak_planks`

### Terraforming a Landscape

1. Select a large area with uneven terrain
2. Replace surface blocks: `//replace grass_block coarse_dirt`
3. Smooth the terrain: `//smooth 5 2.0 +e +p -v=0.3`
4. Add water features: Select water areas and use `//set water`

### Creating a Glass Dome

1. Create a selection at the desired location
2. Create a sphere using `//sphere glass 15 hollow`
3. Cut the bottom half of the sphere by replacing the lower portions with air:
   `//replace glass air` (after adjusting the selection to only include the bottom half)

[Return to Main Documentation](../README.md) 