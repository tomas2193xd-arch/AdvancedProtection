package com.crystalverse.advancedprotection.config;

import com.crystalverse.advancedprotection.AdvancedProtection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final AdvancedProtection plugin;
    private FileConfiguration config;
    private String cachedLanguage = null;

    public ConfigManager(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        plugin.saveDefaultConfig();

        // Inicializar caché
        this.cachedLanguage = config.getString("language", "en");
        plugin.getLogger().info("Configuration reloaded from disk. Language: " + cachedLanguage);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private String getLanguage() {
        if (cachedLanguage != null)
            return cachedLanguage;
        return config.getString("language", "en");
    }

    public void setLanguage(String lang) {
        this.cachedLanguage = lang;
        config.set("language", lang);
        plugin.saveConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        plugin.getLogger()
                .info("DEBUG: Idioma cambiado a " + lang + " (Persistido: " + config.getString("language") + ")");
    }

    public String getMessage(String path) {
        String lang = getLanguage();
        String msg = config.getString("messages." + lang + "." + path);
        if (msg == null) {
            // Try inside 'gui' section
            msg = config.getString("messages." + lang + ".gui." + path);
        }
        if (msg == null) {
            // Fallback to English root
            msg = config.getString("messages.en." + path);
        }
        if (msg == null) {
            // Fallback to English gui
            msg = config.getString("messages.en.gui." + path);
        }
        if (msg == null) {
            return "&cMessage not found: " + path;
        }
        return msg.replace("&", "§").replace("%prefix%", getPrefix());
    }

    public String getPrefix() {
        String lang = getLanguage();
        return config.getString("messages." + lang + ".prefix", "&8[&bAdvancedProtection&8] &7").replace("&", "§");
    }

    public List<String> getMessageList(String path) {
        String lang = getLanguage();
        List<String> list = config.getStringList("messages." + lang + "." + path);

        if (list == null || list.isEmpty()) {
            // Try inside 'gui' section
            list = config.getStringList("messages." + lang + ".gui." + path);
        }

        if (list == null || list.isEmpty()) {
            // Fallback to English root
            list = config.getStringList("messages.en." + path);
        }

        if (list == null || list.isEmpty()) {
            // Fallback to English gui
            list = config.getStringList("messages.en.gui." + path);
        }

        if (list == null)
            return Collections.emptyList();

        List<String> colored = new ArrayList<>();
        for (String s : list) {
            colored.add(s.replace("&", "§"));
        }
        return colored;
    }

    public int getProtectionRadius(int level) {
        int radius = config.getInt("protection.levels." + level + ".radius", -1);
        if (radius <= 0) {
            // Hardcoded fallbacks
            switch (level) {
                case 1:
                    return 10;
                case 2:
                    return 20;
                case 3:
                    return 30;
                case 4:
                    return 50;
                case 5:
                    return 75;
                case 6:
                    return 100;
                default:
                    return 10;
            }
        }
        return radius;
    }

    public double getProtectionCost(int level) {
        double cost = config.getDouble("protection.levels." + level + ".cost", -1.0);

        // Force defaults if config fails or is 0
        if (cost <= 0) {
            // plugin.getLogger().warning("Invalid cost for level " + level + ". Using
            // hardcoded default.");
            switch (level) {
                case 1:
                    return 1000.0;
                case 2:
                    return 2500.0;
                case 3:
                    return 5000.0;
                case 4:
                    return 10000.0;
                case 5:
                    return 25000.0;
                case 6:
                    return 50000.0;
                default:
                    return 1000.0;
            }
        }
        return cost;
    }

    public org.bukkit.Material getProtectionMaterial(int level) {
        String matName = config.getString("protection.levels." + level + ".material");

        // Si no está en config o queremos forzar los defaults correctos (Ores)
        if (matName == null || matName.contains("BLOCK")) {
            switch (level) {
                case 1:
                    return org.bukkit.Material.COAL_ORE;
                case 2:
                    return org.bukkit.Material.IRON_ORE;
                case 3:
                    return org.bukkit.Material.GOLD_ORE;
                case 4:
                    return org.bukkit.Material.DIAMOND_ORE;
                case 5:
                    return org.bukkit.Material.EMERALD_ORE;
                case 6:
                    return org.bukkit.Material.OBSIDIAN;
                default:
                    return org.bukkit.Material.STONE;
            }
        }
        return org.bukkit.Material.matchMaterial(matName);
    }

    public String getProtectionDisplayName(int level) {
        return config.getString("protection.levels." + level + ".name", "Protección Nivel " + level).replace("&", "§");
    }

    // Alias para HologramManager
    public String getProtectionName(int level) {
        return getProtectionDisplayName(level);
    }

    public int getMaxProtections(org.bukkit.entity.Player player) {
        if (player.hasPermission("advancedprotection.admin")
                || player.hasPermission("advancedprotection.limit.unlimited")) {
            return -1; // Infinito
        }

        // Revisar permisos desde 100 hacia abajo
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("advancedprotection.limit." + i)) {
                return i;
            }
        }

        return config.getInt("protection.default_limit", 3);
    }
}
