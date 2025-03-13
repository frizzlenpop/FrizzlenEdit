# Selection System

The FrizzlenEdit Selection System allows players to define regions in the world for operations like filling, replacing, and copying. This is the foundation of most world editing tasks.

## Basic Selection Concepts

### Selection Points

A FrizzlenEdit selection consists of two points that define a 3D rectangular region:

- **Position 1 (pos1)**: The first corner of the selection
- **Position 2 (pos2)**: The opposite corner of the selection

The selection includes all blocks within the rectangular prism defined by these two points.

### Selection Visualization

When you make a selection, it's visually represented in the world using temporary particle effects that outline the selection boundaries. This helps you understand the exact area that will be affected by operations.

## Selection Commands

### Selection Wand

The wand tool lets you set selection points by clicking on blocks.

**Usage:**
```
//wand
```

Once you have the wand (by default, a wooden axe):
- **Left-click a block**: Set position 1
- **Right-click a block**: Set position 2

**Permission:** `frizzlenedit.selection.wand`

### Manual Position Setting

You can set positions directly with commands.

**Usage:**
```
//pos1
//pos2
```

These commands set the respective position to your current location. You can also specify exact coordinates:

```
//pos1 <x> <y> <z>
//pos2 <x> <y> <z>
```

**Permission:** `frizzlenedit.selection.pos`

### Selection Information

Get information about your current selection.

**Usage:**
```
//size
```

This shows:
- The dimensions of your selection
- The volume (number of blocks)
- The coordinates of both positions

**Permission:** `frizzlenedit.selection.info`

## Manipulating Selections

### Expand Selection

Expand your selection in a specified direction.

**Usage:**
```
//expand <amount> [direction]
```

**Parameters:**
- `<amount>`: Number of blocks to expand
- `[direction]`: Direction to expand (up, down, north, south, east, west, or a relative direction)

If no direction is specified, the selection expands in all directions.

**Examples:**
- `//expand 5` - Expand 5 blocks in all directions
- `//expand 10 up` - Expand 10 blocks upward
- `//expand 3 north` - Expand 3 blocks northward

**Permission:** `frizzlenedit.selection.expand`

### Contract Selection

Contract your selection in a specified direction.

**Usage:**
```
//contract <amount> [direction]
```

**Parameters:**
- `<amount>`: Number of blocks to contract
- `[direction]`: Direction to contract (up, down, north, south, east, west, or a relative direction)

If no direction is specified, the selection contracts in all directions.

**Examples:**
- `//contract 2` - Contract 2 blocks in all directions
- `//contract 5 down` - Contract 5 blocks from the bottom
- `//contract 3 east` - Contract 3 blocks from the east side

**Permission:** `frizzlenedit.selection.contract`

### Shift Selection

Move your entire selection in a specified direction.

**Usage:**
```
//shift <amount> <direction>
```

**Parameters:**
- `<amount>`: Number of blocks to shift
- `<direction>`: Direction to shift (up, down, north, south, east, west, or a relative direction)

**Examples:**
- `//shift 10 up` - Move the selection 10 blocks up
- `//shift 5 west` - Move the selection 5 blocks west

**Permission:** `frizzlenedit.selection.shift`

## Advanced Selection Commands

### Selecting Special Regions

#### Selecting Everything

Select your entire current chunk.

**Usage:**
```
//chunk
```

**Permission:** `frizzlenedit.selection.chunk`

#### Expanding to Natural Height

Expand your selection to cover from bedrock to the highest non-air block.

**Usage:**
```
//hpos1
//hpos2
```

These commands set position 1 to the bottom of the world and position 2 to the highest non-air block at your location.

**Permission:** `frizzlenedit.selection.height`

## Selection Types

By default, FrizzlenEdit uses cuboid (rectangular prism) selections. These are the most versatile and work with all operations.

## Tips for Effective Selection

### Precision Selection

For precise control:
1. Use the F3 debug screen to see exact coordinates
2. Use the manual position commands to specify exact coordinates
3. Use expand/contract to fine-tune your selection after initial placement

### Large Selections

When working with very large areas:
1. Be mindful of the server's maximum selection volume (set in configuration)
2. Consider breaking large operations into smaller chunks
3. Check the selection size with `//size` before attempting operations

### Selection Workflow

A typical workflow might be:
1. Make a rough selection with the wand tool
2. Check the size with `//size`
3. Fine-tune with `//expand`, `//contract`, and `//shift`
4. Perform the desired operation on the selection

## Common Issues and Solutions

**Issue**: "Selection volume too large" error  
**Solution**: Reduce your selection size or increase the limit in server config

**Issue**: Selection not visible  
**Solution**: Ensure particle effects are enabled in your client settings

**Issue**: Difficulty selecting specific blocks  
**Solution**: Use the manual position commands with specific coordinates

## Examples

### Creating a Building Foundation

1. Place two markers at opposite corners of your planned building
2. Select the first corner: `//pos1`
3. Move to the opposite corner: `//pos2`
4. Flatten the area to a specific height:
   - Check the current height with `//size`
   - Use `//expand` or `//contract` to adjust if needed
   - Fill with your foundation material: `//set stone`

### Creating a Tunnel

1. Select one end of the tunnel: `//pos1`
2. Move to the other end and select: `//pos2`
3. Make the selection tunnel-sized:
   - Expand 2 blocks up: `//expand 2 up`
   - Expand 1 block down: `//expand 1 down`
   - Expand 2 blocks to each side: `//expand 2 ew` (east-west)
4. Create the tunnel: `//set air`

### Creating a Multi-Story Building

1. Select the building footprint at ground level
2. Expand upward to the desired height: `//expand 20 up`
3. Create the building shell: `//set concrete`
4. Hollow out the interior: Contract the selection and use `//set air`

[Return to Main Documentation](../README.md) 