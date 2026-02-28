# Changelog

All notable changes to AdvancedProtection will be documented in this file.

## [1.5.0] - 2025-02-28

### 🎉 Major Update — Effects System, Safe Teleport & Full i18n

#### ✨ New Features
- **Zone Effects System** ⚡
  - 12 purchasable potion effects (Speed, Haste, Strength, Jump, Regeneration, Resistance, Fire Resistance, Water Breathing, Night Vision, Saturation, Dolphin's Grace, Conduit Power)
  - Upgradeable effect levels with configurable costs
  - Auto-application to all players inside protection zone
  - Per-effect permissions (advancedprotection.effects.speed, etc.)
  - Professional GUI with buy/upgrade/remove functionality
  - Members-only mode option

- **Safe Teleport System** 🏠
  - `/ap tp [id]` now finds a SAFE location above the protection
  - 4-strategy algorithm: scan upward → highest block → adjacent blocks → world top
  - Never suffocates players, even for underground protections
  - Configurable warmup, cooldown, and cost
  - Bypass warmup permission for admins

- **100% Translation System** 🌍
  - ALL text in the plugin is now translatable
  - 50+ new message keys in both Spanish and English
  - Effect names translate per language
  - Admin GUI fully translatable (roles, permissions, pagination)
  - Member permission messages translatable
  - Tax system messages translatable

- **`/ap give` Command** 🎁
  - Give protection blocks to players as admin
  - Syntax: `/ap give <player> <level> [amount]`
  - Permission: `advancedprotection.admin.give`

#### 🐛 Bug Fixes
- Fixed hardcoded English text appearing in Spanish mode
- Fixed admin GUI showing untranslated strings
- Fixed permission toggle messages not following language
- Fixed role names not translating in member menu
- Fixed particle error message hardcoded in English
- Fixed tax messages using hardcoded English
- Fixed max level error not using config message
- Removed unused imports

#### 🔧 Technical Improvements
- EffectsManager now reads effect names from language config
- Removed static getEffectDisplayName() — now instance-based
- All ChatColor hardcoded messages replaced with getMessage()
- Clean code with no unused imports
- Full LuckPerms integration for permissions
- New permissions: tp, effects, give, limits, language

---

## [1.4.0] - 2024-12-04

### 🎉 Major Update - Granular Permissions System

#### ✨ Features Added
- **Complete Granular Permission System**
  - Visual GUI for managing individual member permissions
  - 10 different permission types (Build, Place, Break, Interact, Containers, Manage Members, etc.)
  - 5 predefined roles (Visitor, Member, Builder, Moderator, Co-Owner)
  - Real-time permission toggle with visual feedback
  - Custom role support when permissions are manually edited
  - Full persistence in JSON storage
  - Left-click member to manage permissions, right-click to remove

- **Enhanced Member Management**
  - Visual permission indicators (green/red status)
  - Quick role application buttons
  - Current role display with permission count
  - Instant menu refresh on changes

#### 🐛 Bug Fixes
- Fixed ghost holograms persisting after plugin folder deletion
- Added automatic cleanup of orphaned TextDisplay entities on plugin start
- Improved hologram removal on plugin shutdown
- Fixed hologram state synchronization issues

#### 🔧 Technical Improvements
- Enhanced JSON persistence for MemberData
- Better memory management for hologram entities
- Improved entity cleanup procedures
- More robust permission checking system

---

## [1.3.0] - 2024-12-03

### 🌐 Internationalization Update

#### ✨ Features
- Complete English translation
- Improved Spanish translations
- Language switcher in config
- Translated all GUI messages
- Translated console messages

#### 🐛 Bug Fixes
- Fixed excessive debug messages in console
- Cleaned up spam logs in GuiManager
- Improved error messages

---

## [1.2.0] - 2024-12-03

### 🎨 Visual Effects Update

#### ✨ Features
- Hologram toggle system
- Enhanced visual effects manager
- 3D rotating crystal borders
- Improved particle effects
- Custom hologram formats

#### 🐛 Bug Fixes
- Fixed hologram duplication issues
- Resolved hologram persistence problems
- Fixed visual effect conflicts

---

## [1.1.0] - 2024-12-03

### 💰 Tax System Update

#### ✨ Features
- Automatic tax collection system
- Configurable tax rates per level
- Grace period implementation
- Multi-tier warning notifications
- Login reminders for overdue taxes

---

## [1.0.0] - 2024-12-03

### 🎉 Initial Release

#### ✨ Features Added
- **Core Protection System**
  - 6 upgradeable protection levels (Coal to Obsidian)
  - Dynamic radius expansion (10m to 100m)
  - Per-level customizable costs and materials
  - WorldGuard integration for enhanced compatibility
  - Collision detection to prevent overlapping claims

- **Granular Permission System**
  - Individual member permissions (Build, Break, Interact, Containers, etc.)
  - Predefined roles: Visitor, Member, Builder, Moderator, Co-Owner
  - Custom permission combinations per player
  - Full permission hierarchy with FULL_ACCESS override
  - Configurable default roles in config.yml

- **Tax & Rent System**
  - Automatic tax collection with configurable intervals
  - Percentage or fixed-rate tax modes
  - Custom tax rates per protection level
  - Grace period before protection deletion (default: 72h)
  - Automatic notifications (24h, 12h, 6h, 1h warnings)
  - Login reminders for overdue taxes
  - Toggle-able rent system per protection

- **Visual Effects**
  - 7 customizable particle effects (Crystals, Flames, Hearts, Notes, Witch Magic, Lava, Cloud)
  - Holographic border visualization with rotating crystals
  - 3D rotating crystal displays
  - Real-time TextDisplay holograms (Minecraft 1.19.4+)
  - DecentHolograms support for older versions
  - Purchasable visual upgrades via economy
  - Toggle-able info holograms per protection

- **GUI System**
  - Intuitive inventory-based menus
  - Shop system for purchasing protections
  - Member management interface with add/remove capabilities
  - Flag configuration GUI (PVP, Mob Spawning, TNT, Fire Spread, etc.)
  - Visual effect selector with previews
  - Admin management panel with pagination
  - Protection details view with hologram toggle

- **Commands & Control**
  - `/ap menu` - Main protection menu
  - `/ap visualize` - Show protection borders
  - `/ap trust/untrust` - Member management
  - `/ap trustlist` - View all members
  - `/ap rename/clearname` - Custom protection names
  - `/ap paytax` - Tax payment system
  - `/ap logs` - Admin log viewing
  - `/ap manager` - Admin panel
  - `/ap reload` - Configuration reload
  - Full tab completion for all commands

- **Multi-Language Support**
  - English and Spanish included
  - Easy to add custom languages
  - In-game language switcher
  - Fully translatable config.yml messages

- **Economy Integration**
  - Vault economy support
  - Configurable costs for all protection levels
  - Visual effect purchase system
  - Tax/rent payment system

- **Additional Features**
  - Entry/Exit messages with cooldown system
  - Protection logging with event tracking
  - JSON data storage for reliability
  - Automatic hologram updates every 5 seconds
  - PlaceholderAPI support
  - WorldGuard region integration

#### 🐛 Bug Fixes
- Fixed hologram duplication issues
- Resolved tax system auto-recreation of disabled holograms
- Eliminated spam debug messages in console
- Fixed hologram persistence after protection deletion
- Corrected default hologramEnabled state

#### 🔧 Technical
- Optimized for Minecraft 1.19.4+
- Compatible with Spigot, Paper, and Purpur
- JSON file-based storage system
- API version: 1.19
- Performance optimizations for large servers
- Clean code architecture with proper separation of concerns

#### 📝 Documentation
- Complete Spigot description with BBCode formatting
- Detailed permissions guide
- Configuration examples and explanations
- Professional README with badges
- Command reference guide

---

### Future Plans

#### Planned for v1.1.0
- [ ] MySQL/PostgreSQL support
- [ ] Protection templates
- [ ] Protection transfer system
- [ ] Auction system for protections
- [ ] Web dashboard integration

#### Planned for v1.2.0
- [ ] Region expansion system
- [ ] Protection merging
- [ ] Backup/restore functionality
- [ ] Advanced analytics
- [ ] Mobile notifications

#### Planned for v2.0.0
- [ ] Complete API for developers
- [ ] Advanced flags system
- [ ] Custom events
- [ ] Protection groups
- [ ] Integration with popular plugins

---

## Version Format

AdvancedProtection follows Semantic Versioning (SemVer):
- **MAJOR** version for incompatible API changes
- **MINOR** version for backwards-compatible functionality additions
- **PATCH** version for backwards-compatible bug fixes

---

**For support or to report issues, please visit our Spigot page or Discord server.**
