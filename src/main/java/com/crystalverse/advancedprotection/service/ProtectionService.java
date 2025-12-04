package com.crystalverse.advancedprotection.service;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.api.IStorageProvider;
import com.crystalverse.advancedprotection.config.ConfigManager;
import com.crystalverse.advancedprotection.manager.RegionManager;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProtectionService {

    private final AdvancedProtection plugin;
    private final RegionManager regionManager;
    private final IStorageProvider storageProvider;
    private final ConfigManager configManager;

    public ProtectionService(AdvancedProtection plugin, RegionManager regionManager, IStorageProvider storageProvider,
            ConfigManager configManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        this.storageProvider = storageProvider;
        this.configManager = configManager;
    }

    public boolean createProtection(Player player, Location location, int level) {
        // Verificar si ya hay una protección cerca (colisión)
        if (regionManager.getProtectionAt(location) != null) {
            player.sendMessage(configManager.getMessage("protection_collision"));
            return false;
        }

        // Crear nueva protección
        ProtectionBlock protection = new ProtectionBlock(UUID.randomUUID(), player.getUniqueId(), location, level);

        // Guardar
        regionManager.addProtection(protection);
        storageProvider.saveProtection(protection);

        // Crear holograma solo si está habilitado
        if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()
                && protection.isHologramEnabled()) {
            plugin.getHologramManager().createHologram(protection);
        }

        player.sendMessage(configManager.getMessage("protection_created"));
        return true;
    }

    public void deleteProtection(UUID id) {
        // Eliminar holograma primero
        if (plugin.getHologramManager() != null) {
            plugin.getHologramManager().removeHologram(id);
        }

        regionManager.removeProtection(id);
        storageProvider.deleteProtection(id);
    }

    public void loadProtections() {
        int count = 0;
        for (ProtectionBlock protection : storageProvider.loadAllProtections()) {
            regionManager.addProtection(protection);

            // Crear holograma para cada protección cargada (solo si está habilitado)
            if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()
                    && protection.isHologramEnabled()) {
                plugin.getHologramManager().createHologram(protection);
            }

            count++;
        }
        plugin.getLogger().info("Loaded " + count + " protections.");
    }

    public boolean upgradeProtection(Player player, ProtectionBlock protection) {
        int currentLevel = protection.getLevel();
        int nextLevel = currentLevel + 1;

        if (plugin.getConfigManager().getProtectionRadius(nextLevel) == 10) {
            if (currentLevel >= 6) {
                player.sendMessage(ChatColor.RED + "Max level reached.");
                return false;
            }
        }

        if (currentLevel >= 6) {
            return false;
        }

        double currentCost = plugin.getConfigManager().getProtectionCost(currentLevel);
        double nextCost = plugin.getConfigManager().getProtectionCost(nextLevel);
        double upgradeCost = nextCost - currentCost;

        if (upgradeCost < 0)
            upgradeCost = 0;

        if (!plugin.getEconomyHook().withdraw(player, upgradeCost)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient_funds")
                    .replace("%cost%", String.valueOf(upgradeCost)));
            return false;
        }

        // Check for overlap with new radius
        int newRadius = plugin.getConfigManager().getProtectionRadius(nextLevel);

        protection.setLevel(nextLevel);
        saveProtection(protection);

        // Actualizar holograma
        if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
            plugin.getHologramManager().updateHologram(protection);
        }

        player.sendMessage(plugin.getConfigManager().getMessage("upgrade_success")
                .replace("%level%", String.valueOf(nextLevel)));
        return true;
    }

    public void saveProtection(ProtectionBlock protection) {
        storageProvider.saveProtection(protection);
    }
}
