# Brushes System

The FrizzlenEdit brush system allows players to perform world editing operations interactively by binding editing functionality to a tool. This creates a more fluid and intuitive editing experience compared to selection-based operations.

## Brush Basics

### Getting Started with Brushes

To use brushes in FrizzlenEdit:

1. Create a brush with the appropriate command (see below)
2. Get a brush tool with `//brushtool`
3. Right-click with the tool to use the brush

### Brush Types

FrizzlenEdit provides several types of brushes:

| Brush Type | Description | Command |
|------------|-------------|---------|
| Sphere | Creates spheres of blocks | `//brush sphere <material> <radius>` |
| Cylinder | Creates cylinders of blocks | `//brush cylinder <material> <radius> <height>` |
| Smooth | Smooths terrain | `//brush smooth <radius> [iterations] [heightFactor] [options]` |

## Brush Commands

### Sphere Brush

The sphere brush creates spheres (or balls) of blocks when used.

**Usage:**
```
//brush sphere <material> <radius>
```

**Parameters:**
- `<material>`: The block type to use
- `<radius>`: The radius of the sphere

**Examples:**
- `//brush sphere stone 5` - Creates a stone sphere brush with radius 5
- `//brush sphere grass_block 3` - Creates a grass block sphere brush with radius 3

**Permission:** `frizzlenedit.brush.sphere`

### Cylinder Brush

The cylinder brush creates vertical cylinders of blocks when used.

**Usage:**
```
//brush cylinder <material> <radius> <height>
```

**Parameters:**
- `<material>`: The block type to use
- `<radius>`: The radius of the cylinder
- `<height>`: The height of the cylinder

**Examples:**
- `//brush cylinder stone 5 10` - Creates a stone cylinder brush with radius 5 and height 10
- `//brush cylinder oak_planks 3 1` - Creates an oak planks disk (height 1) with radius 3

**Permission:** `frizzlenedit.brush.cylinder`

### Smooth Brush

The smooth brush smooths terrain when used, using the enhanced smoothing system.

**Usage:**
```
//brush smooth <radius> [iterations] [heightFactor] [options]
```

**Parameters:**
- `<radius>`: The radius of the brush
- `[iterations]`: Number of smoothing passes (default: 4)
- `[heightFactor]`: Controls vertical vs horizontal smoothing (default: 2.0)
- `[options]`:
  - `-e` / `+e`: Disable/Enable erosion simulation (default: enabled)
  - `-p` / `+p`: Disable/Enable preserving surface layer (default: enabled)
  - `-v=N`: Set natural variation (0.0-1.0, default: 0.2)

**Examples:**
- `//brush smooth 10` - Basic smooth brush with radius 10
- `//brush smooth 5 8 1.5` - Smooth brush with radius 5, 8 iterations, 1.5 height factor
- `//brush smooth 15 3 2.5 -e +p -v=0.4` - Customized smooth brush

For detailed information about the smoothing system, see the [Enhanced Smoothing documentation](ENHANCED_SMOOTHING.md).

**Permission:** `frizzlenedit.brush.smooth`

### Remove Brush

Removes the current brush.

**Usage:**
```
//brush none
```

**Permission:** `frizzlenedit.brush.none`

### Brush Tool

Gives you a brush tool (by default, a feather).

**Usage:**
```
//brushtool
```

**Permission:** `frizzlenedit.brush.tool`

## Brush Masking

Masks allow you to restrict your brush to only affect certain block types, providing more precise control.

### Set Mask

Sets a mask for your brush.

**Usage:**
```
//mask <material>
```

**Parameters:**
- `<material>`: The material to use as a mask. Your brush will only affect this material.

**Examples:**
- `//mask stone` - Your brush will only affect stone blocks
- `//mask dirt` - Your brush will only affect dirt blocks

**Permission:** `frizzlenedit.brush.mask`

### Remove Mask

Removes your current mask.

**Usage:**
```
//mask none
```

**Permission:** `frizzlenedit.brush.mask`

## Advanced Brush Usage

### Brush Performance

Brush operations occur in real-time, so performance is important:

- Large brush radii (>20) can cause performance issues
- Smooth brushes are more performance-intensive than simple material brushes
- Consider reducing iterations for smooth brushes in real-time use

### Combining Brushes and Selections

When working on complex projects, consider using a combination of brushes and selection-based operations:

1. Use selection-based operations for large-scale changes and initial shaping
2. Use brushes for detailing, refinement, and natural touches
3. Use the smooth brush as a finishing touch to blend everything together

### Creative Techniques

- **Stepped Terrain**: Use cylinder brushes of decreasing radius to create natural-looking stepped terrain
- **Natural Caves**: Use a sphere brush with air material to create cave networks, then smooth them
- **Custom Landscapes**: Create base terrain with selection operations, then use brushes to add details

## Troubleshooting

**Issue**: Brush not working when right-clicking  
**Solution**: Ensure you have the correct permissions and are using the brush tool (feather by default)

**Issue**: Brush creates unexpected shapes  
**Solution**: Check for blocks in the way or be aware of how brushes interact with existing terrain

**Issue**: Server lag when using large brushes  
**Solution**: Reduce brush radius, or for smooth brushes, reduce iterations

## Examples

### Creating a Natural Cave Entrance

1. Get a sphere brush with air: `//brush sphere air 4`
2. Create the basic cave shape by clicking several times
3. Switch to a smooth brush: `//brush smooth 5 3 1.5`
4. Smooth the edges of the cave to make it look natural

### Building a Castle Tower

1. Use a cylinder brush for the base: `//brush cylinder stone 8 1`
2. Place the base by right-clicking on the ground
3. Switch to a smaller radius for the walls: `//brush cylinder stone 7 20`
4. Place the walls on top of the base
5. Add a decorative top with another cylinder brush

### Terrain Sculpting

1. Create a gentle hill with a sphere brush: `//brush sphere dirt 15`
2. Add some variation with smaller spheres
3. Cover with grass: `//brush sphere grass_block 1`
4. Smooth the entire formation: `//brush smooth 15 3 2.0 +e +p -v=0.3`

[Return to Main Documentation](../README.md) 