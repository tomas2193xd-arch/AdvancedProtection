package com.crystalverse.advancedprotection.hook;

import com.crystalverse.advancedprotection.AdvancedProtection;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {

    private final AdvancedProtection plugin;

    public PlaceholderHook(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "advancedprotection";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on
                     // reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // %advancedprotection_count%
        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(plugin.getRegionManager().getPlayerProtections(player.getUniqueId()).size());
        }

        // %advancedprotection_limit%
        if (params.equalsIgnoreCase("limit")) {
            // TODO: Implement limit logic based on permissions or config
            return "∞";
        }

        return null; // Placeholder is unknown by the Expansion
    }
}
