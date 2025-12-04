package com.crystalverse.advancedprotection.command;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final AdvancedProtection plugin;

    public MainCommand(AdvancedProtection plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getGuiManager().openGlobalMenu((Player) sender);
            } else {
                sender.sendMessage(ChatColor.GREEN + "AdvancedProtection v" + plugin.getDescription().getVersion());
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "menu":
                if (sender instanceof Player) {
                    plugin.getGuiManager().openGlobalMenu((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                }
                break;

            case "reload":
                if (!sender.hasPermission("advancedprotection.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                break;

            case "visualize":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                Player player = (Player) sender;
                ProtectionBlock protection = plugin.getRegionManager().getProtectionAt(player.getLocation());
                if (protection != null) {
                    plugin.getVisualEffectsManager().showBorders(player, protection);
                    player.sendMessage(ChatColor.GREEN + "Visualizing protection borders.");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You are not inside any protection.");
                }
                break;

            case "logs":
                if (!sender.hasPermission("advancedprotection.admin.logs")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                Player p = (Player) sender;
                ProtectionBlock prot = plugin.getRegionManager().getProtectionAt(p.getLocation());

                if (prot == null) {
                    p.sendMessage(ChatColor.YELLOW + "You must be inside a protection to view its logs.");
                    return true;
                }

                // Mostrar últimos 10 logs
                java.util.List<com.crystalverse.advancedprotection.model.ProtectionLog> logs = plugin.getLogManager()
                        .getLogs(prot.getId(), 10);

                if (logs.isEmpty()) {
                    p.sendMessage(ChatColor.YELLOW + "No logs registered for this protection.");
                } else {
                    p.sendMessage(ChatColor.GOLD + "=== Protection Logs " +
                            prot.getId().toString().substring(0, 8) + " ===");
                    for (com.crystalverse.advancedprotection.model.ProtectionLog log : logs) {
                        p.sendMessage(ChatColor.GRAY + log.toString());
                    }
                    p.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.YELLOW + logs.size() + " events");
                }
                break;

            case "trust":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ap trust <player>");
                    return true;
                }

                Player trustPlayer = (Player) sender;
                ProtectionBlock trustProt = plugin.getRegionManager().getProtectionAt(trustPlayer.getLocation());

                if (trustProt == null) {
                    trustPlayer.sendMessage(ChatColor.YELLOW + "You must be inside your protection.");
                    return true;
                }

                if (!trustProt.getOwner().equals(trustPlayer.getUniqueId())) {
                    trustPlayer.sendMessage(ChatColor.RED + "Only the owner can add members.");
                    return true;
                }

                Player targetPlayer = org.bukkit.Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    trustPlayer.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                if (trustProt.isMember(targetPlayer.getUniqueId())) {
                    trustPlayer.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " is already a member.");
                    return true;
                }

                trustProt.addMember(targetPlayer.getUniqueId());
                plugin.getProtectionService().saveProtection(trustProt);

                // Actualizar holograma
                if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                    plugin.getHologramManager().updateHologram(trustProt);
                }

                trustPlayer.sendMessage(ChatColor.GREEN + "✓ " + targetPlayer.getName() + " added as member.");
                targetPlayer
                        .sendMessage(
                                ChatColor.GREEN + "You've been added to " + trustPlayer.getName() + "'s protection.");
                break;

            case "untrust":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ap untrust <player>");
                    return true;
                }

                Player untrustPlayer = (Player) sender;
                ProtectionBlock untrustProt = plugin.getRegionManager().getProtectionAt(untrustPlayer.getLocation());

                if (untrustProt == null) {
                    untrustPlayer.sendMessage(ChatColor.YELLOW + "You must be inside your protection.");
                    return true;
                }

                if (!untrustProt.getOwner().equals(untrustPlayer.getUniqueId())) {
                    untrustPlayer.sendMessage(ChatColor.RED + "Only the owner can remove members.");
                    return true;
                }

                Player targetUntrust = org.bukkit.Bukkit.getPlayer(args[1]);
                if (targetUntrust == null) {
                    // Buscar por nombre offline
                    org.bukkit.OfflinePlayer offlineTarget = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
                    if (!untrustProt.isMember(offlineTarget.getUniqueId())) {
                        untrustPlayer.sendMessage(ChatColor.YELLOW + args[1] + " is not a member.");
                        return true;
                    }

                    untrustProt.removeMember(offlineTarget.getUniqueId());
                    plugin.getProtectionService().saveProtection(untrustProt);

                    // Actualizar holograma
                    if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                        plugin.getHologramManager().updateHologram(untrustProt);
                    }

                    untrustPlayer.sendMessage(ChatColor.GREEN + "✓ " + args[1] + " removed.");
                } else {
                    if (!untrustProt.isMember(targetUntrust.getUniqueId())) {
                        untrustPlayer.sendMessage(ChatColor.YELLOW + targetUntrust.getName() + " is not a member.");
                        return true;
                    }

                    untrustProt.removeMember(targetUntrust.getUniqueId());
                    plugin.getProtectionService().saveProtection(untrustProt);

                    // Actualizar holograma
                    if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                        plugin.getHologramManager().updateHologram(untrustProt);
                    }

                    untrustPlayer.sendMessage(ChatColor.GREEN + "✓ " + targetUntrust.getName() + " removed.");
                    targetUntrust.sendMessage(
                            ChatColor.YELLOW + "You've been removed from " + untrustPlayer.getName()
                                    + "'s protection.");
                }
                break;

            case "trustlist":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                Player listPlayer = (Player) sender;
                ProtectionBlock listProt = plugin.getRegionManager().getProtectionAt(listPlayer.getLocation());

                if (listProt == null) {
                    listPlayer.sendMessage(ChatColor.YELLOW + "You must be inside a protection.");
                    return true;
                }

                if (listProt.getMembers().isEmpty()) {
                    listPlayer.sendMessage(ChatColor.YELLOW + "This protection has no members.");
                } else {
                    listPlayer.sendMessage(ChatColor.GOLD + "=== Protection Members ===");
                    for (java.util.UUID memberId : listProt.getMembers()) {
                        String memberName = org.bukkit.Bukkit.getOfflinePlayer(memberId).getName();
                        listPlayer.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE
                                + (memberName != null ? memberName : "Unknown"));
                    }
                    listPlayer
                            .sendMessage(ChatColor.GRAY + "Total: " + ChatColor.YELLOW + listProt.getMembers().size());
                }
                break;

            case "rename":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ap rename <name>");
                    return true;
                }

                Player renamePlayer = (Player) sender;
                ProtectionBlock renameProt = plugin.getRegionManager().getProtectionAt(renamePlayer.getLocation());

                if (renameProt == null) {
                    renamePlayer.sendMessage(ChatColor.YELLOW + "You must be inside your protection.");
                    return true;
                }

                if (!renameProt.getOwner().equals(renamePlayer.getUniqueId())) {
                    renamePlayer.sendMessage(ChatColor.RED + "Only the owner can rename the protection.");
                    return true;
                }

                // Unir todos los argumentos desde args[1] en adelante
                StringBuilder newName = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1)
                        newName.append(" ");
                    newName.append(args[i]);
                }

                String finalName = newName.toString();

                // Validar longitud
                if (finalName.length() > 20) {
                    renamePlayer.sendMessage(ChatColor.RED + "Name cannot be longer than 20 characters.");
                    return true;
                }

                // Establecer nombre
                renameProt.setCustomName(finalName);
                plugin.getProtectionService().saveProtection(renameProt);

                // Actualizar holograma
                if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                    plugin.getHologramManager().updateHologram(renameProt);
                }

                renamePlayer.sendMessage(ChatColor.GREEN + "✓ Protection renamed to: " + ChatColor.GOLD + finalName);
                break;

            case "clearname":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                Player clearPlayer = (Player) sender;
                ProtectionBlock clearProt = plugin.getRegionManager().getProtectionAt(clearPlayer.getLocation());

                if (clearProt == null) {
                    clearPlayer.sendMessage(ChatColor.YELLOW + "You must be inside your protection.");
                    return true;
                }

                if (!clearProt.getOwner().equals(clearPlayer.getUniqueId())) {
                    clearPlayer.sendMessage(ChatColor.RED + "Only the owner can remove the protection name.");
                    return true;
                }

                if (!clearProt.hasCustomName()) {
                    clearPlayer.sendMessage(ChatColor.YELLOW + "This protection doesn't have a custom name.");
                    return true;
                }

                clearProt.setCustomName(null);
                plugin.getProtectionService().saveProtection(clearProt);

                // Actualizar holograma
                if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                    plugin.getHologramManager().updateHologram(clearProt);
                }

                clearPlayer.sendMessage(ChatColor.GREEN + "✓ Protection name removed.");
                break;

            case "paytax":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                // Verificar si el sistema de impuestos está habilitado
                if (plugin.getTaxManager() == null || !plugin.getTaxManager().isEnabled()) {
                    sender.sendMessage(ChatColor.YELLOW + "The tax system is not enabled.");
                    return true;
                }

                Player taxPlayer = (Player) sender;
                ProtectionBlock taxProt = plugin.getRegionManager().getProtectionAt(taxPlayer.getLocation());

                if (taxProt == null) {
                    taxPlayer.sendMessage(ChatColor.YELLOW + "You must be inside your protection.");
                    return true;
                }

                if (!taxProt.getOwner().equals(taxPlayer.getUniqueId())) {
                    taxPlayer.sendMessage(ChatColor.RED + "Only the owner can pay taxes.");
                    return true;
                }

                // Procesar pago
                plugin.getTaxManager().payTax(taxPlayer, taxProt);
                break;

            case "help":
            default:
                sender.sendMessage(ChatColor.GOLD + "--- AdvancedProtection Help ---");
                sender.sendMessage(ChatColor.YELLOW + "/ap menu " + ChatColor.GRAY + "- Open main menu.");
                sender.sendMessage(ChatColor.YELLOW + "/ap reload " + ChatColor.GRAY + "- Reload configuration.");
                sender.sendMessage(
                        ChatColor.YELLOW + "/ap visualize " + ChatColor.GRAY + "- View current protection borders.");
                sender.sendMessage(ChatColor.YELLOW + "/ap trust <player> " + ChatColor.GRAY + "- Add member.");
                sender.sendMessage(ChatColor.YELLOW + "/ap untrust <player> " + ChatColor.GRAY + "- Remove member.");
                sender.sendMessage(ChatColor.YELLOW + "/ap trustlist " + ChatColor.GRAY + "- View member list.");
                sender.sendMessage(
                        ChatColor.YELLOW + "/ap rename <name> " + ChatColor.GRAY + "- Rename protection.");
                sender.sendMessage(
                        ChatColor.YELLOW + "/ap clearname " + ChatColor.GRAY + "- Remove protection name.");
                if (sender.hasPermission("advancedprotection.admin.logs")) {
                    sender.sendMessage(
                            ChatColor.YELLOW + "/ap logs " + ChatColor.GRAY
                                    + "- View current protection history.");
                }
                sender.sendMessage(
                        ChatColor.YELLOW + "/ap paytax " + ChatColor.GRAY + "- Pay protection tax.");
                if (sender.hasPermission("advancedprotection.admin.manager")) {
                    sender.sendMessage(
                            ChatColor.YELLOW + "/ap manager " + ChatColor.GRAY + "- Open administration panel.");
                }
                break;

            case "manager":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (!sender.hasPermission("advancedprotection.admin.manager")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                    return true;
                }
                plugin.getGuiManager().openAdminManager((Player) sender, 0);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcomandos principales
            List<String> subcommands = Arrays.asList(
                    "menu", "reload", "visualize", "trust", "untrust",
                    "trustlist", "rename", "clearname", "logs", "manager", "paytax", "help");

            // Filtrar según lo que el jugador ya escribió
            String input = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    // Verificar permisos para comandos especiales
                    if (sub.equals("logs") && !sender.hasPermission("advancedprotection.admin.logs")) {
                        continue;
                    }
                    if (sub.equals("manager") && !sender.hasPermission("advancedprotection.admin.manager")) {
                        continue;
                    }
                    if (sub.equals("reload") && !sender.hasPermission("advancedprotection.admin")) {
                        continue;
                    }
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            // Sugerencias para comandos que requieren nombres de jugadores
            String subcommand = args[0].toLowerCase();

            if (subcommand.equals("trust") || subcommand.equals("untrust")) {
                // Sugerir nombres de jugadores online
                String input = args[1].toLowerCase();
                for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (online.getName().toLowerCase().startsWith(input)) {
                        completions.add(online.getName());
                    }
                }
            }
        }

        return completions;
    }
}
