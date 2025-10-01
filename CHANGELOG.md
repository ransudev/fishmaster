# FishMaster Changelog

## [1.0.0] - 2025-09-16

### Added
- Initial release of FishMaster mod
- Auto Fishing feature with fish bite detection
- Sea Creature Killer with multiple attack modes
- Auto Harp for Melody's Harp minigame
- Comprehensive GUI with animated controls
- Webhook integration for Discord notifications
- Extensive configuration system

### Fixed
- **Mouse Ungrab Issue**: Resolved problem where switching windows during auto fishing with mouse ungrab enabled would show the escape menu and interrupt automation
  - Implemented temporary pause menu prevention when auto fishing is active
  - Added window focus monitoring to maintain auto fishing state
  - Created mixins to intercept low-level Minecraft behavior
  - Added proper state restoration when auto fishing stops

### Changed
- Enhanced auto fishing with randomized delays for anti-detection
- Improved mouse simulation for more authentic interactions
- Refined configuration system with better validation
- Updated GUI with improved visual feedback

### Technical Improvements
- Added comprehensive event handling system
- Implemented thread-safe configuration management
- Enhanced error handling and logging
- Improved performance with optimized tick processing