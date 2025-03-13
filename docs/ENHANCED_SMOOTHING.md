# Enhanced Smoothing System

The FrizzlenEdit plugin features an advanced terrain smoothing system designed to produce realistic, natural-looking terrain. Unlike traditional smoothing algorithms that simply average nearby blocks, this enhanced system incorporates geological awareness, erosion simulation, and natural variation to create truly organic landscapes.

## Key Features

### Geological Awareness

The smoothing system recognizes that certain materials naturally appear together in geological formations:

- **Stone Group**: Stone, andesite, diorite, granite, cobblestone, mossy cobblestone
- **Sand Group**: Sand, sandstone, red sand, red sandstone, smooth sandstone
- **Dirt Group**: Dirt, coarse dirt, grass block, rooted dirt, farmland, dirt path
- **Deepslate Group**: Deepslate, cobbled deepslate, polished deepslate, deepslate bricks, deepslate tiles

When smoothing terrain, the system preserves these relationships, ensuring that geological formations remain coherent rather than blending into a uniform mass.

### Erosion Simulation

Steep slopes in nature tend to erode over time, with harder materials remaining and softer materials washing away. The enhanced smoothing system simulates this process by:

1. Detecting steep slopes in the terrain
2. Applying erosion probability based on material hardness
3. Transforming materials on steep slopes into their "eroded" versions

For example, stone on a steep slope might erode into cobblestone, while grass blocks might erode into dirt.

### Surface Layer Preservation

Natural terrain typically has a distinctive top layer - grass blocks covering dirt, sand covering sandstone, etc. The enhanced smoothing system:

- Detects and identifies the top layer of terrain
- Preserves this layer during smoothing
- Ensures natural surface appearance is maintained

This prevents underground materials from being exposed during smoothing, maintaining a realistic terrain appearance.

### Natural Variation

To avoid the artificial uniformity that often results from terrain editing, the system adds controlled natural variation:

- Randomly varies materials within the same geological group
- Maintains geological coherence while adding natural diversity
- Produces more realistic, less "obviously edited" terrain

## Command Usage

### Region Smoothing (Selection-Based)

```
//smooth [iterations] [heightFactor] [options]
```

**Parameters:**
- `iterations`: Number of smoothing passes (default: 4, range: 1-10)
- `heightFactor`: Controls vertical vs horizontal smoothing (default: 2.0, range: 0.1-5.0)
- `options`:
  - `-e` / `+e`: Disable/Enable erosion simulation (default: enabled)
  - `-p` / `+p`: Disable/Enable preserving surface layer (default: enabled)
  - `-v=N`: Set natural variation (range: 0.0-1.0, default: 0.2)

**Examples:**
- `//smooth` - Apply smoothing with default parameters
- `//smooth 6 1.5` - Apply 6 iterations with lower height factor
- `//smooth 3 2.0 -e -v=0.3` - 3 iterations, disable erosion, 30% variation
- `//smooth 5 3.0 +p -v=0` - 5 iterations, aggressive height smoothing, preserve surface, no variation

### Smooth Brush

```
//brush smooth <radius> [iterations] [heightFactor] [options]
```

**Parameters:**
- `radius`: The radius of the brush
- Other parameters are the same as region smoothing

**Examples:**
- `//brush smooth 10` - Create a smooth brush with radius 10
- `//brush smooth 5 8 1.5` - Radius 5, 8 iterations, 1.5 height factor
- `//brush smooth 15 3 2.5 -e +p -v=0.4` - Radius 15, 3 iterations, 2.5 height factor, no erosion, preserve surface, 40% variation

## Technical Details

### Material Hardness

The system assigns hardness values to different materials to simulate realistic erosion:

| Material | Hardness | Material | Hardness |
|----------|----------|----------|----------|
| Bedrock | 100 | Stone | 60 |
| Obsidian | 90 | Granite | 58 |
| Ancient Debris | 85 | Diorite | 57 |
| Deepslate | 70 | Sandstone | 45 |
| End Stone | 65 | Dirt | 25 |
| | | Sand | 15 |

Higher values indicate more erosion-resistant materials.

### Algorithm Overview

1. **Collection Phase**: All blocks in the target area are collected, including a buffer zone for neighborhood calculations
2. **Height Analysis**: The system identifies the highest solid block at each X,Z coordinate
3. **Surface Detection**: Surface materials are identified for later preservation
4. **Iteration Process**: For each smoothing iteration:
   - Each block is analyzed in context of its neighborhood
   - Geological relationships are considered
   - Erosion is simulated on steep slopes
   - Material weights are calculated based on distance and geological group
5. **Final Pass**: Surface materials are restored if the preservation option is enabled
6. **Application**: Changes are applied to the world and recorded in history for undo/redo

## Performance Considerations

The enhanced smoothing system is more computationally intensive than basic smoothing. To maintain good performance:

- Use lower iteration counts for large areas
- Reduce radius for real-time brush operations
- Consider disabling some features for very large operations

## Tips for Effective Use

- **Terrain Sculpting**: Use lower height factors (0.5-1.0) when sculpting dramatic landscapes
- **Gentle Hills**: Use higher height factors (3.0-5.0) for rolling hills and gentle terrain
- **Natural Cliffs**: Enable erosion and use a height factor around 1.5 for natural-looking cliffs
- **Layer Preservation**: Always keep surface preservation enabled when smoothing areas with mixed surface types
- **Natural Variety**: Use higher variation values (0.3-0.5) for wild, natural terrain
- **Controlled Smoothing**: Use lower variation (0.0-0.1) for more controlled, uniform results

## Permissions

- `frizzlenedit.region.smooth` - Permission to use the smooth command
- `frizzlenedit.brush.smooth` - Permission to use the smooth brush

## Troubleshooting

**Issue**: Smoothing creates holes or exposes underground materials  
**Solution**: Ensure surface preservation is enabled (+p)

**Issue**: Terrain looks too uniform after smoothing  
**Solution**: Increase natural variation (-v=0.3 or higher)

**Issue**: Steep areas look unrealistic  
**Solution**: Ensure erosion is enabled (+e) for natural-looking slopes

**Issue**: Server lag during large smoothing operations  
**Solution**: Reduce iterations or operation size, or split into smaller operations

## Examples

**Before and After Examples:**

| Scenario | Before | After | Command |
|----------|--------|-------|---------|
| Mountain Range | [Image] | [Image] | `//smooth 5 2.0 +e +p -v=0.3` |
| Cliffs | [Image] | [Image] | `//smooth 3 1.0 +e +p -v=0.2` |
| Rolling Hills | [Image] | [Image] | `//smooth 6 4.0 -e +p -v=0.1` |
| Island Shores | [Image] | [Image] | `//smooth 4 2.5 +e +p -v=0.4` |

[Return to Main Documentation](../README.md) 