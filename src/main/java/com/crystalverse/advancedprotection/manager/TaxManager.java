package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Gestor del sistema de impuestos/renta automático
 */
public class TaxManager {

    private final AdvancedProtection plugin;
    private boolean enabled;
    private int intervalHours;
    private String rateMode;
    private double taxRate;
    private int gracePeriodHours;
    private List<Integer> warnHoursBefore;
    private boolean warnOnLogin;

    private int taskId = -1;

    public TaxManager(AdvancedProtection plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    /**
     * Carga configuración del sistema de impuestos
     */
    public void loadConfiguration() {
        enabled = plugin.getConfigManager().getConfig().getBoolean("tax_system.enabled", false);
        intervalHours = plugin.getConfigManager().getConfig().getInt("tax_system.interval_hours", 24);
        rateMode = plugin.getConfigManager().getConfig().getString("tax_system.rate_mode", "percentage");
        taxRate = plugin.getConfigManager().getConfig().getDouble("tax_system.tax_rate", 5.0);
        gracePeriodHours = plugin.getConfigManager().getConfig().getInt("tax_system.grace_period_hours", 72);
        warnHoursBefore = plugin.getConfigManager().getConfig()
                .getIntegerList("tax_system.notifications.warn_hours_before");
        if (warnHoursBefore == null || warnHoursBefore.isEmpty()) {
            warnHoursBefore = new ArrayList<>();
            warnHoursBefore.add(24);
            warnHoursBefore.add(12);
            warnHoursBefore.add(6);
            warnHoursBefore.add(1);
        }
        warnOnLogin = plugin.getConfigManager().getConfig().getBoolean("tax_system.notifications.warn_on_login", true);
    }

    /**
     * Inicia el sistema de impuestos
     */
    public void start() {
        if (!enabled) {
            plugin.getLogger().info("Tax system disabled.");
            return;
        }

        // Inicializar fechas de impuestos para protecciones existentes
        initializeExistingProtections();

        // Tarea que se ejecuta cada hora para verificar impuestos
        long ticksPerHour = 20L * 60 * 60; // 72000 ticks = 1 hora
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkTaxes, ticksPerHour, ticksPerHour);

        plugin.getLogger().info("Tax system started. Interval: " + intervalHours + "h, Rate: " + taxRate
                + (rateMode.equals("percentage") ? "%" : "$"));
    }

    /**
     * Detiene el sistema de impuestos
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Inicializa fechas de impuestos para protecciones existentes sin fecha
     */
    private void initializeExistingProtections() {
        int initialized = 0;
        for (ProtectionBlock protection : plugin.getRegionManager().getAllProtections()) {
            if (protection.getNextTaxDue() == 0) {
                long nextDue = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(intervalHours);
                protection.setNextTaxDue(nextDue);
                protection.setRentEnabled(true);
                plugin.getProtectionService().saveProtection(protection);
                initialized++;
            }
        }
        if (initialized > 0) {
            plugin.getLogger().info("Initialized " + initialized + " protections to the tax system.");
        }
    }

    /**
     * Verifica y procesa impuestos de todas las protecciones
     */
    public void checkTaxes() {
        if (!enabled)
            return;

        long now = System.currentTimeMillis();
        List<ProtectionBlock> toDelete = new ArrayList<>();

        for (ProtectionBlock protection : plugin.getRegionManager().getAllProtections()) {
            if (!protection.isRentEnabled()) {
                continue;
            }

            long timeUntilDue = protection.getTimeUntilTaxDue();

            // Verificar si está vencido
            if (protection.isTaxOverdue()) {
                long overdueDuration = now - protection.getNextTaxDue();
                long gracePeriodMillis = TimeUnit.HOURS.toMillis(gracePeriodHours);

                if (overdueDuration > gracePeriodMillis) {
                    // Período de gracia superado, eliminar protección
                    toDelete.add(protection);
                    notifyOwnerTaxDeletion(protection);
                } else {
                    // Dentro del período de gracia, notificar
                    notifyOwnerOverdue(protection, overdueDuration, gracePeriodMillis);
                }
            } else {
                // No vencido, verificar si hay que enviar advertencias
                checkAndSendWarnings(protection, timeUntilDue);
            }
        }

        // Eliminar protecciones vencidas
        for (ProtectionBlock protection : toDelete) {
            plugin.getProtectionService().deleteProtection(protection.getId());
            plugin.getLogger().info("Protection " + protection.getId() + " deleted for tax non-payment.");
        }
    }

    /**
     * Verifica y envía advertencias antes del vencimiento
     */
    private void checkAndSendWarnings(ProtectionBlock protection, long timeUntilDue) {
        long hoursUntilDue = TimeUnit.MILLISECONDS.toHours(timeUntilDue);

        for (int warnHours : warnHoursBefore) {
            // Ventana de 30 minutos para enviar la advertencia
            if (hoursUntilDue <= warnHours && hoursUntilDue > (warnHours - 1)) {
                notifyOwnerUpcoming(protection, hoursUntilDue);
                break;
            }
        }
    }

    /**
     * Notifica al dueño que el impuesto está próximo a vencer
     */
    private void notifyOwnerUpcoming(ProtectionBlock protection, long hoursRemaining) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(protection.getOwner());
        if (owner.isOnline()) {
            Player player = owner.getPlayer();
            double taxAmount = calculateTax(protection);

            player.sendMessage(ChatColor.YELLOW + "⚠ Upcoming tax payment:");
            player.sendMessage(
                    ChatColor.GRAY + "Protection: " + ChatColor.WHITE + getProtectionDisplayName(protection));
            player.sendMessage(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + "$" + String.format("%.2f", taxAmount));
            player.sendMessage(ChatColor.GRAY + "Time remaining: " + ChatColor.RED + hoursRemaining + " hours");
            player.sendMessage(ChatColor.GRAY + "Use /ap paytax to pay.");
        }
    }

    /**
     * Notifica al dueño que el impuesto está vencido
     */
    private void notifyOwnerOverdue(ProtectionBlock protection, long overdueDuration, long gracePeriodMillis) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(protection.getOwner());
        if (owner.isOnline()) {
            Player player = owner.getPlayer();
            long hoursLeft = TimeUnit.MILLISECONDS.toHours(gracePeriodMillis - overdueDuration);
            double taxAmount = calculateTax(protection);

            player.sendMessage(ChatColor.RED + "⚠⚠⚠ TAX OVERDUE ⚠⚠⚠");
            player.sendMessage(
                    ChatColor.GRAY + "Protection: " + ChatColor.WHITE + getProtectionDisplayName(protection));
            player.sendMessage(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + "$" + String.format("%.2f", taxAmount));
            player.sendMessage(
                    ChatColor.GRAY + "Time before deletion: " + ChatColor.DARK_RED + hoursLeft + " hours");
            player.sendMessage(ChatColor.YELLOW + "Pay NOW with /ap paytax!");
        }
    }

    /**
     * Notifica al dueño que su protección fue eliminada
     */
    private void notifyOwnerTaxDeletion(ProtectionBlock protection) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(protection.getOwner());
        if (owner.isOnline()) {
            Player player = owner.getPlayer();
            player.sendMessage(ChatColor.DARK_RED + "═══════════════════════════");
            player.sendMessage(ChatColor.RED + " ⚠ PROTECTION DELETED ⚠");
            player.sendMessage(
                    ChatColor.GRAY + "Your protection " + ChatColor.WHITE + getProtectionDisplayName(protection));
            player.sendMessage(ChatColor.GRAY + "was deleted for tax non-payment.");
            player.sendMessage(ChatColor.DARK_RED + "═══════════════════════════");
        }
    }

    /**
     * Calcula el impuesto de una protección
     */
    public double calculateTax(ProtectionBlock protection) {
        // Verificar si hay tasa personalizada por nivel
        String levelKey = "tax_system.level_rates." + protection.getLevel();
        if (plugin.getConfigManager().getConfig().contains(levelKey)) {
            return plugin.getConfigManager().getConfig().getDouble(levelKey);
        }

        // Usar tasa global
        if (rateMode.equals("percentage")) {
            double protectionCost = plugin.getConfigManager().getProtectionCost(protection.getLevel());
            return protectionCost * (taxRate / 100.0);
        } else {
            return taxRate;
        }
    }

    /**
     * Procesa el pago de impuesto de una protección
     */
    public boolean payTax(Player player, ProtectionBlock protection) {
        double taxAmount = calculateTax(protection);

        if (!plugin.getEconomyHook().withdraw(player, taxAmount)) {
            player.sendMessage(
                    ChatColor.RED + "Insufficient funds. You need: $" + String.format("%.2f", taxAmount));
            return false;
        }

        // Actualizar fechas
        long now = System.currentTimeMillis();
        protection.setLastTaxPaid(now);
        protection.setNextTaxDue(now + TimeUnit.HOURS.toMillis(intervalHours));
        plugin.getProtectionService().saveProtection(protection);

        player.sendMessage(ChatColor.GREEN + "✓ Tax paid: $" + String.format("%.2f", taxAmount));
        player.sendMessage(ChatColor.GRAY + "Next payment: " + formatTime(protection.getTimeUntilTaxDue()));

        return true;
    }

    /**
     * Formatea tiempo en formato legible
     */
    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long days = hours / 24;
        long remainingHours = hours % 24;

        if (days > 0) {
            return days + "d " + remainingHours + "h";
        }
        return hours + "h";
    }

    /**
     * Obtiene nombre de display de una protección
     */
    private String getProtectionDisplayName(ProtectionBlock protection) {
        if (protection.hasCustomName()) {
            return protection.getCustomName();
        }
        return plugin.getConfigManager().getProtectionName(protection.getLevel());
    }

    /**
     * Verifica impuestos al login del jugador
     */
    public void checkTaxesOnLogin(Player player) {
        if (!enabled || !warnOnLogin)
            return;

        List<ProtectionBlock> protections = plugin.getRegionManager().getPlayerProtections(player.getUniqueId());

        for (ProtectionBlock protection : protections) {
            if (!protection.isRentEnabled())
                continue;

            if (protection.isTaxOverdue()) {
                long overdueDuration = System.currentTimeMillis() - protection.getNextTaxDue();
                long gracePeriodMillis = TimeUnit.HOURS.toMillis(gracePeriodHours);
                notifyOwnerOverdue(protection, overdueDuration, gracePeriodMillis);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
