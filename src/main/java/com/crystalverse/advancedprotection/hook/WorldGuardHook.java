package com.crystalverse.advancedprotection.hook;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Hook para integración con WorldGuard
 * Permite verificar regiones de WorldGuard antes de aplicar lógica de
 * AdvancedProtection
 */
public class WorldGuardHook {

    private final AdvancedProtection plugin;
    private boolean worldGuardEnabled = false;
    private WorldGuardPlugin worldGuardPlugin = null;

    public WorldGuardHook(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    /**
     * Intenta conectar con WorldGuard
     * 
     * @return true si WorldGuard está disponible, false si no
     */
    public boolean setupWorldGuard() {
        // Verificar si WorldGuard está en el servidor
        Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
            worldGuardEnabled = false;
            return false;
        }

        worldGuardPlugin = (WorldGuardPlugin) wgPlugin;
        worldGuardEnabled = true;

        plugin.getLogger().info("WorldGuard detectado. Integración activada.");
        return true;
    }

    /**
     * Verifica si WorldGuard está habilitado y funcionando
     */
    public boolean isEnabled() {
        return worldGuardEnabled && worldGuardPlugin != null;
    }

    /**
     * Verifica si una ubicación está dentro de una región de WorldGuard
     * 
     * @param location Ubicación a verificar
     * @return true si está en región WG, false si no
     */
    public boolean isInRegion(Location location) {
        if (!isEnabled()) {
            return false;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            // Si hay al menos una región, retorna true
            return set.size() > 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Error verificando región de WorldGuard: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un jugador puede construir en una ubicación según WorldGuard
     * 
     * @param player   Jugador a verificar
     * @param location Ubicación donde intenta construir
     * @return true si WG permite construir, false si WG bloquea
     */
    public boolean canBuild(Player player, Location location) {
        if (!isEnabled()) {
            return true; // Si WG no está, permitimos (AP se encargará)
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            // Verificar si el jugador puede construir (flag BUILD)
            return set.testState(worldGuardPlugin.wrapPlayer(player), Flags.BUILD);
        } catch (Exception e) {
            plugin.getLogger().warning("Error verificando permisos de construcción en WorldGuard: " + e.getMessage());
            return true; // En caso de error, permitimos
        }
    }

    /**
     * Verifica si un jugador puede interactuar en una ubicación según WorldGuard
     * 
     * @param player   Jugador a verificar
     * @param location Ubicación donde intenta interactuar
     * @return true si WG permite interactuar, false si WG bloquea
     */
    public boolean canInteract(Player player, Location location) {
        if (!isEnabled()) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            // Verificar si el jugador puede interactuar (flag INTERACT)
            return set.testState(worldGuardPlugin.wrapPlayer(player), Flags.INTERACT);
        } catch (Exception e) {
            plugin.getLogger().warning("Error verificando permisos de interacción en WorldGuard: " + e.getMessage());
            return true;
        }
    }

    /**
     * Verifica si se pueden crear protecciones de AP en regiones de WG
     * según la configuración del plugin
     * 
     * @param location Ubicación donde se intenta colocar protección
     * @return true si se permite, false si se bloquea
     */
    public boolean canCreateProtectionHere(Location location) {
        if (!isEnabled()) {
            return true;
        }

        // Verificar configuración
        boolean allowInWGRegions = plugin.getConfigManager().getConfig()
                .getBoolean("worldguard.allow_protections_in_regions", false);

        // Si está permitido crear en regiones WG, OK
        if (allowInWGRegions) {
            return true;
        }

        // Si no está permitido, verificar si está en región
        return !isInRegion(location);
    }

    /**
     * Obtiene el nombre de la región en la ubicación (para mensajes)
     * 
     * @param location Ubicación a verificar
     * @return Nombre de la región o null si no hay
     */
    public String getRegionNameAt(Location location) {
        if (!isEnabled()) {
            return null;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            if (set.size() > 0) {
                return set.getRegions().iterator().next().getId();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error obteniendo nombre de región: " + e.getMessage());
        }

        return null;
    }
}
