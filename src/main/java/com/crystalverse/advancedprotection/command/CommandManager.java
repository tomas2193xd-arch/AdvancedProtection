package com.crystalverse.advancedprotection.command;

import com.crystalverse.advancedprotection.AdvancedProtection;

public class CommandManager {
    private final AdvancedProtection plugin;

    public CommandManager(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        MainCommand mainCommand = new MainCommand(plugin);
        plugin.getCommand("advancedprotection").setExecutor(mainCommand);
        plugin.getCommand("advancedprotection").setTabCompleter(mainCommand);
    }
}
