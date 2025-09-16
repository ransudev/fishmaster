# FishMaster Installation Guide

## System Requirements

### Minimum Requirements
- **Operating System**: Windows 10/11, macOS 10.14+, or Linux
- **Java**: Java 21 (included with Minecraft Launcher)
- **Minecraft**: Version 1.21.5
- **RAM**: 4GB minimum (8GB recommended)
- **Storage**: 500MB free space

### Recommended Requirements
- **Operating System**: Windows 11, macOS 12+, or modern Linux distribution
- **Java**: Java 21
- **Minecraft**: Version 1.21.5
- **RAM**: 8GB or more
- **Storage**: 1GB free space

## Prerequisites

### 1. Install Fabric Loader
Before installing FishMaster, you need to install Fabric Loader for Minecraft 1.21.5:

1. Download the **Fabric Installer** from [https://fabricmc.net/use/](https://fabricmc.net/use/)
2. Run the installer and select:
   - **Minecraft Version**: 1.21.5
   - **Loader Version**: Latest (recommended)
   - **Install Location**: Your default Minecraft directory
3. Click "Install"

### 2. Verify Fabric Installation
After installation, launch the Minecraft Launcher and verify that a new profile named "Fabric 1.21.5" (or similar) has been created.

## Installing FishMaster

### Method 1: Manual Installation (Recommended)

1. **Download the FishMaster Mod**
   - Locate the `fishmaster-1.0.0.jar` file in `/home/ransu/Documents/IdeaProjects/fishmaster/build/libs/`
   - Copy this file to your clipboard or download location

2. **Locate Your Minecraft Directory**
   - **Windows**: `%APPDATA%\\.minecraft`
   - **macOS**: `~/Library/Application Support/minecraft`
   - **Linux**: `~/.minecraft`

3. **Navigate to Mods Folder**
   - Open your Minecraft directory
   - If there's no `mods` folder, create one
   - Path should be: `[Minecraft Directory]/mods`

4. **Install the Mod**
   - Paste the `fishmaster-1.0.0.jar` file into the `mods` folder
   - Ensure the file is directly in the mods folder, not in a subfolder

### Method 2: Using Mod Manager (If Available)

1. Open your preferred mod manager (e.g., MultiMC, Prism Launcher)
2. Navigate to your 1.21.5 Fabric instance
3. Go to the "Mods" tab
4. Click "Add" or "Import"
5. Select the `fishmaster-1.0.0.jar` file
6. Confirm installation

## Post-Installation Steps

### 1. Launch Minecraft
1. Open the Minecraft Launcher
2. Select the "Fabric 1.21.5" profile (or the profile created during Fabric installation)
3. Click "Play"

### 2. Verify Installation
Upon first launch, you should see:
- A new "Mods" button in the main menu (if using Fabric API)
- FishMaster listed in the mod list
- No error messages during startup

### 3. Initial Configuration
On first launch, FishMaster will:
1. Create a configuration file at `.minecraft/config/fishmaster/config.json`
2. Set default keybinds (B for auto fishing toggle)
3. Initialize all features with default settings

## Troubleshooting Installation Issues

### Common Issues and Solutions

#### 1. "Missing Fabric API" Error
**Symptom**: Game crashes with "Fabric API not found" message
**Solution**: 
- Download Fabric API from [https://www.curseforge.com/minecraft/mc-mods/fabric-api](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
- Install the version compatible with Minecraft 1.21.5
- Place the Fabric API jar in your mods folder alongside FishMaster

#### 2. "Mod Conflict" Warning
**Symptom**: Warning about conflicting mods during startup
**Solution**:
- Remove or disable conflicting mods
- Common conflicts: Other auto-fishing or automation mods
- FishMaster works best as a standalone automation solution

#### 3. "Failed to Load Mod" Error
**Symptom**: Game crashes during loading phase
**Solution**:
1. Check that you're using Minecraft 1.21.5
2. Verify Fabric Loader is installed correctly
3. Ensure all required dependencies are present
4. Delete the config file at `.minecraft/config/fishmaster/config.json` and restart

#### 4. "No Keybinds Working" Issue
**Symptom**: FishMaster commands don't respond to keypresses
**Solution**:
1. Check in-game controls settings
2. Verify keybinds haven't been reassigned
3. Restart Minecraft to refresh keybind registration

## Updating FishMaster

### Safe Update Process
1. **Backup Configuration** (optional but recommended):
   - Copy `.minecraft/config/fishmaster/config.json` to a safe location

2. **Remove Old Version**:
   - Delete the old `fishmaster-*.jar` file from your mods folder

3. **Install New Version**:
   - Place the new `fishmaster-1.0.0.jar` file in your mods folder

4. **Verify Update**:
   - Launch Minecraft
   - Check mod version in mod list
   - Test key functionality

### Configuration Preservation
- FishMaster is designed to preserve your settings during updates
- Configuration files are backward compatible when possible
- Major version updates may require reconfiguration

## Required Dependencies

FishMaster requires the following mods/libraries to function properly:

### Included Dependencies (No Action Required)
- **Fabric Language Kotlin**: Provides Kotlin support (included in jar)
- **Elementa**: GUI framework (included in jar)
- **UniversalCraft**: Cross-platform utilities (included in jar)

### External Dependencies (Must Be Installed Separately)
- **Fabric API**: Core Fabric modding framework
  - Version: Compatible with 1.21.5
  - Download: [CurseForge Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

## Compatibility Information

### Compatible Minecraft Versions
- ✅ **Primary Support**: 1.21.5
- ⚠️ **Secondary Support**: 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4
- ❌ **Unsupported**: Versions below 1.21 or above 1.21.5

### Compatible Mod Loaders
- ✅ **Fabric**: Fully supported
- ⚠️ **Quilt**: May work but not officially tested
- ❌ **Forge**: Not compatible

### Known Conflicting Mods
- **Other Auto-Fishing Mods**: May cause conflicts or duplication
- **Macro/Scripting Mods**: Potential keybind conflicts
- **Input Overlay Mods**: May interfere with mouse ungrab feature

## Performance Considerations

### Recommended JVM Arguments
For optimal performance, consider adding these JVM arguments:
```
-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M
```

### Resource Usage
- **RAM**: Approximately 200-300MB additional usage
- **CPU**: Minimal impact during idle periods
- **GPU**: No additional GPU requirements
- **Disk**: Minimal I/O operations

## First-Time Setup

### 1. Launch Minecraft with FishMaster
After installation, launch Minecraft using the Fabric 1.21.5 profile.

### 2. Verify Mod Loading
Check that FishMaster appears in the mod list without errors.

### 3. Test Basic Functionality
- Press the default auto-fishing keybind (B)
- You should see a notification message indicating FishMaster is enabled
- Equip a fishing rod and cast it manually
- FishMaster should automatically detect fish bites

### 4. Configure Settings (Optional)
- Use the `/fm` or `/fishmaster` command to open the configuration GUI
- Adjust settings to your preference
- Set up Discord webhook notifications (optional)

## Getting Started Guide

### Quick Start Checklist
1. ✅ Install Fabric Loader for Minecraft 1.21.5
2. ✅ Download FishMaster jar file
3. ✅ Place jar file in mods folder
4. ✅ Launch Minecraft with Fabric profile
5. ✅ Verify no errors in game log
6. ✅ Test auto-fishing feature with keybind (B)
7. ✅ Configure settings as needed

### Essential First Steps
1. **Test Auto Fishing**:
   - Equip a fishing rod
   - Press B to enable auto fishing
   - Cast your line and verify automatic reeling

2. **Configure Notifications** (Optional):
   - Use `/fmwh set [DISCORD_WEBHOOK_URL]` to set up Discord notifications
   - Enable with `/fmwh enable`

3. **Adjust Settings**:
   - Open GUI with `/fm` command
   - Customize delays and keybinds
   - Enable/disable features as needed

## Support Resources

### Community Support
- **GitHub Issues**: [https://github.com/Rohan08521/fishmaster/issues](https://github.com/Rohan08521/fishmaster/issues)
- **Discord**: Join the FishMaster community server (link in README)

### Documentation
- **User Guide**: Detailed usage instructions
- **Developer Guide**: Information for contributors
- **Changelog**: Version history and updates

### Reporting Issues
When reporting issues, please include:
1. **Minecraft Version**
2. **FishMaster Version**
3. **Complete Crash Log** (if applicable)
4. **Steps to Reproduce**
5. **Other Mods Installed`