# StrideSurfer Fishing Mode - Feature Documentation

## Overview
The StrideSurfer Fishing Mode is a specialized fishing mechanic for Minecraft 1.21.5 Fabric that automatically accumulates 20-30 StrideSurfers before killing them all with melee attacks using the "Figstone Splitter" weapon.

## Features

### 1. **Automatic StrideSurfer Accumulation**
- Detects and tracks StrideSurfers as they spawn from fishing
- Waits until 20-30 StrideSurfers are nearby (random count each cycle)
- Does NOT kill them immediately - allows accumulation

### 2. **Melee Kill Mode**
- Automatically equips "Figstone Splitter" weapon when target count is reached
- Performs rapid melee attacks (left-click) on all nearby StrideSurfers
- Uses smooth rotation to face targets
- 500ms cooldown between attacks for realistic combat

### 3. **Automatic Fishing Loop**
- Resumes fishing automatically after all StrideSurfers are eliminated
- Generates new random target count (20-30) for next cycle
- Seamlessly integrates with existing auto-fishing system

## How to Use

### Keybind Setup
1. **Default Keybind**: `N` key
2. **Configurable**: Can be changed in the config file at:
   ```
   .minecraft/config/fishmaster/config.json
   ```
   Look for: `"strideSurferFishingKeybind"`

### Activation
1. Press the keybind (default: `N`) to toggle StrideSurfer Fishing Mode
2. The mod will automatically:
   - Enable Auto Fishing (if not already enabled)
   - Enable Sea Creature Killer
   - Switch to "StrideSurfer Melee" mode

### Requirements
- **Weapon**: Must have "Figstone Splitter" in your hotbar
- **Fishing Rod**: Any fishing rod in your inventory
- **Location**: Must be in a valid fishing area where StrideSurfers spawn

## Configuration

### Config File Location
```
.minecraft/config/fishmaster/config.json
```

### Relevant Settings
```json
{
  "strideSurferFishingKeybind": 78,  // 78 = N key (GLFW key code)
  "strideSurferFishingEnabled": false,
  "seaCreatureKillerMode": "StrideSurfer Melee",
  "seaCreatureKillerEnabled": true
}
```

### Keybind Codes (GLFW)
- `66` = B key
- `78` = N key
- `77` = M key
- `75` = K key
- See [GLFW Key Codes](https://www.glfw.org/docs/latest/group__keys.html) for full list

## Technical Implementation

### Files Created/Modified

#### New Files:
1. **StrideSurferMode.java**
   - Location: `src/client/java/rohan/fishmaster/feature/seacreaturekiller/`
   - Implements the melee kill logic
   - Tracks StrideSurfers and manages kill mode

2. **StrideSurferFishingHandler.java**
   - Location: `src/client/java/rohan/fishmaster/handler/`
   - Handles keybind detection and mode toggling
   - Auto-enables required features

3. **STRIDESURFER_FISHING_GUIDE.md**
   - This documentation file

#### Modified Files:
1. **FishMasterConfig.java**
   - Added `strideSurferFishingKeybind` config option
   - Added `strideSurferFishingEnabled` state tracking
   - Added getters and setters for new config options

2. **SeaCreatureKiller.java**
   - Integrated StrideSurferMode into mode system
   - Added mode switching logic

3. **FishMasterClient.java**
   - Initialized StrideSurferFishingHandler on mod startup

### Mode System Architecture

The StrideSurfer mode extends the existing `SeaCreatureKillerMode` base class:

```
SeaCreatureKillerMode (abstract)
├── RCMMode (right-click mage weapons)
├── FireVeilWandMode (fire veil wand attacks)
└── StrideSurferMode (melee attacks) ← NEW
```

### Combat Flow

```
1. Fish cast → StrideSurfer spawns
2. Track StrideSurfer (don't kill)
3. Count nearby StrideSurfers
4. If count < target (20-30): Continue fishing
5. If count >= target:
   a. Enter Kill Mode
   b. Equip "Figstone Splitter"
   c. Attack all StrideSurfers with melee
   d. Wait until all are dead
6. Exit Kill Mode
7. Resume fishing (goto step 1)
```

## Chat Messages

### Mode Activation
```
[FishMaster] StrideSurfer Fishing Mode: ENABLED
  → Auto Fishing: ENABLED
  → Sea Creature Killer: StrideSurfer Melee
  → Target: 20-30 StrideSurfers before attack
```

### During Accumulation
```
SCK: StrideSurfers: 5/25
SCK: StrideSurfers: 10/25
SCK: StrideSurfers: 15/25
SCK: StrideSurfers: 20/25
```

### Kill Mode Activated
```
SCK: KILL MODE - 25 StrideSurfers detected!
```

### Combat Complete
```
SCK: All StrideSurfers eliminated! Resuming fishing...
```

### Weapon Not Found
```
SCK: Weapon 'Figstone Splitter' not found!
```

## Troubleshooting

### StrideSurfers Not Being Tracked
- **Check**: Is StrideSurfer Fishing Mode enabled? (Press `N` key)
- **Check**: Is Auto Fishing active?
- **Check**: Is Sea Creature Killer enabled?

### Weapon Not Equipping
- **Solution**: Ensure "Figstone Splitter" is in your hotbar (slots 1-9)
- **Check**: Weapon name must match exactly (case-sensitive)

### Not Attacking After Accumulation
- **Check**: Is the weapon equipped?
- **Check**: Are StrideSurfers within 6 block range?
- **Solution**: Try moving closer to the fishing spot

### Mode Doesn't Toggle
- **Check**: Config file for correct keybind code
- **Check**: No GUI/menu is open when pressing keybind
- **Solution**: Reload the mod or restart Minecraft

## Performance Notes

- **Attack Cooldown**: 500ms between attacks (configurable in code)
- **Detection Range**: 6 blocks radius
- **Rotation Speed**: 200ms smooth rotation to targets
- **Click Delay**: 10-30ms random delay for human-like behavior

## Compatibility

- **Minecraft Version**: 1.21.5
- **Mod Loader**: Fabric
- **Dependencies**: 
  - Fabric API
  - Fabric Language Kotlin 1.10.0+
  - Java 21+

## Future Enhancements

Potential improvements for future versions:
- [ ] Configurable target count range (currently 20-30)
- [ ] Configurable weapon name
- [ ] GUI settings panel
- [ ] Multiple weapon support
- [ ] Custom attack patterns
- [ ] Statistics tracking (kills, efficiency, etc.)

## Credits

- **Implementation**: Minecraft 1.21.5 Fabric adaptation
- **Base Logic**: Inspired by 1.8.9 MobKiller system
- **Mode System**: Extends existing SeaCreatureKiller framework

## License

This feature is part of the FishMaster mod and follows the same license (MIT).

---

**Last Updated**: 2025-10-01  
**Version**: 1.0.0  
**Minecraft**: 1.21.5  
**Fabric Loader**: 0.16.10+
