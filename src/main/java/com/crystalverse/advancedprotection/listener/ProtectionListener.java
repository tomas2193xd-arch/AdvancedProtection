package com.crystalverse.advancedprotection.listener;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.manager.RegionManager;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
import com.crystalverse.advancedprotection.model.ProtectionFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ProtectionListener implements Listener {

    private final AdvancedProtection plugin;
    private final RegionManager regionManager;

    // Cooldown para mensajes de entrada/salida (jugadorUUID -> último timestamp)
    private final java.util.Map<java.util.UUID, Long> messageCooldowns = new java.util.HashMap<>();
    private static final long COOLDOWN_MS = 30000; // 30 segundos

    public ProtectionListener(AdvancedProtection plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // Verificar si es un bloque de protección válido
        int level = getProtectionLevel(block.getType());
        if (level != -1) {
            // Verificar límites de protección
            int maxProtections = plugin.getConfigManager().getMaxProtections(player);
            if (maxProtections != -1) {
                int currentProtections = plugin.getRegionManager().getPlayerProtections(player.getUniqueId()).size();
                if (currentProtections >= maxProtections) {
                    player.sendMessage(plugin.getConfigManager().getMessage("limit_reached")
                            .replace("%current%", String.valueOf(currentProtections))
                            .replace("%max%", String.valueOf(maxProtections)));
                    event.setCancelled(true);
                    return;
                }
            }

            // Verificar si WorldGuard permite crear protecciones aquí
            if (plugin.getWorldGuardHook() != null && plugin.getWorldGuardHook().isEnabled()) {
                if (!plugin.getWorldGuardHook().canCreateProtectionHere(block.getLocation())) {
                    String regionName = plugin.getWorldGuardHook().getRegionNameAt(block.getLocation());
                    String msg = plugin.getConfigManager().getMessage("worldguard_region_conflict")
                            .replace("%region%", regionName != null ? regionName : "unknown");
                    player.sendMessage(msg);
                    event.setCancelled(true);
                    return;
                }
            }

            // Verificar si ya hay una protección cerca que solape
            int radius = plugin.getConfigManager().getProtectionRadius(level);
            if (plugin.getRegionManager().isRegionOverlapping(block.getLocation(), radius)) {
                player.sendMessage(plugin.getConfigManager().getMessage("protection_too_close"));
                event.setCancelled(true);
                return;
            }

            // Crear la protección
            if (plugin.getProtectionService().createProtection(player, block.getLocation(), level)) {
                // player.sendMessage(plugin.getConfigManager().getMessage("protection_placed"));
                String title = ChatColor.GREEN + "Protection Created!";
                String subtitle = ChatColor.GRAY + "Right-click to manage";
                player.sendTitle(title, subtitle, 10, 70, 20);

                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);

                // Efecto visual
                plugin.getVisualEffectsManager().showBorders(player,
                        plugin.getRegionManager().getProtection(block.getLocation()));

                // Entregar manual
                giveManual(player);
            } else {
                event.setCancelled(true);
            }
            return;
        }

        // Verificar WorldGuard primero (si está configurado como prioridad)
        if (shouldCheckWorldGuardFirst() && plugin.getWorldGuardHook() != null
                && plugin.getWorldGuardHook().isEnabled()) {
            if (!plugin.getWorldGuardHook().canBuild(player, block.getLocation())) {
                // WorldGuard bloquea, cancelar y salir (WG mostrará su mensaje)
                event.setCancelled(true);
                return;
            }
        }

        if (!canBuild(player, block.getLocation(), ProtectionFlag.BLOCK_PLACE)) {
            event.setCancelled(true);
            sendDenyMessage(player);

            // Registrar intento bloqueado
            ProtectionBlock protection = regionManager.getProtectionAt(block.getLocation());
            if (protection != null) {
                plugin.getLogManager().addLog(new com.crystalverse.advancedprotection.model.ProtectionLog(
                        protection.getId(),
                        com.crystalverse.advancedprotection.model.ProtectionLog.LogType.BLOCK_PLACE_DENIED,
                        player.getUniqueId(),
                        player.getName(),
                        "Bloque: " + block.getType().name()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // 1. Verificar si es el bloque central de una protección
        ProtectionBlock protectionAtBlock = plugin.getRegionManager().getProtectionAt(block.getLocation());

        if (protectionAtBlock != null && protectionAtBlock.getCenter().equals(block.getLocation())) {
            // Es el bloque central - SOLO EL DUEÑO puede romperlo
            if (!protectionAtBlock.getOwner().equals(player.getUniqueId())) {
                // No es el dueño
                if (player.hasPermission("advancedprotection.bypass")) {
                    // Admin puede romperlo sin restricciones
                } else {
                    // No es dueño ni admin - DENEGAR
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getMessage("protection_cant_break"));
                    return;
                }
            }

            // Es el dueño o admin, verificar si está shifteando
            if (!player.isSneaking()) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getMessage("protection_shift_break"));
                return;
            }

            // Eliminar protección
            plugin.getLogManager().addLog(new com.crystalverse.advancedprotection.model.ProtectionLog(
                    protectionAtBlock.getId(),
                    com.crystalverse.advancedprotection.model.ProtectionLog.LogType.PROTECTION_DELETED,
                    player.getUniqueId(),
                    player.getName(),
                    "Protección eliminada"));

            plugin.getProtectionService().deleteProtection(protectionAtBlock.getId());
            plugin.getVisualEffectsManager().stopVisualizing(player);
            player.sendMessage(plugin.getConfigManager().getMessage("protection_removed"));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            return;
        }

        // 2. Verificar WorldGuard primero (si está configurado)
        if (shouldCheckWorldGuardFirst() && plugin.getWorldGuardHook() != null
                && plugin.getWorldGuardHook().isEnabled()) {
            if (!plugin.getWorldGuardHook().canBuild(player, block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        // 3. Verificar si el bloque está DENTRO de una protección de AP
        if (!canBuild(player, block.getLocation(), ProtectionFlag.BLOCK_BREAK)) {
            event.setCancelled(true);
            sendDenyMessage(player);

            // Registrar intento bloqueado
            ProtectionBlock protection = regionManager.getProtectionAt(block.getLocation());
            if (protection != null) {
                plugin.getLogManager().addLog(new com.crystalverse.advancedprotection.model.ProtectionLog(
                        protection.getId(),
                        com.crystalverse.advancedprotection.model.ProtectionLog.LogType.BLOCK_BREAK_DENIED,
                        player.getUniqueId(),
                        player.getName(),
                        "Bloque: " + block.getType().name()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;
        Player player = event.getPlayer();

        // 1. Ignorar Left Click (Romper bloque se maneja en onBlockBreak)
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            return;
        }

        // 2. Lógica para abrir GUI (Click derecho con bloque de protección)
        ProtectionBlock protection = regionManager.getProtection(event.getClickedBlock().getLocation());
        if (protection != null && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && protection.getCenter().equals(event.getClickedBlock().getLocation())) {
            if (protection.getOwner().equals(player.getUniqueId()) || protection.isMember(player.getUniqueId())
                    || player.hasPermission("advancedprotection.admin")) {
                event.setCancelled(true);
                plugin.getGuiManager().openMainMenu(player, protection);
                return;
            }
        }

        // 3. Verificar WorldGuard para interacción (si está configurado)
        if (shouldCheckWorldGuardFirst() && plugin.getWorldGuardHook() != null
                && plugin.getWorldGuardHook().isEnabled()) {
            if (event.getClickedBlock().getType().isInteractable()) {
                if (!plugin.getWorldGuardHook().canInteract(player, event.getClickedBlock().getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // 4. Verificar flag INTERACT de AdvancedProtection
        // Solo verificamos INTERACT si el bloque es interactuable (cofres, puertas,
        // botones...)
        // Si es un bloque normal (tierra, piedra), dejamos pasar el evento para que
        // BlockPlaceEvent decida si se puede poner bloque.
        if (event.getClickedBlock().getType().isInteractable()) {
            // Excepción: Si el jugador está shifteando y tiene un bloque en la mano,
            // probablemente quiere poner bloque, no interactuar.
            boolean isPlacingBlock = player.isSneaking() && event.getItem() != null
                    && event.getItem().getType().isBlock();

            if (!isPlacingBlock) {
                if (!canBuild(player, event.getClickedBlock().getLocation(), ProtectionFlag.INTERACT)) {
                    event.setCancelled(true);
                    sendDenyMessage(player);

                    // Registrar intento bloqueado
                    ProtectionBlock protInteract = regionManager.getProtectionAt(event.getClickedBlock().getLocation());
                    if (protInteract != null) {
                        plugin.getLogManager().addLog(new com.crystalverse.advancedprotection.model.ProtectionLog(
                                protInteract.getId(),
                                com.crystalverse.advancedprotection.model.ProtectionLog.LogType.INTERACT_DENIED,
                                player.getUniqueId(),
                                player.getName(),
                                "Bloque: " + event.getClickedBlock().getType().name()));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
            org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker == null)
            return; // No fue un jugador (ni directo ni proyectil)

        Player victim = (Player) event.getEntity();
        ProtectionBlock protection = regionManager.getProtectionAt(victim.getLocation());

        if (protection != null) {
            if (!protection.hasFlag(ProtectionFlag.PVP)) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getConfigManager().getMessage("pvp_disabled"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        ProtectionBlock protection = regionManager.getProtectionAt(event.getLocation());
        if (protection != null) {
            if (!protection.hasFlag(ProtectionFlag.EXPLOSIONS)) {
                event.setCancelled(true);
                event.blockList().clear();
            }
        } else {
            java.util.Iterator<Block> it = event.blockList().iterator();
            while (it.hasNext()) {
                Block b = it.next();
                if (regionManager.getProtectionAt(b.getLocation()) != null) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
        if (event.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;

        ProtectionBlock protection = regionManager.getProtectionAt(event.getLocation());
        if (protection != null) {
            if (event.getEntity() instanceof org.bukkit.entity.Monster) {
                if (!protection.hasFlag(ProtectionFlag.MOB_SPAWN)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private int getProtectionLevel(Material material) {
        for (int i = 1; i <= 6; i++) {
            if (plugin.getConfigManager().getProtectionMaterial(i) == material) {
                return i;
            }
        }
        return -1;
    }

    private boolean canBuild(Player player, Location loc, ProtectionFlag flag) {
        ProtectionBlock protection = regionManager.getProtectionAt(loc);

        if (protection == null) {
            return true;
        }

        // Owner siempre puede
        if (protection.getOwner().equals(player.getUniqueId())) {
            return true;
        }

        // Admin bypass
        if (player.hasPermission("advancedprotection.bypass")) {
            return true;
        }

        // Verificar permisos granulares si está habilitado
        boolean granularPermsEnabled = plugin.getConfigManager().getConfig().getBoolean("permissions.enabled", true);

        if (granularPermsEnabled && protection.isMember(player.getUniqueId())) {
            // Determinar qué permiso necesita según el flag
            com.crystalverse.advancedprotection.model.MemberPermission requiredPerm = null;

            switch (flag) {
                case BLOCK_PLACE:
                    requiredPerm = com.crystalverse.advancedprotection.model.MemberPermission.PLACE_BLOCKS;
                    break;
                case BLOCK_BREAK:
                    requiredPerm = com.crystalverse.advancedprotection.model.MemberPermission.BREAK_BLOCKS;
                    break;
                case INTERACT:
                    requiredPerm = com.crystalverse.advancedprotection.model.MemberPermission.INTERACT;
                    break;
                case CHEST_ACCESS:
                    requiredPerm = com.crystalverse.advancedprotection.model.MemberPermission.CONTAINER_ACCESS;
                    break;
                default:
                    // Para otros flags, verificar BUILD general
                    requiredPerm = com.crystalverse.advancedprotection.model.MemberPermission.BUILD;
                    break;
            }

            if (requiredPerm != null && protection.hasMemberPermission(player.getUniqueId(), requiredPerm)) {
                return true;
            }
        } else if (protection.isMember(player.getUniqueId())) {
            // Sistema antiguo: miembros tienen acceso total
            return true;
        }

        // Verificar flag público
        if (protection.hasFlag(flag)) {
            return true;
        }

        return false;
    }

    private void sendDenyMessage(Player player) {
        // Enviar Título de Advertencia
        String title = plugin.getConfigManager().getMessage("warning_title");
        String subtitle = plugin.getConfigManager().getMessage("warning_subtitle");
        player.sendTitle(title, subtitle, 10, 40, 10);

        // Sonido de advertencia
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);

        // Mensaje en chat (opcional, ya está el título)
        player.sendMessage(plugin.getConfigManager().getMessage("area_protected"));
    }

    private void giveManual(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        org.bukkit.inventory.meta.BookMeta meta = (org.bukkit.inventory.meta.BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(plugin.getConfigManager().getMessage("manual_title"));
            meta.setAuthor(plugin.getConfigManager().getMessage("manual_author"));

            List<String> pages = plugin.getConfigManager().getMessageList("manual_content");
            // Combinar lista en una sola página o varias si es muy largo
            // Por simplicidad, unimos todo en un string con saltos de línea
            StringBuilder content = new StringBuilder();
            for (String line : pages) {
                content.append(line).append("\n");
            }
            meta.setPages(content.toString());

            book.setItemMeta(meta);
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
            player.sendMessage(plugin.getConfigManager().getMessage("inventory_full_manual"));
        } else {
            player.getInventory().addItem(book);
            player.sendMessage(plugin.getConfigManager().getMessage("manual_received"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null)
            return;

        // Optimización: Solo verificar si cambió de bloque
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        ProtectionBlock fromProtection = regionManager.getProtectionAt(from);
        ProtectionBlock toProtection = regionManager.getProtectionAt(to);

        // Caso 1: Entrando a una protección (null -> protección)
        if (fromProtection == null && toProtection != null) {
            sendEnterMessage(player, toProtection);
        }
        // Caso 2: Saliendo de una protección (protección -> null)
        else if (fromProtection != null && toProtection == null) {
            sendExitMessage(player, fromProtection);
        }
        // Caso 3: Cambiando de una protección a otra (protección A -> protección B)
        else if (fromProtection != null && toProtection != null) {
            if (!fromProtection.getId().equals(toProtection.getId())) {
                sendExitMessage(player, fromProtection);
                sendEnterMessage(player, toProtection);
            }
        }
    }

    private void sendEnterMessage(Player player, ProtectionBlock protection) {
        // Verificar cooldown
        long now = System.currentTimeMillis();
        Long lastMessage = messageCooldowns.get(player.getUniqueId());

        if (lastMessage != null && (now - lastMessage) < COOLDOWN_MS) {
            // Todavía en cooldown, no enviar mensaje
            return;
        }

        // Actualizar timestamp del último mensaje
        messageCooldowns.put(player.getUniqueId(), now);

        String ownerName = Bukkit.getOfflinePlayer(protection.getOwner()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        String msg = plugin.getConfigManager().getMessage("protection_enter")
                .replace("%owner%", ownerName);

        // Usar Action Bar para ser menos intrusivo pero visible "en pantalla"
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg));
    }

    private void sendExitMessage(Player player, ProtectionBlock protection) {
        // Verificar cooldown
        long now = System.currentTimeMillis();
        Long lastMessage = messageCooldowns.get(player.getUniqueId());

        if (lastMessage != null && (now - lastMessage) < COOLDOWN_MS) {
            // Todavía en cooldown, no enviar mensaje
            return;
        }

        // Actualizar timestamp del último mensaje
        messageCooldowns.put(player.getUniqueId(), now);

        String ownerName = Bukkit.getOfflinePlayer(protection.getOwner()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        String msg = plugin.getConfigManager().getMessage("protection_exit")
                .replace("%owner%", ownerName);

        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg));
    }

    /**
     * Verifica si se debe verificar WorldGuard primero según configuración
     */
    private boolean shouldCheckWorldGuardFirst() {
        if (plugin.getWorldGuardHook() == null || !plugin.getWorldGuardHook().isEnabled()) {
            return false;
        }
        String priority = plugin.getConfigManager().getConfig().getString("worldguard.priority", "worldguard_first");
        return priority.equalsIgnoreCase("worldguard_first");
    }
}
