# FishMaster - Advanced Fishing Automation Mod

FishMaster is a Minecraft Fabric mod for version 1.21.5 that provides automation features for Hypixel Skyblock, particularly focused on fishing and related activities.

## Features

### Auto Fishing System
- **Smart Fishing Automation**: Automatically casts and reels in fishing rods based on fish bite detection
- **Fish Bite Detection**: Uses armor stand entities with "!!!" name tags to detect when a fish bites
- **Configurable Delays**: Customizable recast and reeling delays with randomized variations to avoid detection
- **Failsafe Mechanisms**: Health checks, emergency stops, and connection loss detection
- **Anti-Detection Features**: Randomized delays to mimic human behavior

### Sea Creature Killer (SCK)
- **Automatic Sea Creature Elimination**: Detects and eliminates sea creatures that appear during fishing
- **Multiple Attack Modes**:
  - RCM (Right Click Mage) - Looks down and right-clicks for ground-based attacks
  - Fire Veil Wand - Uses the Fire Veil Wand ability
- **Target Recognition**: Maintains a comprehensive list of sea creatures to target
- **Smart Weapon Switching**: Automatically switches between fishing rod and combat weapons

### Auto Harp
- **Melody's Harp Automation**: Automatically completes the harp minigame in Hypixel Skyblock
- **Quartz Block Detection**: Identifies and clicks quartz blocks in the harp interface
- **Change Detection**: Uses inventory hashing to detect when to make moves

### Configuration System
- **Persistent Settings**: JSON-based configuration that saves between sessions
- **Extensive Customization**: Over 20 configurable parameters including:
  - Auto fishing keybind
  - Recast/reeling delays
  - Health thresholds
  - Mouse ungrab settings
  - Webhook notifications
  - SCK settings

### GUI System
- **Modern Interface**: Kotlin-based GUI with animated components using Elementa
- **Tabbed Navigation**: Main, Misc, Failsafes, and Extras tabs
- **Interactive Controls**: Toggle switches, dropdowns, cycle buttons, and keybind settings
- **Visual Feedback**: Smooth animations and color transitions

## Mouse Ungrab Feature Fix

### Problem
When using the auto fishing feature with mouse ungrab enabled, switching to another window would cause Minecraft to show the escape menu, interrupting the fishing automation.

### Solution
Implemented a comprehensive fix that prevents the escape menu from appearing when switching windows during auto fishing:

1. **Temporary Configuration Change**: When auto fishing is enabled with mouse ungrab, the mod temporarily disables Minecraft's `pauseOnLostFocus` option
2. **Focus Event Handling**: Added window focus monitoring to maintain auto fishing state when switching applications
3. **Mixin Interception**: Created mixins to intercept low-level Minecraft behavior and prevent pause menu display
4. **State Restoration**: Properly restores original settings when auto fishing is disabled

### How It Works
- When auto fishing starts with mouse ungrab enabled:
  - Temporarily disables `pauseOnLostFocus` to prevent escape menu
  - Maintains mouse ungrab for background usage
  
- When switching to another window:
  - Auto fishing continues uninterrupted
  - No escape menu appears
  - Mouse remains ungrabbed

- When returning to Minecraft:
  - Auto fishing resumes normally
  - Mouse state is properly maintained

- When auto fishing stops:
  - Original `pauseOnLostFocus` setting is restored
  - Normal Minecraft behavior resumes

### Benefits
- **Seamless Background Operation**: FishMaster can run in the background while you use other applications
- **No Interruptions**: Switching windows no longer interrupts fishing automation
- **Preserved Functionality**: All other Minecraft features work normally when not auto fishing
- **Configurable**: Feature can be enabled/disabled through the mod's configuration

## Webhook Integration
- **Discord Notifications**: Sends fishing status updates to Discord webhooks
- **Health Checks**: Periodic status reports with configurable intervals
- **Emergency Alerts**: Notifies when failsafes are triggered

## Installation

1. Install Fabric Loader for Minecraft 1.21.5
2. Place the FishMaster jar file in your `.minecraft/mods` folder
3. Launch Minecraft with the Fabric profile

## Usage

1. **Enable Auto Fishing**: Use the configured keybind (default: B) or the GUI
2. **Configure Settings**: Open the GUI with `/fm` or `/fishmaster` command
3. **Set Up Webhooks**: Use `/fmwh set <webhook_url>` to configure Discord notifications
4. **Customize SCK**: Set your preferred attack mode and mage weapon

## Commands

- `/fm` or `/fishmaster` - Open the configuration GUI
- `/fmwh set <url>` - Set Discord webhook URL
- `/fmwh enable` - Enable webhook notifications
- `/fmwh disable` - Disable webhook notifications
- `/fmwh test` - Send a test message to the configured webhook
- `/setmageweapon` - Set custom mage weapon for SCK
- `/clearmageweapon` - Clear custom mage weapon

## Configuration

All settings are stored in `.minecraft/config/fishmaster/config.json` and can be modified through the GUI or directly in the file.

## Technical Details

### Core Components
- **Mod Entry Point**: `FishMasterClient` initializes all systems
- **Configuration**: `FishMasterConfig` manages persistent settings
- **Command System**: `/fm`, `/fishmaster`, `/fmwh` commands for control
- **Event Handling**: Tick-based processing for all automated features

### Key Technical Features
- **Mixin System**: Uses Minecraft mixins for low-level integration
- **Mouse Simulation**: Direct mouse event simulation for more authentic interactions
- **Rotation Handling**: Smooth player rotation for combat features
- **Multi-threading**: Thread-safe configuration with batched saves
- **Kotlin-Java Interop**: Mixed language codebase with proper bridging

### Dependencies
- **Fabric API**: Core modding framework
- **Elementa**: GUI library for the configuration interface
- **Kotlin**: Language support for modern features
- **Essential.gg Libraries**: Elementa and UniversalCraft for UI components

## Development

### Building
```bash
./gradlew build
```

### Compiling
```bash
./gradlew compileClientJava
```

### Creating Mod Jar
```bash
./gradlew remapJar
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.