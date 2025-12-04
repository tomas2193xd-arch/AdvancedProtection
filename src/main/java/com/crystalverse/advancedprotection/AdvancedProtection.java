package com.crystalverse.advancedprotection;

import com.crystalverse.advancedprotection.command.CommandManager;
import com.crystalverse.advancedprotection.config.ConfigManager;
import com.crystalverse.advancedprotection.manager.GuiManager;
import com.crystalverse.advancedprotection.manager.RegionManager;
import com.crystalverse.advancedprotection.manager.VisualEffectsManager;
import com.crystalverse.advancedprotection.service.ProtectionService;
import com.crystalverse.advancedprotection.api.IStorageProvider;
import com.crystalverse.advancedprotection.data.JsonStorageProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AdvancedProtection extends JavaPlugin {

    private static AdvancedProtection instance;
    private ConfigManager configManager;
    private IStorageProvider storageProvider;
    private RegionManager regionManager;
    private ProtectionService protectionService;
    private VisualEffectsManager visualEffectsManager;
    private GuiManager guiManager;
    private CommandManager commandManager;
    private com.crystalverse.advancedprotection.hook.EconomyHook economyHook;
    private com.crystalverse.advancedprotection.hook.WorldGuardHook worldGuardHook;
    private com.crystalverse.advancedprotection.manager.LogManager logManager;
    private com.crystalverse.advancedprotection.manager.HologramManager hologramManager;
    private com.crystalverse.advancedprotection.manager.TaxManager taxManager;

    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();

        // 1. Cargar Configuración
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        // Inicializar Economía
        this.economyHook = new com.crystalverse.advancedprotection.hook.EconomyHook(this);
        if (!economyHook.setupEconomy()) {
            logger.warning(
                    "Vault no encontrado o sin plugin de economía. Las funciones de compra estarán deshabilitadas.");
        }

        // Inicializar WorldGuard
        this.worldGuardHook = new com.crystalverse.advancedprotection.hook.WorldGuardHook(this);
        if (configManager.getConfig().getBoolean("worldguard.enabled", true)) {
            if (worldGuardHook.setupWorldGuard()) {
                logger.info("WorldGuard detectado y configurado correctamente.");
            } else {
                logger.info("WorldGuard no encontrado. Funcionando sin integración WG.");
            }
        } else {
            logger.info("Integración con WorldGuard desactivada en config.yml");
        }

        // Hook de PlaceholderAPI
        /*
         * if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         * new
         * com.crystalverse.advancedprotection.hook.PlaceholderHook(this).register();
         * logger.info("PlaceholderAPI encontrado. Hook registrado.");
         * }
         */

        // 2. Inicializar Almacenamiento
        this.storageProvider = new JsonStorageProvider(this);
        if (!this.storageProvider.init()) {
            logger.severe("No se pudo inicializar el almacenamiento. Deshabilitando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 3. Inicializar Managers y Servicios
        this.regionManager = new RegionManager(this);
        this.visualEffectsManager = new VisualEffectsManager(this);
        this.logManager = new com.crystalverse.advancedprotection.manager.LogManager(this);
        this.guiManager = new GuiManager(this);

        // Inicializar HologramManager
        this.hologramManager = new com.crystalverse.advancedprotection.manager.HologramManager(this);
        this.hologramManager.initialize();

        // Inicializar TaxManager
        this.taxManager = new com.crystalverse.advancedprotection.manager.TaxManager(this);
        this.taxManager.start();

        // El servicio orquesta la lógica
        this.protectionService = new ProtectionService(this, regionManager, storageProvider, configManager);
        this.protectionService.loadProtections();

        // 4. Registrar Comandos y Eventos
        this.commandManager = new CommandManager(this);
        this.commandManager.registerCommands();

        // Registrar Listeners
        getServer().getPluginManager()
                .registerEvents(new com.crystalverse.advancedprotection.listener.ProtectionListener(this), this);
        getServer().getPluginManager()
                .registerEvents(new com.crystalverse.advancedprotection.listener.PlayerListener(this), this);

        // Mensaje de inicio profesional
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA
                + "   _   ___  _   _  _   _  _  ___ ___ ___   ___ ___  ___ _____ ___ ___ _____ ___ ___  _  _ ");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA
                + "  /_\\ |   \\| | | |/_\\ | \\| |/ __| __|   \\ | _ \\ _ \\/ _ \\_   _| __/ __|_   _|_ _/ _ \\| \\| |");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA
                + " / _ \\| |) | |_| / _ \\| .` | (__| _|| |) ||  _/   / (_) || | | _| (__  | |  | | (_) | .` |");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA
                + "/_/ \\_\\___/ \\___/_/ \\_\\_|\\_|\\___|___|___/ |_| |_|_\\\\___/ |_| |___\\___| |_| |___\\___/|_|\\_|");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.GRAY + "  Version: " + ChatColor.GREEN + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "  Author: " + ChatColor.GREEN + "CrystalVerse Team");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "  Status: " + ChatColor.GREEN + "ENABLED");
        Bukkit.getConsoleSender().sendMessage("");

        // Generar documentación de permisos
        new com.crystalverse.advancedprotection.manager.PermissionsGenerator(this).generate();
    }

    @Override
    public void onDisable() {
        if (storageProvider != null) {
            storageProvider.shutdown();
        }
        if (visualEffectsManager != null) {
            visualEffectsManager.shutdown(); // Limpiar tareas de partículas
        }
        if (hologramManager != null) {
            hologramManager.shutdown(); // Limpiar hologramas
        }
        if (taxManager != null) {
            taxManager.stop(); // Detener verificación de impuestos
        }

        // Mensaje Premium de Despedida
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "==========================================");
        Bukkit.getConsoleSender()
                .sendMessage(ChatColor.RED + "   AdvancedProtection v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   Status: " + ChatColor.DARK_RED + "DISABLED");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "   See you soon!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "==========================================");
    }

    public static AdvancedProtection getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public ProtectionService getProtectionService() {
        return protectionService;
    }

    public VisualEffectsManager getVisualEffectsManager() {
        return visualEffectsManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public com.crystalverse.advancedprotection.hook.EconomyHook getEconomyHook() {
        return economyHook;
    }

    public com.crystalverse.advancedprotection.manager.LogManager getLogManager() {
        return logManager;
    }

    public com.crystalverse.advancedprotection.hook.WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public com.crystalverse.advancedprotection.manager.HologramManager getHologramManager() {
        return hologramManager;
    }

    public com.crystalverse.advancedprotection.manager.TaxManager getTaxManager() {
        return taxManager;
    }
}
