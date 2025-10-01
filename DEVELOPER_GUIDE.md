# FishMaster Developer Guide

This guide provides technical documentation for developers working on the FishMaster mod.

## Project Structure

```
src/
├── client/
│   ├── java/
│   │   └── rohan/
│   │       └── fishmaster/
│   │           ├── command/       # Command implementations
│   │           ├── config/        # Configuration system
│   │           ├── core/          # Core utilities
│   │           ├── data/          # Data models
│   │           ├── event/         # Event handlers
│   │           ├── feature/       # Main feature implementations
│   │           ├── handler/       # Various handlers
│   │           ├── keybind/       # Keybind management
│   │           ├── mixin/         # Mixin implementations
│   │           ├── render/        # Rendering utilities
│   │           ├── util/          # Utility classes
│   │           ├── utils/         # Additional utilities
│   │           └── FishMasterClient.java  # Main mod entry point
│   ├── kotlin/
│   │   └── rohan/
│   │       └── fishmaster/
│   │           ├── gui/           # GUI implementation
│   │           └── qol/          # Quality of life features
│   └── resources/                 # Resource files
└── main/                          # Main source set (empty in this mod)
```

## Key Components

### AutoFishingFeature
The core of the fishing automation system.

**Key Methods:**
- `toggle()` - Enables/disables auto fishing
- `tick()` - Main processing loop
- `startCasting()` - Handles fishing rod casting
- `detectArmorStandFishBite()` - Detects fish bites using armor stands
- `simulateRightClick()` - Simulates mouse clicks for authentic interaction

**State Management:**
- `enabled` - Whether auto fishing is active
- `isFishing` - Whether currently fishing
- `currentState` - Current fishing state (IDLE, CASTING, FISHING)

### SeaCreatureKiller
Automatically eliminates sea creatures during fishing.

**Key Methods:**
- `tick()` - Main processing loop
- `enterCombat()` - Initiates combat with a sea creature
- `exitCombat()` - Exits combat mode
- `isTargetSeaCreature()` - Determines if an entity is a target

### Configuration System
Persistent JSON-based configuration management.

**Key Features:**
- Thread-safe operations
- Automatic saving with batching
- Validation of input values
- Default value handling

### GUI System
Kotlin-based modern interface using Elementa.

**Key Components:**
- `FishmasterScreen` - Main GUI screen
- `AnimatedToggleSwitch` - Animated toggle controls
- `AnimatedDropdown` - Animated dropdown menus
- `AnimatedCycleButton` - Animated cycling buttons

## Mouse Ungrab Feature Implementation

### Problem
When the mouse is ungrabbed during auto fishing and the user switches windows, Minecraft shows the escape menu, interrupting the automation.

### Solution Overview
The solution involves temporarily modifying Minecraft's pause behavior when auto fishing is active with mouse ungrab enabled.

### Key Files

#### 1. WindowFocusEvents.java
Handles window focus changes:
- Monitors window focus state using tick events
- Notifies AutoFishingFeature of focus changes
- Maintains state information

#### 2. AutoFishingFeature.java (Enhanced)
Added methods for pause menu management:
- `preventPauseMenu()` - Temporarily disables `pauseOnLostFocus`
- `restorePauseMenuSetting()` - Restores original setting
- `shouldPreventPauseMenu()` - Determines when to prevent pause menu
- `onWindowFocusChanged()` - Handles focus change events

#### 3. MinecraftClientMixin.java
Intercepts window focus changes at the Minecraft client level:
- Mixin for `MinecraftClient.onWindowFocusChanged`
- Calls AutoFishingFeature handlers
- Maintains normal behavior when not auto fishing

#### 4. InGameHudMixin.java
Prevents pause menu rendering:
- Redirects `MinecraftClient.isPaused()` check
- Prevents pause menu display when auto fishing is active
- Preserves normal behavior otherwise

### Implementation Details

#### State Management
- `originalPauseOnLostFocus` - Stores the original pause setting
- `wasWindowFocused` - Tracks previous window focus state
- `wasAutoFishingEnabled` - Tracks auto fishing state during focus changes

#### Flow
1. **Auto Fishing Start:**
   - Store original `pauseOnLostFocus` value
   - Set `pauseOnLostFocus = false`
   - Enable mouse ungrab

2. **Window Loses Focus:**
   - AutoFishingFeature.onWindowFocusChanged(false) called
   - Maintain auto fishing state
   - Keep mouse ungrabbed

3. **Window Gains Focus:**
   - AutoFishingFeature.onWindowFocusChanged(true) called
   - Resume normal operation
   - Maintain mouse state

4. **Auto Fishing Stop:**
   - Restore original `pauseOnLostFocus` value
   - Re-enable normal pause behavior

### Benefits
- **Non-Intrusive**: Only affects behavior when auto fishing is active
- **Reversible**: Completely restores original settings when disabled
- **Compatible**: Works with existing Minecraft features
- **Configurable**: Respects user's mouse ungrab setting

## Mixin System

### Configuration
Defined in `fishmaster.client.mixins.json`:
```json
{
  "required": true,
  "package": "rohan.fishmaster.mixin.client",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "FishingBobberEntityMixin",
    "InGameHudMixin",
    "MouseMixin",
    "MinecraftClientMixin"
  ]
}
```

### Key Mixins

#### MouseMixin
Provides access to mouse button simulation:
```java
@Mixin(Mouse.class)
public interface MouseMixin {
    @Invoker("onMouseButton")
    void invokeOnMouseButton(long window, int button, int action, int mods);
}
```

#### MinecraftClientMixin
Intercepts window focus changes:
```java
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "onWindowFocusChanged", at = @At("TAIL"))
    private void onWindowFocusChanged(boolean focused, CallbackInfo ci) {
        // Handle focus change
    }
}
```

#### InGameHudMixin
Prevents pause menu display:
```java
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isPaused()Z"))
    private boolean redirectIsPaused(MinecraftClient client) {
        // Prevent pause menu when auto fishing
    }
}
```

## Event System

### Registration
Events are registered in `FishMasterClient.onInitializeClient()`:
- `ClientTickEvents.END_CLIENT_TICK` - Main processing loop
- `ClientPlayConnectionEvents.DISCONNECT` - Connection handling
- `ClientPlayConnectionEvents.JOIN` - World join handling

### Custom Events
- `FishingEvents` - Core fishing-related events
- `WindowFocusEvents` - Window focus management
- `ClientTickHandler` - Tick-based processing

## Build System

### Gradle Configuration
Key dependencies:
```gradle
dependencies {
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.120.0+1.21.5"
    modImplementation "net.fabricmc:fabric-language-kotlin:1.13.2+kotlin.2.1.20"
    implementation(include("gg.essential:elementa:710"))
    modImplementation(include("gg.essential:universalcraft-1.21.5-fabric:427"))
}
```

### Building
```bash
# Compile Java code
./gradlew compileClientJava

# Build mod jar
./gradlew remapJar

# Full build
./gradlew build
```

## Testing

### Manual Testing
1. Enable auto fishing with mouse ungrab
2. Switch to another window
3. Verify escape menu doesn't appear
4. Confirm auto fishing continues
5. Return to Minecraft and verify normal operation

### Automated Testing
Currently no automated tests are implemented. Consider adding:
- Unit tests for configuration system
- Integration tests for feature interactions
- Performance benchmarks

## Code Standards

### Naming Conventions
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Variables: `camelCase`

### Documentation
- All public methods should have JavaDoc comments
- Complex logic should have inline comments
- Class-level documentation for major components

### Error Handling
- Graceful degradation when possible
- Meaningful error messages
- Proper logging for debugging

## Contributing

### Code Review Process
1. All changes require review
2. Follow established patterns
3. Maintain backward compatibility
4. Ensure proper error handling

### Pull Request Guidelines
1. Include description of changes
2. Reference related issues
3. Add tests if applicable
4. Update documentation

## Troubleshooting

### Common Issues

#### Build Failures
- Ensure correct Java version (21)
- Check Fabric dependencies
- Verify mixin configuration

#### Runtime Errors
- Check Minecraft logs for mixin errors
- Verify configuration file format
- Confirm mod compatibility

#### Feature Issues
- Test with default configuration
- Check for conflicting mods
- Verify Minecraft version compatibility

### Debugging Tips
- Enable debug mode in GUI for detailed logging
- Check `.minecraft/logs/latest.log`
- Use breakpoints in development environment
- Monitor network traffic if needed

## Future Improvements

### Planned Features
- Enhanced anti-detection mechanisms
- Additional fishing modes
- Improved GUI customization
- Performance optimizations

### Technical Debt
- Add comprehensive test suite
- Improve code documentation
- Optimize resource usage
- Enhance error handling