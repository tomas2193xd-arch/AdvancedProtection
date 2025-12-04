package com.crystalverse.advancedprotection.hook;

import com.crystalverse.advancedprotection.AdvancedProtection;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

    private final AdvancedProtection plugin;
    private Economy economy = null;

    public EconomyHook(AdvancedProtection plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Economy hooked into: " + economy.getName());
        return economy != null;
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (economy == null)
            return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null)
            return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (economy == null)
            return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (economy == null)
            return String.valueOf(amount);
        return economy.format(amount);
    }

    public double getBalance(OfflinePlayer player) {
        if (economy == null)
            return 0.0;
        return economy.getBalance(player);
    }

    public boolean isEnabled() {
        return economy != null;
    }
}
