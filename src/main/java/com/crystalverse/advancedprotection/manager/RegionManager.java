package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionManager {

    private final AdvancedProtection plugin;
    private final Map<UUID, ProtectionBlock> protections = new HashMap<>();

    public RegionManager(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    public void addProtection(ProtectionBlock protection) {
        protections.put(protection.getId(), protection);
    }

    public void removeProtection(UUID id) {
        protections.remove(id);
    }

    public ProtectionBlock getProtection(UUID id) {
        return protections.get(id);
    }

    public ProtectionBlock getProtection(Location blockLoc) {
        for (ProtectionBlock protection : protections.values()) {
            if (protection.getCenter().equals(blockLoc)) {
                return protection;
            }
        }
        return null;
    }

    public ProtectionBlock getProtectionAt(Location loc) {
        for (ProtectionBlock protection : protections.values()) {
            if (protection.getCenter().getWorld().equals(loc.getWorld())) {
                int radius = plugin.getConfigManager().getProtectionRadius(protection.getLevel());
                Location center = protection.getCenter();

                // Usar lógica Cuboide (Cuadrado en X/Z) en lugar de Esférica
                // Esto evita que las esquinas queden desprotegidas y mejora la detección
                boolean inX = Math.abs(center.getBlockX() - loc.getBlockX()) <= radius;
                boolean inZ = Math.abs(center.getBlockZ() - loc.getBlockZ()) <= radius;
                boolean inY = Math.abs(center.getBlockY() - loc.getBlockY()) <= radius; // También verificamos altura
                                                                                        // por seguridad

                if (inX && inZ && inY) {
                    return protection;
                }
            }
        }
        return null;
    }

    public java.util.List<ProtectionBlock> getPlayerProtections(UUID playerId) {
        java.util.List<ProtectionBlock> playerProtections = new java.util.ArrayList<>();
        for (ProtectionBlock protection : protections.values()) {
            if (protection.getOwner().equals(playerId)) {
                playerProtections.add(protection);
            }
        }
        return playerProtections;
    }

    public boolean isRegionOverlapping(Location center, int radius) {
        for (ProtectionBlock protection : protections.values()) {
            if (!protection.getCenter().getWorld().equals(center.getWorld()))
                continue;

            int otherRadius = plugin.getConfigManager().getProtectionRadius(protection.getLevel());
            Location otherCenter = protection.getCenter();

            // Verificar superposición de Cuboides (AABB)
            boolean overlapX = Math.abs(center.getBlockX() - otherCenter.getBlockX()) <= (radius + otherRadius);
            boolean overlapZ = Math.abs(center.getBlockZ() - otherCenter.getBlockZ()) <= (radius + otherRadius);
            boolean overlapY = Math.abs(center.getBlockY() - otherCenter.getBlockY()) <= (radius + otherRadius);

            if (overlapX && overlapZ && overlapY) {
                return true;
            }
        }
        return false;
    }

    public java.util.List<ProtectionBlock> getAllProtections() {
        return new java.util.ArrayList<>(protections.values());
    }
}
