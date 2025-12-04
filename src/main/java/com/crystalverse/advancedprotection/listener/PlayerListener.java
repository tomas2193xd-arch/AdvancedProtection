package com.crystalverse.advancedprotection.listener;

import com.crystalverse.advancedprotection.AdvancedProtection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener para eventos de jugadores (login, etc)
 */
public class PlayerListener implements Listener {

    private final AdvancedProtection plugin;

    public PlayerListener(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Verificar impuestos pendientes
        if (plugin.getTaxManager() != null && plugin.getTaxManager().isEnabled()) {
            plugin.getTaxManager().checkTaxesOnLogin(player);
        }
    }
}
