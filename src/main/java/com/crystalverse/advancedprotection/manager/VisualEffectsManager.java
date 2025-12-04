package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.model.ProtectionBlock;
import com.crystalverse.advancedprotection.AdvancedProtection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisualEffectsManager {
    private final AdvancedProtection plugin;
    private final Map<UUID, Integer> activeTasks = new HashMap<>();
    private final Map<UUID, java.util.List<org.bukkit.entity.Entity>> activeEntities = new HashMap<>();
    private final Map<UUID, UUID> activeProtections = new HashMap<>();

    public VisualEffectsManager(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    public void showBorders(Player player, ProtectionBlock protection) {
        // Toggle logic
        if (activeProtections.containsKey(player.getUniqueId())) {
            UUID activeId = activeProtections.get(player.getUniqueId());
            if (activeId.equals(protection.getId())) {
                stopVisualizing(player);
                return;
            }
        }
        startVisualizing(player, protection);
    }

    public void startVisualizing(Player player, ProtectionBlock protection) {
        // If already visualizing this specific protection, do nothing (avoid flicker)
        if (activeProtections.containsKey(player.getUniqueId())) {
            if (activeProtections.get(player.getUniqueId()).equals(protection.getId())) {
                return;
            }
            // If visualizing something else, stop it first
            stopVisualizing(player);
        }

        int radius = plugin.getConfigManager().getProtectionRadius(protection.getLevel());
        Location center = protection.getCenter();
        World world = center.getWorld();

        // Determine Material based on particle type
        org.bukkit.Material glassMaterial;
        String type = protection.getParticleType();

        if (type.equals("VILLAGER_HAPPY"))
            glassMaterial = org.bukkit.Material.LIME_STAINED_GLASS;
        else if (type.equals("FLAME"))
            glassMaterial = org.bukkit.Material.ORANGE_STAINED_GLASS;
        else if (type.equals("HEART"))
            glassMaterial = org.bukkit.Material.RED_STAINED_GLASS;
        else if (type.equals("NOTE"))
            glassMaterial = org.bukkit.Material.LIGHT_BLUE_STAINED_GLASS;
        else if (type.equals("SPELL_WITCH"))
            glassMaterial = org.bukkit.Material.PURPLE_STAINED_GLASS;
        else if (type.equals("LAVA"))
            glassMaterial = org.bukkit.Material.MAGMA_BLOCK;
        else if (type.equals("CLOUD"))
            glassMaterial = org.bukkit.Material.WHITE_STAINED_GLASS;
        else
            glassMaterial = org.bukkit.Material.GLASS;

        // Spawn Holographic Border
        spawnHolographicBorder(player, center, radius, glassMaterial);

        // Task to keep entities rotating (optional, or just keep track)
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!player.isOnline() || !player.getWorld().equals(world)
                    || player.getLocation().distanceSquared(center) > (radius + 50) * (radius + 50)) {
                stopVisualizing(player);
                return;
            }

            // Rotation logic: Spin all active entities for this player
            if (activeEntities.containsKey(player.getUniqueId())) {
                float angle = (System.currentTimeMillis() % 3600) / 10.0f; // 0 to 360 degrees over time

                for (org.bukkit.entity.Entity e : activeEntities.get(player.getUniqueId())) {
                    if (e instanceof org.bukkit.entity.BlockDisplay) {
                        org.bukkit.entity.BlockDisplay bd = (org.bukkit.entity.BlockDisplay) e;
                        org.bukkit.util.Transformation t = bd.getTransformation();

                        // Rotate around Y axis
                        t.getLeftRotation().set(new org.joml.AxisAngle4f((float) Math.toRadians(angle), 0, 1, 0));

                        bd.setTransformation(t);
                        bd.setInterpolationDuration(1); // Smooth updates
                        bd.setInterpolationDelay(0);
                    }
                }
            }

        }, 0L, 1L); // Update every tick for smooth animation

        activeTasks.put(player.getUniqueId(), taskId);
        activeProtections.put(player.getUniqueId(), protection.getId());
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(org.bukkit.ChatColor.GREEN + "Visualizing Borders: ON"));
    }

    private void spawnHolographicBorder(Player player, Location center, int radius, org.bukkit.Material material) {
        java.util.List<org.bukkit.entity.Entity> entities = new java.util.ArrayList<>();
        World world = center.getWorld();
        int y = center.getBlockY();

        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        // Altura máxima de visualización: usamos el radio como límite vertical
        int maxHeight = Math.min(radius, 30); // Cap a 30 para radios muy grandes

        // 1. Corner Pillars (Altura dinámica basada en radius, cada 2 bloques)
        int[][] corners = { { minX, minZ }, { minX, maxZ }, { maxX, minZ }, { maxX, maxZ } };
        for (int[] corner : corners) {
            for (int h = 0; h <= maxHeight; h += 2) {
                Location loc = new Location(world, corner[0] + 0.5, y + h + 0.5, corner[1] + 0.5);
                entities.add(spawnDisplay(loc, material, player));
            }
        }

        // 2. Edge Markers (Alturas ajustadas al radio, cada 4 bloques)
        // Distribuimos marcadores en diferentes alturas dentro del límite
        int midHeight = maxHeight / 2;
        int topHeight = maxHeight;

        // X-axis edges
        for (int x = minX + 2; x < maxX; x += 4) {
            // Nivel bajo
            entities.add(spawnDisplay(new Location(world, x + 0.5, y + 2.5, minZ + 0.5), material, player));
            entities.add(spawnDisplay(new Location(world, x + 0.5, y + 2.5, maxZ + 0.5), material, player));

            // Nivel medio (solo si el radio es mayor a 5)
            if (maxHeight > 5) {
                entities.add(
                        spawnDisplay(new Location(world, x + 0.5, y + midHeight + 0.5, minZ + 0.5), material, player));
                entities.add(
                        spawnDisplay(new Location(world, x + 0.5, y + midHeight + 0.5, maxZ + 0.5), material, player));
            }

            // Nivel alto (solo si el radio es mayor a 10)
            if (maxHeight > 10) {
                entities.add(
                        spawnDisplay(new Location(world, x + 0.5, y + topHeight + 0.5, minZ + 0.5), material, player));
                entities.add(
                        spawnDisplay(new Location(world, x + 0.5, y + topHeight + 0.5, maxZ + 0.5), material, player));
            }
        }

        // Z-axis edges
        for (int z = minZ + 2; z < maxZ; z += 4) {
            // Nivel bajo
            entities.add(spawnDisplay(new Location(world, minX + 0.5, y + 2.5, z + 0.5), material, player));
            entities.add(spawnDisplay(new Location(world, maxX + 0.5, y + 2.5, z + 0.5), material, player));

            // Nivel medio (solo si el radio es mayor a 5)
            if (maxHeight > 5) {
                entities.add(
                        spawnDisplay(new Location(world, minX + 0.5, y + midHeight + 0.5, z + 0.5), material, player));
                entities.add(
                        spawnDisplay(new Location(world, maxX + 0.5, y + midHeight + 0.5, z + 0.5), material, player));
            }

            // Nivel alto (solo si el radio es mayor a 10)
            if (maxHeight > 10) {
                entities.add(
                        spawnDisplay(new Location(world, minX + 0.5, y + topHeight + 0.5, z + 0.5), material, player));
                entities.add(
                        spawnDisplay(new Location(world, maxX + 0.5, y + topHeight + 0.5, z + 0.5), material, player));
            }
        }

        activeEntities.put(player.getUniqueId(), entities);
    }

    private org.bukkit.entity.Entity spawnDisplay(Location loc, org.bukkit.Material mat, Player player) {
        // Spawn BlockDisplay
        org.bukkit.entity.BlockDisplay display = (org.bukkit.entity.BlockDisplay) loc.getWorld().spawnEntity(loc,
                org.bukkit.entity.EntityType.BLOCK_DISPLAY);
        display.setBlock(mat.createBlockData());

        // Visual properties
        display.setGlowing(true); // Glow effect
        display.setGlowColorOverride(org.bukkit.Color.AQUA); // Default glow color, could be customized

        // Scale it down slightly to look like a floating crystal
        org.bukkit.util.Transformation transformation = display.getTransformation();
        transformation.getScale().set(0.6f, 0.6f, 0.6f);
        display.setTransformation(transformation);

        // Metadata to prevent interaction/gravity
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setVisibleByDefault(false); // Hide from everyone
        player.showEntity(plugin, display); // Show only to player

        return display;
    }

    public void stopVisualizing(Player player) {
        if (activeTasks.containsKey(player.getUniqueId())) {
            plugin.getServer().getScheduler().cancelTask(activeTasks.remove(player.getUniqueId()));
            activeProtections.remove(player.getUniqueId());

            // Remove entities
            if (activeEntities.containsKey(player.getUniqueId())) {
                for (org.bukkit.entity.Entity e : activeEntities.get(player.getUniqueId())) {
                    e.remove();
                }
                activeEntities.remove(player.getUniqueId());
            }

            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(org.bukkit.ChatColor.RED + "Visualizing Borders: OFF"));
        }
    }

    private void cancelVisualization(Player player) {
        stopVisualizing(player);
    }

    public void shutdown() {
        activeTasks.values().forEach(id -> plugin.getServer().getScheduler().cancelTask(id));
        activeTasks.clear();
        activeProtections.clear();

        // Remove all entities on shutdown
        activeEntities.values().forEach(list -> list.forEach(org.bukkit.entity.Entity::remove));
        activeEntities.clear();
    }

    public boolean isVisualizing(Player player) {
        return activeTasks.containsKey(player.getUniqueId());
    }
}
