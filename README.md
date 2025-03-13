# FrizzlenEdit

FrizzlenEdit is a high-performance WorldEdit alternative for Minecraft, designed to efficiently manipulate the Minecraft world through a powerful set of commands, brushes, and operations.

![FrizzlenEdit Logo](docs/images/logo.png)

## Overview

FrizzlenEdit provides a comprehensive toolkit for players and server administrators to create, modify, and enhance Minecraft worlds. Built with performance in mind, it handles large operations efficiently while providing an intuitive interface for both basic and advanced world editing.

## Key Features

- **Selection System**: Create and manipulate regions with precision
- **Block Operations**: Set, replace, and transform blocks quickly
- **Advanced Brush System**: Edit terrain interactively with customizable brushes
- **Geometric Structures**: Create cylinders, spheres, and more
- **Clipboard Functionality**: Copy, cut, and paste structures
- **Schematic Support**: Save and load structures
- **History System**: Undo and redo operations
- **Enhanced Terrain Tools**: Advanced smoothing with geological awareness

## Getting Started

### Installation

1. Download the latest FrizzlenEdit JAR file
2. Place the JAR in your server's `plugins` folder
3. Restart your server
4. Start editing with the commands listed below!

### Basic Commands

- `//wand` - Get a selection wand
- `//pos1` and `//pos2` - Set selection points
- `//set <block>` - Fill selection with a block type
- `//replace <from> <to>` - Replace blocks in selection
- `//copy` and `//paste` - Copy and paste selections
- `//undo` and `//redo` - Manage history

## Detailed Documentation

For more information on specific features, check out these detailed guides:

- [Selection System](docs/SELECTION.md)
- [Block Operations](docs/BLOCK_OPERATIONS.md)
- [Brushes](docs/BRUSHES.md)
- [Clipboard](docs/CLIPBOARD.md)
- [Schematic System](docs/SCHEMATICS.md)
- [History System](docs/HISTORY.md)
- [Enhanced Smoothing](docs/ENHANCED_SMOOTHING.md)

## Enhanced Smoothing System

FrizzlenEdit features an advanced terrain smoothing system that goes beyond simple averaging. The enhanced smoothing:

- Respects geological formations by keeping related materials together
- Simulates natural erosion on steep slopes
- Preserves the distinctive top layer of terrain
- Adds natural variation to avoid artificial uniformity
- Offers extensive customization options

Learn more about the smoothing capabilities in the [Enhanced Smoothing Guide](docs/ENHANCED_SMOOTHING.md).

## Permissions

FrizzlenEdit uses a permission-based system to control access to different features:

- `frizzlenedit.*` - All permissions
- `frizzlenedit.region.*` - All region manipulation permissions
- `frizzlenedit.brush.*` - All brush permissions
- `frizzlenedit.clipboard.*` - All clipboard permissions
- `frizzlenedit.history.*` - History manipulation permissions

For a complete list of permissions, see the [Permissions Guide](docs/PERMISSIONS.md).

## Configuration

FrizzlenEdit can be configured through the `config.yml` file. This allows server administrators to:

- Set limits on operation sizes
- Configure performance settings
- Adjust brush parameters
- Customize behavior for tools

See the [Configuration Guide](docs/CONFIGURATION.md) for detailed information.

## Integration

FrizzlenEdit is designed to work alongside other plugins and can be integrated with:

- Permission systems
- Economy plugins
- Custom server setups

## Contributing

Contributions to FrizzlenEdit are welcome! Whether it's bug reports, feature requests, or code contributions, please follow our [Contribution Guidelines](CONTRIBUTING.md).

## License

FrizzlenEdit is licensed under [LICENSE NAME]. See the [LICENSE](LICENSE) file for details.

## Credits

FrizzlenEdit is developed and maintained by the FrizzlenPop team. 