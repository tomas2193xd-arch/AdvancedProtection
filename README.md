# ⚔️ AdvancedProtection

[![Spigot](https://img.shields.io/badge/Spigot-1.19.4+-orange.svg)](https://www.spigotmc.org/)
[![Version](https://img.shields.io/badge/version-1.0-blue.svg)](https://github.com/CrystalVerse/AdvancedProtection)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

> **The Ultimate Land Protection Plugin for Minecraft 1.19+**

![Banner](https://imgur.com/a/AVosdM3)

📹 **[Watch Demo Video](https://youtu.be/nc31xFtYS-c)**

---

## ✨ Features

### 🛡️ Advanced Protection System
- 6 Upgradeable Protection Levels (Coal → Obsidian)
- Dynamic radius expansion (10m to 100m)
- Per-level customizable costs and materials
- WorldGuard integration
- Collision detection

### 👥 Granular Permission System
- Individual member permissions
- Predefined roles (Visitor, Member, Builder, Moderator, Co-Owner)
- Custom permission combinations
- Full permission hierarchy

### 💰 Tax & Rent System
- Automatic tax collection
- Percentage or fixed-rate modes
- Custom tax rates per level
- Grace period system
- Automatic notifications

### 🎨 Visual Effects
- 7 customizable particle effects
- Holographic border visualization
- 3D rotating crystal displays
- Real-time TextDisplay holograms
- DecentHolograms support

### 🖥️ Professional GUI
- Intuitive inventory menus
- Shop system
- Member management
- Flag configuration
- Visual effect selector
- Admin panel

---

## 📦 Installation

1. Download `AdvancedProtection-1.0.jar`
2. Place in `/plugins` folder
3. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) (Required)
4. Restart server
5. Configure `config.yml`
6. Reload with `/ap reload`

### Optional Dependencies
- **WorldGuard** - Enhanced compatibility
- **PlaceholderAPI** - Placeholder support
- **DecentHolograms** - Alternative hologram engine

---

## 📋 Commands

### Player Commands
```
/ap menu           - Open main menu
/ap visualize      - Show protection borders
/ap trust <player> - Add member
/ap untrust <player> - Remove member
/ap trustlist      - View members
/ap rename <name>  - Rename protection
/ap clearname      - Remove name
/ap paytax         - Pay tax
/ap help           - Command list
```

### Admin Commands
```
/ap reload    - Reload config
/ap logs      - View logs
/ap manager   - Admin panel
```

---

## 🔐 Permissions

| Permission                         | Description        | Default |
| ---------------------------------- | ------------------ | ------- |
| `advancedprotection.use`           | Basic usage        | `true`  |
| `advancedprotection.admin`         | Full admin access  | `op`    |
| `advancedprotection.admin.logs`    | View logs          | `op`    |
| `advancedprotection.admin.manager` | Admin panel        | `op`    |
| `advancedprotection.bypass`        | Bypass protections | `op`    |

**For detailed permission guide, see [PERMISSIONS.txt](PERMISSIONS.txt)**

---

## ⚙️ Configuration

### Protection Levels
```yaml
protections:
  1:
    name: "&7Coal Protection"
    material: COAL_BLOCK
    radius: 10
    cost: 100.0
  # ... up to level 6
```

### Tax System
```yaml
tax_system:
  enabled: false
  interval_hours: 24
  rate_mode: 'percentage'
  tax_rate: 5.0
  grace_period_hours: 72
```

### Permissions
```yaml
permissions:
  enabled: true
  default_role: "member"
  roles:
    member:
      permissions:
        - BUILD
        - INTERACT
        - CONTAINER_ACCESS
```

**Full config example: [config.yml](src/main/resources/config.yml)**

---

## 🎯 How to Use

### For Players
1. Open `/ap menu`
2. Purchase a protection level
3. Place the protection block
4. Right-click to configure
5. Add members and customize

### For Admins
1. Configure costs and radii
2. Enable/disable systems
3. Set up roles
4. Manage via `/ap manager`
5. Monitor with `/ap logs`

---

## 🔧 Technical Details

- **Compatibility:** Minecraft 1.19.4+
- **Server Software:** Spigot, Paper, Purpur
- **Storage:** JSON file-based
- **API Version:** 1.19
- **Performance:** Optimized for large servers

---

## 📞 Support

- **Spigot:** [Plugin Page](#)
- **Discord:** Coming Soon
- **Issues:** [GitHub Issues](#)
- **Wiki:** Coming Soon

---

## 📸 Screenshots

*Coming Soon*

---

## 🌟 Star History

If you like this plugin, please give it a star!

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 💖 Credits

**Developed by:** CrystalVerse Team  
**Version:** 1.0  
**Last Updated:** December 2024

---

<div align="center">

**⭐ If you enjoy this plugin, please leave a review! ⭐**

Made with ❤️ by CrystalVerse

</div>
