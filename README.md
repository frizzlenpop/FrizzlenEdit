# FrizzlenEdit

A powerful Minecraft world editing plugin with extensive schematic, clipboard, and brush functionality for efficient world manipulation.

## Command Reference

### General Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `/fe` | Main plugin command | |
| `/fe help` | Show plugin help | |
| `/fe reload` | Reload the configuration | |
| `/fe version` | Show the plugin version | |

### Selection Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `//wand` | Get a selection wand | |
| `//pos1` | Set position 1 to your current location | |
| `//pos2` | Set position 2 to your current location | |
| `//expand <amount> [direction]` | Expand the selection | `<amount>`: Number of blocks to expand<br>`[direction]`: Optional direction (up, down, north, south, east, west, or relative direction) |
| `//contract <amount> [direction]` | Contract the selection | `<amount>`: Number of blocks to contract<br>`[direction]`: Optional direction |
| `//size` | Show the size of the selection | |

### Clipboard Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `//copy` | Copy the selection to clipboard | |
| `//cut` | Cut the selection to clipboard | |
| `//paste` | Paste from clipboard | |
| `//pastelarge` | Paste large clipboard content in batches | `[batch <size>]`: Set batch size<br>`[delay <ticks>]`: Set tick delay between batches |
| `//flip <direction>` | Flip the clipboard | `<direction>`: Direction to flip (up, down, north, south, east, west) |
| `//rotate <degrees>` | Rotate the clipboard | `<degrees>`: Rotation angle in degrees (90, 180, 270) |

### Block Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `//set <block>` | Set all blocks in the selection | `<block>`: Block type to set |
| `//replace <from> <to>` | Replace blocks in the selection | `<from>`: Block type to replace<br>`<to>`: Block type to replace with |

### History Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `//undo` | Undo the last operation | |
| `//redo` | Redo the last undone operation | |
| `//clearhistory` | Clear your history | |

### Brush Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `//brush` | Main brush command | |
| `//brush sphere <material> <radius>` | Create a sphere brush | `<material>`: Block material for the brush<br>`<radius>`: Radius of the sphere |
| `//brush cylinder <material> <radius> <height>` | Create a cylinder brush | `<material>`: Block material for the brush<br>`<radius>`: Radius of the cylinder<br>`<height>`: Height of the cylinder |
| `//brush smooth <radius>` | Create a smooth brush | `<radius>`: Radius of the smooth operation |
| `//brush none` | Remove the current brush | |
| `//mask <material>` | Set a material mask for the brush | `<material>`: Material to use as mask |
| `//mask none` | Remove the current mask | |
| `//brushtool` | Toggle brush tool mode | |

### Schematic Commands

| Command | Description | Arguments |
|---------|-------------|-----------|
| `//schematic save <name>` | Save your selection as a schematic | `<name>`: Name of the schematic |
| `//schematic load <name>` | Load a schematic to your clipboard | `<name>`: Name of the schematic |
| `//schematic delete <name>` | Delete a schematic | `<name>`: Name of the schematic |
| `//schematic list` | List available schematics | |
| `//schematic formats` | List supported schematic formats | |
| `//schematic paste <name> [noair]` | Paste a schematic at your location | `<name>`: Name of the schematic<br>`[noair]`: Optional flag to not paste air blocks |
| `//schematic pastelarge <name> [noair] [batch <size>] [delay <ticks>] [noadaptive]` | Paste a large schematic with adaptive performance | `<name>`: Name of the schematic<br>`[noair]`: Optional flag to not paste air blocks<br>`[batch <size>]`: Set batch size<br>`[delay <ticks>]`: Set tick delay between batches<br>`[noadaptive]`: Disable adaptive performance |
| `//schematic adaptivepaste <name> [noair]` | Paste with automatic performance optimization | `<name>`: Name of the schematic<br>`[noair]`: Optional flag to not paste air blocks |

## Argument Details

- `<name>`: Name of the schematic file (without file extension)
- `<block>` or `<material>`: Block type in Minecraft format (e.g., "stone", "oak_planks", "minecraft:grass_block")
- `<amount>`: A positive integer
- `<radius>`, `<height>`: Positive numbers defining brush dimensions
- `[noair]`: Optional flag to skip air blocks when pasting
- `[batch <size>]`: Number of blocks to process per batch (default from config)
- `[delay <ticks>]`: Delay between batches in server ticks (default from config)
- `[noadaptive]`: Disable adaptive performance optimization
- `<direction>`: One of: up, down, north, south, east, west
- `<degrees>`: Rotation angle: 90, 180, or 270

## Adaptive Performance System

The plugin includes an adaptive performance system that automatically adjusts batch sizes and delays when pasting large schematics. This system:

- Monitors server TPS (Ticks Per Second) in real-time
- Adjusts batch sizes based on current server performance
- Increases or decreases delay between batches as needed
- Can be enabled or disabled with the `[noadaptive]` flag when using `//schematic pastelarge`
- Is used by default in both `//schematic pastelarge` and `//schematic adaptivepaste`

## Permissions

Each command requires a corresponding permission. The permission format follows the pattern:
`frizzlenedit.<category>.<command>`

Examples:
- `frizzlenedit.admin.reload` - Permission to reload the plugin
- `frizzlenedit.selection.wand` - Permission to use the selection wand
- `frizzlenedit.schematic.paste` - Permission to paste schematics 