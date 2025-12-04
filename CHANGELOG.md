# Changelog

All notable changes to AdvancedProtection will be documented in this file.

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
