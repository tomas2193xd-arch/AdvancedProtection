package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
// import eu.decentsoftware.holograms.api.DHAPI;
// import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;

import java.util.*;

/**
 * Manager de Hologramas con soporte para:
 * - DecentHolograms (si está disponible) - Persistente y profesional
 * - TextDisplay nativo (fallback) - Funcional sin dependencias
 * 
 * Los hologramas muestran información de la protección en tiempo real
 */
public class HologramManager {

    private final AdvancedProtection plugin;
    private boolean useDecentHolograms = false;

    // Cache: ProtectionID -> Hologram ID/Entity
    private final Map<UUID, String> decentHologramIds = new HashMap<>();
    private final Map<UUID, TextDisplay> nativeHolograms = new HashMap<>();

    // Configuración
    private boolean enabled = true;
    private double heightOffset = 2.5;
    private int updateInterval = 5; // segundos
    private List<String> format;

    public HologramManager(AdvancedProtection plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    /**
     * Inicializa el sistema de hologramas
     */
    public boolean initialize() {
        // Intentar usar DecentHolograms
        if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
            try {
                // Verificar que la API está disponible
                Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                useDecentHolograms = true;
                plugin.getLogger().info("DecentHolograms detectado. Usando hologramas persistentes.");
                return true;
            } catch (ClassNotFoundException e) {
                plugin.getLogger()
                        .warning("DecentHolograms encontrado pero API no disponible. Usando TextDisplay nativo.");
            }
        } else {
            plugin.getLogger().info("DecentHolograms no encontrado. Usando TextDisplay nativo (1.19.4+).");
        }

        useDecentHolograms = false;

        // Clean up any orphaned holograms from previous sessions
        cleanupOrphanedHolograms();

        // Iniciar tarea de actualización periódica
        startUpdateTask();

        return true;
    }

    /**
     * Removes orphaned holograms from previous sessions
     */
    private void cleanupOrphanedHolograms() {
        int removed = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof TextDisplay) {
                    TextDisplay display = (TextDisplay) entity;
                    // Remove invulnerable TextDisplays (our holograms)
                    if (display.isInvulnerable()) {
                        display.remove();
                        removed++;
                    }
                }
            }
        }
        if (removed > 0) {
            plugin.getLogger().info("Cleaned up " + removed + " orphaned holograms from previous sessions.");
        }
    }

    /**
     * Carga configuración de hologramas
     */
    private void loadConfiguration() {
        enabled = plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true);
        heightOffset = plugin.getConfigManager().getConfig().getDouble("holograms.height_offset", 2.5);
        updateInterval = plugin.getConfigManager().getConfig().getInt("holograms.update_interval", 5);

        format = plugin.getConfigManager().getConfig().getStringList("holograms.format");
        if (format == null || format.isEmpty()) {
            // Formato por defecto
            format = Arrays.asList(
                    "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    "&b⚔ &lProtección &b%level_name%",
                    "&7Dueño: &f%owner%",
                    "&7Miembros: &a%members%",
                    "&7Radio: &e%radius%m",
                    "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        }
    }

    /**
     * Crea un holograma para una protección
     */
    public void createHologram(ProtectionBlock protection) {
        if (!enabled)
            return;

        Location location = getHologramLocation(protection);
        List<String> lines = formatLines(protection);

        // Always use native TextDisplay (1.19.4+)
        createNativeHologram(protection, location, lines);
    }

    /**
     * Actualiza un holograma existente
     */
    public void updateHologram(ProtectionBlock protection) {
        if (!enabled)
            return;

        // Always use native TextDisplay
        updateNativeHologram(protection);
    }

    /**
     * Elimina un holograma
     */
    public void removeHologram(ProtectionBlock protection) {
        removeHologram(protection.getId());
    }

    /**
     * Elimina un holograma por UUID
     */
    public void removeHologram(UUID protectionId) {
        /*
         * DecentHolograms code commented - using native TextDisplay
         * if (useDecentHolograms) {
         * String holoId = decentHologramIds.remove(protectionId);
         * if (holoId != null) {
         * Hologram hologram = DHAPI.getHologram(holoId);
         * if (hologram != null) {
         * hologram.delete();
         * }
         * }
         * } else {
         */
        TextDisplay display = nativeHolograms.remove(protectionId);
        if (display != null && !display.isDead()) {
            display.remove();
        }
        // }
    }

    /**
     * Elimina todos los hologramas
     */
    public void removeAllHolograms() {
        /*
         * DecentHolograms code commented - using native TextDisplay
         * if (useDecentHolograms) {
         * for (String holoId : decentHologramIds.values()) {
         * Hologram hologram = DHAPI.getHologram(holoId);
         * if (hologram != null) {
         * hologram.delete();
         * }
         * }
         * decentHologramIds.clear();
         * } else {
         */

        // Remove holograms from memory
        for (TextDisplay display : nativeHolograms.values()) {
            if (!display.isDead()) {
                display.remove();
            }
        }
        nativeHolograms.clear();

        // CRITICAL: Also remove ALL orphaned TextDisplay entities from all worlds
        // This prevents ghost holograms when plugin folder is deleted
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof TextDisplay) {
                    TextDisplay display = (TextDisplay) entity;
                    // Check if it's our hologram by checking if it has our custom name pattern
                    // or if it's invulnerable (our holograms are invulnerable)
                    if (display.isInvulnerable()) {
                        display.remove();
                    }
                }
            }
        }

        // }
    }

    // ========== DECENT HOLOGRAMS IMPLEMENTATION ==========
    // Commented out - DecentHolograms not available in Maven
    // Plugin uses native TextDisplay (works great on 1.19.4+)

    /*
     * private void createDecentHologram(ProtectionBlock protection, Location
     * location, List<String> lines) {
     * String holoId = "ap_" + protection.getId().toString();
     * 
     * try {
     * // Crear holograma
     * Hologram hologram = DHAPI.createHologram(holoId, location);
     * 
     * // Agregar líneas
     * for (String line : lines) {
     * DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&',
     * line));
     * }
     * 
     * // Configurar
     * hologram.setDefaultVisibleState(true);
     * hologram.setAlwaysFacePlayer(true);
     * 
     * decentHolograms.put(protection.getId(), holoId);
     * } catch (Exception e) {
     * plugin.getLogger().warning("Error creando holograma DecentHolograms: " +
     * e.getMessage());
     * }
     * }
     * 
     * private void updateDecentHologram(ProtectionBlock protection) {
     * String holoId = decentHologramIds.get(protection.getId());
     * if (holoId == null) {
     * // No existe, crear nuevo
     * createHologram(protection);
     * return;
     * }
     * 
     * try {
     * Hologram hologram = DHAPI.getHologram(holoId);
     * if (hologram == null) {
     * // No existe, crear nuevo
     * createHologram(protection);
     * return;
     * }
     * 
     * // Actualizar ubicación
     * Location newLoc = getHologramLocation(protection);
     * DHAPI.moveHologram(hologram, newLoc);
     * 
     * // Actualizar líneas
     * List<String> newLines = formatLines(protection);
     * hologram.getPage(0).getLines().clear();
     * for (String line : newLines) {
     * DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&',
     * line));
     * }
     * 
     * hologram.realignLines();
     * hologram.updateAll();
     * } catch (Exception e) {
     * plugin.getLogger().warning("Error actualizando holograma DecentHolograms: " +
     * e.getMessage());
     * }
     * }
     */

    // ========== NATIVE TEXT DISPLAY IMPLEMENTATION ==========

    private void createNativeHologram(ProtectionBlock protection, Location location, List<String> lines) {
        try {
            // Crear TextDisplay
            TextDisplay display = (TextDisplay) location.getWorld()
                    .spawnEntity(location, org.bukkit.entity.EntityType.TEXT_DISPLAY);

            // Configurar texto
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                text.append(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
                if (i < lines.size() - 1) {
                    text.append("\n");
                }
            }
            display.setText(text.toString());

            // Configurar visualización
            display.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(true);
            display.setBackgroundColor(org.bukkit.Color.fromARGB(120, 0, 0, 0));

            // Metadata
            display.setInvulnerable(true);
            display.setGravity(false);
            display.setPersistent(true);

            nativeHolograms.put(protection.getId(), display);
        } catch (Exception e) {
            plugin.getLogger().warning("Error creando holograma nativo: " + e.getMessage());
        }
    }

    private void updateNativeHologram(ProtectionBlock protection) {
        TextDisplay display = nativeHolograms.get(protection.getId());

        if (display == null || display.isDead()) {
            // No existe o está muerto, crear nuevo
            createHologram(protection);
            return;
        }

        try {
            // Actualizar ubicación
            Location newLoc = getHologramLocation(protection);
            display.teleport(newLoc);

            // Actualizar texto
            List<String> newLines = formatLines(protection);
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < newLines.size(); i++) {
                text.append(ChatColor.translateAlternateColorCodes('&', newLines.get(i)));
                if (i < newLines.size() - 1) {
                    text.append("\n");
                }
            }
            display.setText(text.toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Error actualizando holograma nativo: " + e.getMessage());
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Obtiene la ubicación del holograma para una protección
     */
    private Location getHologramLocation(ProtectionBlock protection) {
        return protection.getCenter().clone().add(0.5, heightOffset, 0.5);
    }

    /**
     * Formatea las líneas del holograma con placeholders
     */
    private List<String> formatLines(ProtectionBlock protection) {
        List<String> formatted = new ArrayList<>();

        String ownerName = Bukkit.getOfflinePlayer(protection.getOwner()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        String customName = protection.hasCustomName() ? protection.getCustomName() : "";
        int memberCount = protection.getMembers().size();
        int radius = plugin.getConfigManager().getProtectionRadius(protection.getLevel());
        String levelName = plugin.getConfigManager().getProtectionName(protection.getLevel());

        for (String line : format) {
            // Reemplazar placeholders
            String formattedLine = line
                    .replace("%owner%", ownerName)
                    .replace("%members%", String.valueOf(memberCount))
                    .replace("%radius%", String.valueOf(radius))
                    .replace("%level%", String.valueOf(protection.getLevel()))
                    .replace("%level_name%", levelName)
                    .replace("%custom_name%", customName);

            // Si tiene nombre custom y la línea lo muestra, usar ese
            if (plugin.getConfigManager().getConfig().getBoolean("holograms.show_custom_name", true)
                    && protection.hasCustomName() && line.contains("Protección")) {
                formattedLine = formattedLine.replace("Protección", customName);
            }

            formatted.add(formattedLine);
        }

        return formatted;
    }

    /**
     * Inicia tarea de actualización periódica
     */
    private void startUpdateTask() {
        if (!enabled || updateInterval <= 0)
            return;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Actualizar solo hologramas habilitados
            for (ProtectionBlock protection : plugin.getRegionManager().getAllProtections()) {
                if (protection.isHologramEnabled()) {
                    updateHologram(protection);
                }
            }
        }, 20L * updateInterval, 20L * updateInterval);
    }

    /**
     * Recarga todos los hologramas (útil para /ap reload)
     */
    public void reloadAllHolograms() {
        removeAllHolograms();
        loadConfiguration();

        // Recrear todos los hologramas
        for (ProtectionBlock protection : plugin.getRegionManager().getAllProtections()) {
            createHologram(protection);
        }
    }

    /**
     * Verifica si los hologramas están habilitados
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Verifica si está usando DecentHolograms
     */
    public boolean isUsingDecentHolograms() {
        return useDecentHolograms;
    }

    /**
     * Shutdown cleanup
     */
    public void shutdown() {
        removeAllHolograms();
    }
}
