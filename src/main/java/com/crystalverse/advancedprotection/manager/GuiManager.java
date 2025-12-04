package com.crystalverse.advancedprotection.manager;

import com.crystalverse.advancedprotection.AdvancedProtection;
import com.crystalverse.advancedprotection.model.ProtectionBlock;
import com.crystalverse.advancedprotection.model.ProtectionFlag;
import com.crystalverse.advancedprotection.model.MemberPermission;
import com.crystalverse.advancedprotection.model.MemberData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class GuiManager implements Listener {
    private final AdvancedProtection plugin;
    private final Map<UUID, ProtectionBlock> editingSessions = new HashMap<>();
    private final Set<UUID> adminViewers = new HashSet<>();

    public GuiManager(AdvancedProtection plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Títulos de Inventarios (Ahora dinámicos)
    private String getTitle(String key) {
        return plugin.getConfigManager().getMessage("gui." + key);
    }

    // --- MENÚS ---

    public void openGlobalMenu(Player player) {
        adminViewers.remove(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 45, getTitle("main_title"));
        fillBorders(inv);

        ItemStack info = createItem(Material.BOOK, plugin.getConfigManager().getMessage("gui.info_name"),
                plugin.getConfigManager().getMessageList("gui.info_lore"));
        inv.setItem(20, info);

        ItemStack myProtections = createItem(Material.ENDER_CHEST,
                plugin.getConfigManager().getMessage("gui.my_protections_name"),
                plugin.getConfigManager().getMessageList("gui.my_protections_lore"));
        inv.setItem(22, myProtections);

        ItemStack shop = createItem(Material.EMERALD, plugin.getConfigManager().getMessage("gui.shop_name"),
                plugin.getConfigManager().getMessageList("gui.shop_lore"));
        inv.setItem(24, shop);

        ItemStack language = createItem(Material.PAPER, plugin.getConfigManager().getMessage("gui.language_name"),
                plugin.getConfigManager().getMessageList("gui.language_lore"));
        inv.setItem(26, language);

        ItemStack compat = createItem(Material.COMPASS, plugin.getConfigManager().getMessage("gui.compat_name"),
                replacePlaceholders(plugin.getConfigManager().getMessageList("gui.compat_lore"),
                        "%version%", plugin.getDescription().getVersion()));
        inv.setItem(18, compat);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    public void openLanguageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, getTitle("language_title"));
        fillBorders(inv);

        ItemStack es = createItem(Material.PAINTING, ChatColor.YELLOW + "Español",
                ChatColor.GRAY + "Click para seleccionar Español.");
        inv.setItem(11, es);

        ItemStack en = createItem(Material.PAINTING, ChatColor.YELLOW + "English",
                ChatColor.GRAY + "Click to select English.");
        inv.setItem(15, en);

        addBackButton(inv, 22);
        player.openInventory(inv);
    }

    public void openPlayerProtections(Player player) {
        List<ProtectionBlock> protections = plugin.getRegionManager().getPlayerProtections(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, getTitle("my_protections_title"));
        fillBorders(inv);

        int slot = 10;
        for (ProtectionBlock protection : protections) {
            if (slot % 9 == 0 || slot % 9 == 8)
                slot += 2;
            if (slot >= 44)
                break;

            String locStr = String.format("X:%d Y:%d Z:%d",
                    protection.getCenter().getBlockX(),
                    protection.getCenter().getBlockY(),
                    protection.getCenter().getBlockZ());

            // Fix: Use Ores instead of Blocks (except Obsidian)
            Material mat = plugin.getConfigManager().getProtectionMaterial(protection.getLevel());
            if (mat == Material.COAL_BLOCK)
                mat = Material.COAL_ORE;
            else if (mat == Material.IRON_BLOCK)
                mat = Material.IRON_ORE;
            else if (mat == Material.GOLD_BLOCK)
                mat = Material.GOLD_ORE;
            else if (mat == Material.DIAMOND_BLOCK)
                mat = Material.DIAMOND_ORE;
            else if (mat == Material.EMERALD_BLOCK)
                mat = Material.EMERALD_ORE;
            // Obsidian stays Obsidian

            ItemStack item = createItem(mat,
                    plugin.getConfigManager().getMessage("gui.protection_item_name").replace("%id%",
                            protection.getId().toString().substring(0, 8)),
                    replacePlaceholders(plugin.getConfigManager().getMessageList("gui.protection_item_lore"),
                            "%id%", protection.getId().toString().substring(0, 8),
                            "%location%", locStr,
                            "%level%", String.valueOf(protection.getLevel()),
                            "%radius%",
                            String.valueOf(plugin.getConfigManager().getProtectionRadius(protection.getLevel()))));

            // Añadir ID al lore oculto para identificarlo al hacer click
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(ChatColor.BLACK + "ID: " + protection.getId().toString());
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot, item);
            slot++;
        }

        addBackButton(inv, 49);
        player.openInventory(inv);
    }

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, getTitle("shop_title"));
        fillBorders(inv);

        // Botón de Saldo (Centro)
        double balance = plugin.getEconomyHook().isEnabled() ? plugin.getEconomyHook().getBalance(player) : 0.0;
        ItemStack balanceItem = createItem(Material.SUNFLOWER,
                plugin.getConfigManager().getMessage("gui.balance_name"),
                replacePlaceholders(plugin.getConfigManager().getMessageList("gui.balance_lore"),
                        "%balance%", String.format("%,.2f", balance)));
        inv.setItem(4, balanceItem);

        // Slots simétricos para 6 items: 19, 20, 21 (Izq) y 23, 24, 25 (Der)
        int[] slots = { 19, 20, 21, 23, 24, 25 };

        for (int i = 1; i <= 6; i++) {
            double cost = plugin.getConfigManager().getProtectionCost(i);
            int radius = plugin.getConfigManager().getProtectionRadius(i);
            Material material = plugin.getConfigManager().getProtectionMaterial(i);
            String name = plugin.getConfigManager().getProtectionDisplayName(i);

            // Force Ores logic removed - handled by ConfigManager
            if (material == Material.OBSIDIAN) {
                name = "Obsidian Protection";
            } else if (name.contains("Block")) {
                name = name.replace("Block", "Protection");
            }

            ChatColor color = getLevelColor(i);

            ItemStack item = createItem(material, color + "" + ChatColor.BOLD + name.toUpperCase(),
                    replacePlaceholders(plugin.getConfigManager().getMessageList("gui.shop_item_lore"),
                            "%color%", color.toString(),
                            "%level%", String.valueOf(i),
                            "%radius%", String.valueOf(radius),
                            "%cost%", String.format("%,.0f", cost)));

            if (i <= slots.length) {
                inv.setItem(slots[i - 1], item);
            }
        }

        addBackButton(inv, 40);
        player.openInventory(inv);
    }

    public void openMainMenu(Player player, ProtectionBlock protection) {
        adminViewers.remove(player.getUniqueId());
        setEditingProtection(player, protection);
        Inventory inv = Bukkit.createInventory(null, 45, // Increased size to 45
                plugin.getConfigManager().getMessage("gui.protection_title")
                        .replace("%id%", protection.getId().toString().substring(0, 8)));

        for (int i = 0; i < 45; i++) { // Fill 45 slots
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        // --- REDESIGNED GUI (45 Slots) - SYMMETRICAL ---
        // Row 0: Glass
        // Row 1: Members (11) - Info (13) - Flags (15)
        // Row 2: Visualize (20) - [EMPTY 22] - Visual Settings (24)
        // Row 3: Glass
        // Row 4: Back (40)

        ItemStack info = createItem(Material.BOOK, plugin.getConfigManager().getMessage("gui.info_item_name"),
                replacePlaceholders(plugin.getConfigManager().getMessageList("gui.info_item_lore"),
                        "%level%", String.valueOf(protection.getLevel()),
                        "%radius%",
                        String.valueOf(plugin.getConfigManager().getProtectionRadius(protection.getLevel()))));
        inv.setItem(13, info); // Center Top

        ItemStack members = createItem(Material.PLAYER_HEAD,
                plugin.getConfigManager().getMessage("gui.members_item_name"),
                plugin.getConfigManager().getMessageList("gui.members_item_lore"));
        inv.setItem(11, members); // Left Top

        ItemStack flags = createItem(Material.REDSTONE_TORCH,
                plugin.getConfigManager().getMessage("gui.flags_item_name"),
                plugin.getConfigManager().getMessageList("gui.flags_item_lore"));
        inv.setItem(15, flags); // Right Top

        ItemStack visualize = createItem(Material.ENDER_EYE, plugin.getConfigManager().getMessage("gui.visualize_name"),
                plugin.getConfigManager().getMessageList("gui.visualize_lore"));
        inv.setItem(20, visualize); // Left Bottom

        // Visual Settings Button
        ItemStack visualSettings = createItem(Material.NETHER_STAR,
                plugin.getConfigManager().getMessage("gui.visual_settings_name"),
                plugin.getConfigManager().getMessageList("gui.visual_settings_lore"));
        inv.setItem(24, visualSettings); // Right Bottom (Symmetrical to Visualize)

        // Hologram Toggle Button (New)
        boolean holoEnabled = protection.isHologramEnabled();
        List<String> holoLore = Arrays.asList(
                "&7Toggle the floating info hologram",
                "&7above your protection center.",
                "",
                holoEnabled ? "&a&lENABLED" : "&c&lDISABLED",
                "&eClick to toggle!");
        List<String> coloredHoloLore = new ArrayList<>();
        for (String line : holoLore) {
            coloredHoloLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        ItemStack hologramToggle = createItem(Material.ARMOR_STAND,
                ChatColor.translateAlternateColorCodes('&', "&b&lInfo Hologram"),
                coloredHoloLore);
        inv.setItem(22, hologramToggle); // Center Bottom (Perfect Symmetry)

        // Back Button (Consistent with other menus)
        addBackButton(inv, 40);

        player.openInventory(inv);
    }

    public void openFlagsMenu(Player player, ProtectionBlock protection, boolean asAdmin) {
        String title = asAdmin ? ChatColor.DARK_RED + "Admin: Flags" : getTitle("flags_title");
        Inventory inv = Bukkit.createInventory(null, 27, title);
        fillBorders(inv);

        int[] slots = { 10, 11, 12, 13, 14, 15, 16 };
        int i = 0;
        for (ProtectionFlag flag : ProtectionFlag.values()) {
            if (i >= slots.length)
                break;

            boolean enabled = protection.hasFlag(flag);
            Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
            String status = enabled ? plugin.getConfigManager().getMessage("gui.flag_enabled")
                    : plugin.getConfigManager().getMessage("gui.flag_disabled");

            ItemStack item = createItem(mat, ChatColor.GOLD + flag.name(),
                    replacePlaceholders(plugin.getConfigManager().getMessageList("gui.flag_lore"),
                            "%status%", status));

            inv.setItem(slots[i], item);
            i++;
        }

        addBackButton(inv, 22);
        player.openInventory(inv);
    }

    public void openMembersMenu(Player player, ProtectionBlock protection) {
        Inventory inv = Bukkit.createInventory(null, 54, getTitle("members_title"));
        fillBorders(inv);

        ItemStack addMember = createItem(Material.PLAYER_HEAD,
                plugin.getConfigManager().getMessage("gui.add_member_name"),
                plugin.getConfigManager().getMessageList("gui.add_member_lore"));
        inv.setItem(4, addMember);

        int slot = 10;
        for (UUID memberId : protection.getMembers()) {
            if (slot >= 44)
                break;
            if (slot % 9 == 0 || slot % 9 == 8)
                slot += 2;

            String memberName = Bukkit.getOfflinePlayer(memberId).getName();
            if (memberName == null)
                memberName = plugin.getConfigManager().getMessage("unknown_player");

            ItemStack item = createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + memberName,
                    Arrays.asList(
                            ChatColor.GRAY + "UUID: " + ChatColor.WHITE + memberId.toString().substring(0, 8),
                            "",
                            ChatColor.GREEN + "Left Click: " + ChatColor.WHITE + "Manage Permissions",
                            ChatColor.RED + "Right Click: " + ChatColor.WHITE + "Remove Member"));

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(memberId));
                item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            slot++;
        }

        addBackButton(inv, 49);
        player.openInventory(inv);
    }

    public void openAddMemberMenu(Player player, ProtectionBlock protection) {
        Inventory inv = Bukkit.createInventory(null, 54, getTitle("add_member_title"));
        fillBorders(inv);

        int slot = 10;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId()))
                continue;
            if (protection.isMember(online.getUniqueId()))
                continue;

            if (slot >= 44)
                break;
            if (slot % 9 == 0 || slot % 9 == 8)
                slot += 2;

            ItemStack item = createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + online.getName(),
                    plugin.getConfigManager().getMessageList("gui.add_player_lore"));

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(online);
                item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            slot++;
        }

        addBackButton(inv, 49);
        player.openInventory(inv);
    }

    public void openVisualMenu(Player player, ProtectionBlock protection) {
        Inventory inv = Bukkit.createInventory(null, 27, getTitle("visual_title"));
        fillBorders(inv);

        org.bukkit.Particle[] particles = {
                org.bukkit.Particle.VILLAGER_HAPPY,
                org.bukkit.Particle.FLAME,
                org.bukkit.Particle.HEART,
                org.bukkit.Particle.NOTE,
                org.bukkit.Particle.SPELL_WITCH,
                org.bukkit.Particle.LAVA,
                org.bukkit.Particle.CLOUD
        };

        int[] slots = { 10, 11, 12, 13, 14, 15, 16 }; // Removed slot 22

        for (int i = 0; i < particles.length; i++) {
            if (i >= slots.length)
                break;
            org.bukkit.Particle p = particles[i];

            double cost = plugin.getConfigManager().getConfig().getDouble("economy.visual_effect_cost", 500.0);

            String displayName;
            String desc;
            Material icon;

            switch (p) {
                case VILLAGER_HAPPY:
                    displayName = "&a&lLime Crystal";
                    desc = "&7Green holographic crystal.";
                    icon = Material.LIME_STAINED_GLASS;
                    break;
                case FLAME:
                    displayName = "&6&lOrange Crystal";
                    desc = "&7Orange holographic crystal.";
                    icon = Material.ORANGE_STAINED_GLASS;
                    break;
                case HEART:
                    displayName = "&c&lRed Crystal";
                    desc = "&7Red holographic crystal.";
                    icon = Material.RED_STAINED_GLASS;
                    break;
                case NOTE:
                    displayName = "&b&lLight Blue Crystal";
                    desc = "&7Light blue holographic crystal.";
                    icon = Material.LIGHT_BLUE_STAINED_GLASS;
                    break;
                case SPELL_WITCH:
                    displayName = "&5&lPurple Crystal";
                    desc = "&7Purple holographic crystal.";
                    icon = Material.PURPLE_STAINED_GLASS;
                    break;
                case LAVA:
                    displayName = "&6&lMagma Crystal";
                    desc = "&7Magma holographic crystal.";
                    icon = Material.MAGMA_BLOCK;
                    break;
                case CLOUD:
                    displayName = "&f&lWhite Crystal";
                    desc = "&7White holographic crystal.";
                    icon = Material.WHITE_STAINED_GLASS;
                    break;
                default:
                    displayName = "&e" + p.name();
                    desc = "&7Standard crystal.";
                    icon = Material.GLASS;
            }

            // FORCE COLOR TRANSLATION HERE
            String finalName = ChatColor.translateAlternateColorCodes('&', displayName);
            List<String> rawLore = plugin.getConfigManager().getMessageList("gui.particle_item_lore");
            List<String> finalLore = new ArrayList<>();

            for (String line : rawLore) {
                String replaced = line.replace("%cost%", String.format("%,.0f", cost))
                        .replace("%desc%", desc);
                finalLore.add(ChatColor.translateAlternateColorCodes('&', replaced));
            }

            ItemStack item = createItem(icon, finalName, finalLore);
            inv.setItem(slots[i], item);
        }

        addBackButton(inv, 26);
        player.openInventory(inv);
    }

    /**
     * Opens the permissions management GUI for a specific member
     */
    public void openPermissionsMenu(Player player, ProtectionBlock protection, UUID memberId) {
        setEditingProtection(player, protection);

        String memberName = Bukkit.getOfflinePlayer(memberId).getName();
        if (memberName == null)
            memberName = "Unknown";

        Inventory inv = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + "Permissions: " + memberName);

        // Fill with glass
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        // Get or create member data
        MemberData memberData = protection.getMemberData(memberId);
        if (memberData == null) {
            memberData = new MemberData(memberId);
            memberData.applyRole("member");
            protection.setMemberPermissions(memberId, memberData);
        }

        // Permission icons (2 rows: icon on top, status below)
        displayPermissionIcon(inv, 10, MemberPermission.BUILD, memberData, Material.DIAMOND_PICKAXE);
        displayPermissionIcon(inv, 11, MemberPermission.PLACE_BLOCKS, memberData, Material.GRASS_BLOCK);
        displayPermissionIcon(inv, 12, MemberPermission.BREAK_BLOCKS, memberData, Material.IRON_PICKAXE);
        displayPermissionIcon(inv, 13, MemberPermission.INTERACT, memberData, Material.OAK_DOOR);
        displayPermissionIcon(inv, 14, MemberPermission.CONTAINER_ACCESS, memberData, Material.CHEST);
        displayPermissionIcon(inv, 15, MemberPermission.MANAGE_MEMBERS, memberData, Material.PLAYER_HEAD);
        displayPermissionIcon(inv, 16, MemberPermission.MANAGE_FLAGS, memberData, Material.WHITE_BANNER);

        displayPermissionIcon(inv, 19, MemberPermission.MANAGE_SETTINGS, memberData, Material.COMPARATOR);
        displayPermissionIcon(inv, 20, MemberPermission.TELEPORT, memberData, Material.ENDER_PEARL);
        displayPermissionIcon(inv, 21, MemberPermission.FULL_ACCESS, memberData, Material.NETHER_STAR);

        // Predefined roles header
        inv.setItem(31, createItem(Material.NAME_TAG, ChatColor.GOLD + "Quick Roles",
                Arrays.asList(ChatColor.GRAY + "Click a role to apply its permissions")));

        // Role buttons
        inv.setItem(37, createItem(Material.LEATHER_BOOTS, ChatColor.WHITE + "Visitor",
                Arrays.asList(ChatColor.GRAY + "Basic visitor access", "",
                        ChatColor.YELLOW + "• Interact", "", ChatColor.GREEN + "Click to apply!")));

        inv.setItem(38, createItem(Material.IRON_SWORD, ChatColor.GREEN + "Member",
                Arrays.asList(ChatColor.GRAY + "Standard member", "",
                        ChatColor.YELLOW + "• Build & Break", ChatColor.YELLOW + "• Interact",
                        ChatColor.YELLOW + "• Containers", "", ChatColor.GREEN + "Click to apply!")));

        inv.setItem(39, createItem(Material.GOLDEN_PICKAXE, ChatColor.YELLOW + "Builder",
                Arrays.asList(ChatColor.GRAY + "Extended building", "",
                        ChatColor.YELLOW + "• All Member perms", ChatColor.YELLOW + "• Place/Break control",
                        "", ChatColor.GREEN + "Click to apply!")));

        inv.setItem(40, createItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Moderator",
                Arrays.asList(ChatColor.GRAY + "Can manage members", "",
                        ChatColor.YELLOW + "• All Builder perms", ChatColor.YELLOW + "• Manage Members",
                        "", ChatColor.GREEN + "Click to apply!")));

        inv.setItem(41, createItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Co-Owner",
                Arrays.asList(ChatColor.GRAY + "Full access", "",
                        ChatColor.YELLOW + "• FULL ACCESS", "", ChatColor.GREEN + "Click to apply!")));

        // Current role display
        String currentRole = memberData.getRoleName() != null ? memberData.getRoleName() : "Custom";
        inv.setItem(4, createItem(Material.PAPER,
                ChatColor.AQUA + "Current Role: " + ChatColor.YELLOW + currentRole,
                Arrays.asList(ChatColor.GRAY + "Member: " + ChatColor.WHITE + memberName,
                        ChatColor.GRAY + "Active Permissions: " + ChatColor.GREEN
                                + memberData.getPermissions().size())));

        addBackButton(inv, 49);
        player.openInventory(inv);
    }

    /**
     * Displays a permission icon with toggle status
     */
    private void displayPermissionIcon(Inventory inv, int slot,
            MemberPermission permission, MemberData memberData, Material iconMaterial) {

        boolean hasPermission = memberData.hasPermission(permission);

        // Icon on top row
        List<String> iconLore = new ArrayList<>();
        iconLore.add(ChatColor.GRAY + permission.getDescription());
        iconLore.add("");
        iconLore.add(hasPermission ? ChatColor.GREEN + "✓ ENABLED" : ChatColor.RED + "✗ DISABLED");
        iconLore.add("");
        iconLore.add(ChatColor.YELLOW + "Click to toggle!");

        ItemStack icon = createItem(iconMaterial, ChatColor.AQUA + permission.getDisplayName(), iconLore);
        inv.setItem(slot, icon);

        // Status indicator on bottom row
        Material statusMaterial = hasPermission ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String statusText = hasPermission ? ChatColor.GREEN + "✓ ENABLED" : ChatColor.RED + "✗ DISABLED";

        ItemStack status = createItem(statusMaterial, statusText, Arrays.asList(ChatColor.YELLOW + "Click to toggle!"));
        inv.setItem(slot + 9, status);
    }

    // --- EVENTOS ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ProtectionBlock editingProtection = getEditingProtection(player);

        if (title.equals(getTitle("main_title"))) {
            event.setCancelled(true);
            Material type = event.getCurrentItem().getType();
            if (type == Material.ENDER_CHEST) {
                openPlayerProtections(player);
            } else if (type == Material.EMERALD) {
                openShop(player);
            } else if (type == Material.PAPER) {
                if (player.hasPermission("advancedprotection.admin")) {
                    openLanguageMenu(player);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("no_permission_language"));
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            } else if (type == Material.BOOK) {
                player.closeInventory();
                for (String line : plugin.getConfigManager().getMessageList("gui.guide_message")) {
                    player.sendMessage(line);
                }
            }
        } else if (title.equals(getTitle("language_title"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                openGlobalMenu(player);
            } else if (event.getSlot() == 11) { // ES
                plugin.getConfigManager().setLanguage("es");
                player.sendMessage(ChatColor.GREEN + "Idioma cambiado a Español.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.closeInventory();
            } else if (event.getSlot() == 15) { // EN
                plugin.getConfigManager().setLanguage("en");
                player.sendMessage(ChatColor.GREEN + "Language changed to English.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.closeInventory();
            }
        } else if (title.equals(getTitle("my_protections_title"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                openGlobalMenu(player);
            } else if (event.getCurrentItem().getType() != Material.BLACK_STAINED_GLASS_PANE) { // Allow clicking any
                                                                                                // protection item
                // Visualizar bordes desde la lista
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore()) {
                    List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                    for (String line : lore) {
                        if (line.contains("ID: ")) {
                            String idStr = ChatColor.stripColor(line).replace("ID: ", "").trim();
                            List<ProtectionBlock> protections = plugin.getRegionManager()
                                    .getPlayerProtections(player.getUniqueId());
                            for (ProtectionBlock p : protections) {
                                if (p.getId().toString().startsWith(idStr)) {
                                    player.closeInventory();
                                    plugin.getVisualEffectsManager().showBorders(player, p);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else if (title.equals(getTitle("shop_title"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                openGlobalMenu(player);
            } else if (event.getCurrentItem().getType() == Material.SUNFLOWER) {
                // Solo informativo, reproducir sonido
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            } else if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore()) {
                List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                boolean isShopItem = false;
                for (String line : lore) {
                    if (ChatColor.stripColor(line).startsWith("Nivel:")) {
                        isShopItem = true;
                        break;
                    }
                    // Fallback para inglés
                    if (ChatColor.stripColor(line).startsWith("Level:")) {
                        isShopItem = true;
                        break;
                    }
                }
                if (isShopItem) {
                    handleShopPurchase(player, event.getCurrentItem());
                }
            }
        } else if (title.equals(getTitle("visual_title"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (editingProtection != null)
                    openMainMenu(player, editingProtection);
                return;
            }

            if (editingProtection == null)
                return;

            Material type = event.getCurrentItem().getType();
            // Check if it's one of our crystal icons
            if (type == Material.LIME_STAINED_GLASS || type == Material.ORANGE_STAINED_GLASS
                    || type == Material.RED_STAINED_GLASS ||
                    type == Material.LIGHT_BLUE_STAINED_GLASS || type == Material.PURPLE_STAINED_GLASS
                    || type == Material.MAGMA_BLOCK ||
                    type == Material.WHITE_STAINED_GLASS || type == Material.GLASS) {

                String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                String particleName = null;

                // Map display names back to particle names (Updated for Crystals)
                if (displayName.contains("Lime Crystal"))
                    particleName = "VILLAGER_HAPPY";
                else if (displayName.contains("Orange Crystal"))
                    particleName = "FLAME";
                else if (displayName.contains("Red Crystal"))
                    particleName = "HEART";
                else if (displayName.contains("Light Blue Crystal"))
                    particleName = "NOTE";
                else if (displayName.contains("Purple Crystal"))
                    particleName = "SPELL_WITCH";
                else if (displayName.contains("Magma Crystal"))
                    particleName = "LAVA";
                else if (displayName.contains("White Crystal"))
                    particleName = "CLOUD";
                else if (displayName.contains("Witch Magic"))
                    particleName = "SPELL_WITCH";
                else if (displayName.contains("Molten Lava"))
                    particleName = "LAVA";
                else if (displayName.contains("Fluffy Cloud"))
                    particleName = "CLOUD";
                else if (displayName.contains("World Border"))
                    particleName = "WORLD_BORDER";
                else
                    particleName = displayName.trim(); // Fallback

                try {
                    double cost = plugin.getConfigManager().getConfig().getDouble("economy.visual_effect_cost", 500.0);

                    if (plugin.getEconomyHook().isEnabled()) {
                        if (!plugin.getEconomyHook().withdraw(player, cost)) {
                            player.sendMessage(plugin.getConfigManager().getMessage("visual_no_money")
                                    .replace("%cost%", String.format("%,.0f", cost)));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            return;
                        }
                        player.sendMessage(plugin.getConfigManager().getMessage("visual_bought")
                                .replace("%cost%", String.format("%,.0f", cost)));
                    }

                    editingProtection.setParticleType(particleName);
                    plugin.getProtectionService().saveProtection(editingProtection);

                    player.sendMessage(plugin.getConfigManager().getMessage("particle_changed").replace("%particle%",
                            displayName)); // Show pretty name

                    // Send Title
                    player.sendTitle(ChatColor.GREEN + "Effect Purchased!", ChatColor.YELLOW + displayName, 10, 70, 20);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                    // Instant Refresh: Stop current visualization (if any) and start new one
                    plugin.getVisualEffectsManager().stopVisualizing(player);
                    plugin.getVisualEffectsManager().showBorders(player, editingProtection);

                    openMainMenu(player, editingProtection);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error selecting particle.");
                    e.printStackTrace();
                }
            }
        } else if (title.equals(getTitle("flags_title"))
                || ChatColor.stripColor(title).equals(ChatColor.stripColor(getTitle("flags_title")))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (editingProtection != null) {
                    openMainMenu(player, editingProtection);
                }
            } else if (editingProtection != null) {
                handleFlagClick(player, editingProtection, event.getCurrentItem(), false);
            }
        } else if (title.equals(ChatColor.DARK_RED + "Admin: Flags")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (editingProtection != null) {
                    openAdminProtectionDetails(player, editingProtection);
                }
            } else if (editingProtection != null) {
                handleFlagClick(player, editingProtection, event.getCurrentItem(), true);
            }
        } else if (title.equals(getTitle("members_title"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (editingProtection != null) {
                    if (adminViewers.contains(player.getUniqueId())) {
                        openAdminProtectionDetails(player, editingProtection);
                    } else {
                        openMainMenu(player, editingProtection);
                    }
                }
            } else if (event.getSlot() == 4) { // Añadir Miembro
                if (editingProtection != null)
                    openAddMemberMenu(player, editingProtection);
            } else if (event.getCurrentItem().getType() == Material.PLAYER_HEAD && editingProtection != null) {
                // Click on member head
                String playerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                if (target != null) {
                    // Right click = Remove member
                    // Left click = Manage permissions
                    if (event.getClick() == org.bukkit.event.inventory.ClickType.RIGHT) {
                        // Remove member
                        editingProtection.removeMember(target.getUniqueId());
                        editingProtection.removeMemberPermissions(target.getUniqueId());
                        plugin.getProtectionService().saveProtection(editingProtection);

                        // Update hologram
                        if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                            plugin.getHologramManager().updateHologram(editingProtection);
                        }

                        player.sendMessage(ChatColor.GREEN + "✓ Removed member: " + ChatColor.WHITE + playerName);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                        openMembersMenu(player, editingProtection);
                    } else {
                        // Open permissions menu (left click)
                        openPermissionsMenu(player, editingProtection, target.getUniqueId());
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    }
                }
            }
        } else if (title.equals(getTitle("add_member_title"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (editingProtection != null)
                    openMembersMenu(player, editingProtection);
            } else if (event.getCurrentItem().getType() == Material.PLAYER_HEAD && editingProtection != null) {
                String playerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayer(playerName);
                if (target != null) {
                    editingProtection.addMember(target.getUniqueId());
                    plugin.getProtectionService().saveProtection(editingProtection); // Guardar

                    // Actualizar holograma
                    if (plugin.getHologramManager() != null && plugin.getHologramManager().isEnabled()) {
                        plugin.getHologramManager().updateHologram(editingProtection);
                    }

                    player.sendMessage(
                            plugin.getConfigManager().getMessage("member_added").replace("%player%", playerName));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    openMembersMenu(player, editingProtection);
                }
            }
        } else if (title
                .startsWith(plugin.getConfigManager().getMessage("gui.protection_title").replace("%id%", "").trim())) {
            event.setCancelled(true);
            if (event.getSlot() == 13) { // Info (Center Top)
                player.sendMessage(plugin.getConfigManager().getMessage("gui.expiration_msg"));
                player.closeInventory();
            } else if (event.getSlot() == 11) { // Members (Left Top)
                if (editingProtection != null)
                    openMembersMenu(player, editingProtection);
            } else if (event.getSlot() == 15) { // Flags (Right Top)
                if (editingProtection != null)
                    openFlagsMenu(player, editingProtection, false);
            } else if (event.getSlot() == 20) { // Visualize (Left Bottom)
                if (editingProtection != null) {
                    player.closeInventory();
                    plugin.getVisualEffectsManager().showBorders(player, editingProtection);
                }
            } else if (event.getSlot() == 24) { // Visual Settings (Right Bottom)
                if (editingProtection != null) {
                    openVisualMenu(player, editingProtection);
                }
            } else if (event.getSlot() == 22) { // Hologram Toggle (Center Bottom)
                if (editingProtection != null) {
                    boolean newState = !editingProtection.isHologramEnabled();
                    editingProtection.setHologramEnabled(newState);
                    plugin.getProtectionService().saveProtection(editingProtection);

                    // Control HologramManager
                    if (plugin.getHologramManager() != null) {
                        if (newState) {
                            // Activar - crear holograma
                            plugin.getHologramManager().createHologram(editingProtection);
                        } else {
                            // Desactivar - eliminar holograma
                            plugin.getHologramManager().removeHologram(editingProtection.getId());
                        }
                    }

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    openMainMenu(player, editingProtection); // Refresh menu
                }
            } else if (event.getSlot() == 40) { // Back Button
                openPlayerProtections(player);
            }
        } else if (title.startsWith(ChatColor.DARK_PURPLE + "Permissions: ")) {
            event.setCancelled(true);

            if (editingProtection == null) {
                player.closeInventory();
                return;
            }

            // Extract member name from title
            String memberName = title.replace(ChatColor.DARK_PURPLE + "Permissions: ", "");
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(memberName);

            if (target == null) {
                player.closeInventory();
                return;
            }

            UUID memberId = target.getUniqueId();
            MemberData memberData = editingProtection.getMemberData(memberId);

            if (memberData == null) {
                memberData = new MemberData(memberId);
                editingProtection.setMemberPermissions(memberId, memberData);
            }

            // Back button
            if (event.getSlot() == 49) {
                openMembersMenu(player, editingProtection);
                return;
            }

            // Permission toggles (slots 10-21 for icons and 19-30 for status indicators)
            int slot = event.getSlot();
            MemberPermission toggledPerm = null;

            // Map slots to permissions (both icon row and status row)
            if (slot == 10 || slot == 19)
                toggledPerm = MemberPermission.BUILD;
            else if (slot == 11 || slot == 20)
                toggledPerm = MemberPermission.PLACE_BLOCKS;
            else if (slot == 12 || slot == 21)
                toggledPerm = MemberPermission.BREAK_BLOCKS;
            else if (slot == 13 || slot == 22)
                toggledPerm = MemberPermission.INTERACT;
            else if (slot == 14 || slot == 23)
                toggledPerm = MemberPermission.CONTAINER_ACCESS;
            else if (slot == 15 || slot == 24)
                toggledPerm = MemberPermission.MANAGE_MEMBERS;
            else if (slot == 16 || slot == 25)
                toggledPerm = MemberPermission.MANAGE_FLAGS;
            else if (slot == 19 || slot == 28)
                toggledPerm = MemberPermission.MANAGE_SETTINGS;
            else if (slot == 20 || slot == 29)
                toggledPerm = MemberPermission.TELEPORT;
            else if (slot == 21 || slot == 30)
                toggledPerm = MemberPermission.FULL_ACCESS;

            if (toggledPerm != null) {
                // Toggle permission
                if (memberData.hasPermission(toggledPerm)) {
                    memberData.removePermission(toggledPerm);
                    player.sendMessage(ChatColor.RED + "✗ Disabled: " + ChatColor.WHITE + toggledPerm.getDisplayName());
                } else {
                    memberData.addPermission(toggledPerm);
                    player.sendMessage(
                            ChatColor.GREEN + "✓ Enabled: " + ChatColor.WHITE + toggledPerm.getDisplayName());
                }

                memberData.setRoleName("Custom"); // Mark as custom when manually edited
                plugin.getProtectionService().saveProtection(editingProtection);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openPermissionsMenu(player, editingProtection, memberId); // Refresh
                return;
            }

            // Role buttons (slots 37-41)
            String roleName = null;
            if (slot == 37)
                roleName = "visitor";
            else if (slot == 38)
                roleName = "member";
            else if (slot == 39)
                roleName = "builder";
            else if (slot == 40)
                roleName = "moderator";
            else if (slot == 41)
                roleName = "co-owner";

            if (roleName != null) {
                memberData.applyRole(roleName);
                plugin.getProtectionService().saveProtection(editingProtection);
                player.sendMessage(ChatColor.GREEN + "✓ Applied role: " + ChatColor.YELLOW + roleName.toUpperCase());
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                openPermissionsMenu(player, editingProtection, memberId); // Refresh
            }
        } else if (title.startsWith(ChatColor.DARK_RED + "Admin Manager")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null)
                return;

            if (event.getSlot() == 49) {
                // Back - Salir al menú global (o cerrar si se prefiere, pero global es seguro)
                openGlobalMenu(player);
                return;
            }

            if (event.getCurrentItem().getType() == Material.ARROW) {
                // Pagination
                String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                int currentPage = Integer.parseInt(title.replace("Admin Manager (P.", "").replace(")", "").trim()) - 1;

                if (name.contains("Next")) {
                    openAdminManager(player, currentPage + 1);
                } else if (name.contains("Previous")) {
                    openAdminManager(player, currentPage - 1);
                }
            } else {
                // Click on protection item
                UUID id = getProtectionIdFromLore(event.getCurrentItem());
                if (id != null) {
                    ProtectionBlock p = plugin.getRegionManager().getProtection(id);
                    if (p != null) {
                        openAdminProtectionDetails(player, p);
                    } else {
                        player.sendMessage(ChatColor.RED + "Protection not found (maybe deleted?).");
                        openAdminManager(player, 0);
                    }
                }
            }
        } else if (title.startsWith(ChatColor.DARK_RED + "Admin: ")) {
            event.setCancelled(true);
            if (editingProtection == null) {
                player.closeInventory();
                return;
            }

            if (event.getSlot() == 22) { // Back
                openAdminManager(player, 0);
            } else if (event.getSlot() == 10) { // Teleport
                player.teleport(editingProtection.getCenter().clone().add(0.5, 1, 0.5));
                player.sendMessage(ChatColor.GREEN + "Teleported to protection center.");
                player.closeInventory();
            } else if (event.getSlot() == 16) { // Delete
                plugin.getProtectionService().deleteProtection(editingProtection.getId());
                plugin.getVisualEffectsManager().stopVisualizing(player); // Stop visuals if active
                player.sendMessage(ChatColor.RED + "Protection deleted by admin.");
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
                openAdminManager(player, 0);
            } else if (event.getSlot() == 12) { // Members
                openMembersMenu(player, editingProtection);
            } else if (event.getSlot() == 14) { // Flags
                openFlagsMenu(player, editingProtection, true);
            }
        }
    }

    private void handleShopPurchase(Player player, ItemStack item) {
        if (!plugin.getEconomyHook().isEnabled()) {
            player.sendMessage(plugin.getConfigManager().getMessage("economy_disabled"));
            return;
        }

        try {
            List<String> lore = item.getItemMeta().getLore();
            int level = -1;
            for (String line : lore) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("Level:") || stripped.startsWith("Nivel:")) {
                    String levelStr = stripped.replace("Nivel:", "").replace("Level:", "").trim();
                    try {
                        level = Integer.parseInt(levelStr);
                        break;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (level == -1) {
                player.sendMessage(ChatColor.RED + "Error: Could not detect protection level from item.");
                return;
            }

            double cost = plugin.getConfigManager().getProtectionCost(level);
            Material material = plugin.getConfigManager().getProtectionMaterial(level);
            String name = plugin.getConfigManager().getProtectionDisplayName(level);
            ChatColor color = getLevelColor(level);

            if (plugin.getEconomyHook().getBalance(player) >= cost) {
                plugin.getEconomyHook().withdraw(player, cost);

                String date = java.time.LocalDate.now().toString();

                ItemStack protectionBlock = createItem(material,
                        plugin.getConfigManager().getMessage("gui.protection_block_name")
                                .replace("%color%", color.toString())
                                .replace("%name%", name.toUpperCase()),
                        replacePlaceholders(plugin.getConfigManager().getMessageList("gui.protection_block_lore"),
                                "%player%", player.getName(),
                                "%cost%", String.format("%,.0f", cost),
                                "%date%", date,
                                "%color%", color.toString(),
                                "%radius%", String.valueOf(plugin.getConfigManager().getProtectionRadius(level)),
                                "%level%", String.valueOf(level)));

                player.getInventory().addItem(protectionBlock);

                player.sendTitle(ChatColor.GREEN + "¡Compra Exitosa!",
                        plugin.getConfigManager().getMessage("protection_bought").replace("%name%", name), 10, 70, 20);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.closeInventory();
            } else {
                player.sendTitle(ChatColor.RED + "¡Dinero Insuficiente!",
                        plugin.getConfigManager().getMessage("insufficient_funds").replace("%cost%",
                                String.format("%,.0f", cost)),
                        10, 70,
                        20);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        } catch (Exception e) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop_error"));
            e.printStackTrace();
        }
    }

    // --- UTILIDADES ---

    private void fillBorders(Inventory inv) {
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler);
            }
        }
    }

    private void addBackButton(Inventory inv, int slot) {
        ItemStack back = createItem(Material.ARROW, plugin.getConfigManager().getMessage("gui.back_name"),
                plugin.getConfigManager().getMessageList("gui.back_lore"));
        inv.setItem(slot, back);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, Arrays.asList(lore));
    }

    private void handleFlagClick(Player player, ProtectionBlock protection, ItemStack item, boolean asAdmin) {
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        String normalizedName = displayName.toUpperCase().replace(" ", "_");

        // Debug
        // plugin.getLogger().info("Click Flag: " + displayName + " -> Normalized: " +
        // normalizedName);

        ProtectionFlag selectedFlag = null;

        // 1. Direct Match
        try {
            selectedFlag = ProtectionFlag.valueOf(normalizedName);
        } catch (IllegalArgumentException e) {
            // 2. Fuzzy Match (Contains)
            for (ProtectionFlag flag : ProtectionFlag.values()) {
                if (normalizedName.contains(flag.name())) {
                    selectedFlag = flag;
                    break;
                }
            }
        }

        if (selectedFlag != null) {
            if (protection.hasFlag(selectedFlag)) {
                protection.removeFlag(selectedFlag);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.5f);
                player.sendMessage(plugin.getConfigManager().getMessage("flag_changed")
                        .replace("%flag%", selectedFlag.name())
                        .replace("%status%", plugin.getConfigManager().getMessage("gui.flag_disabled")));
            } else {
                protection.addFlag(selectedFlag);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
                player.sendMessage(plugin.getConfigManager().getMessage("flag_changed")
                        .replace("%flag%", selectedFlag.name())
                        .replace("%status%", plugin.getConfigManager().getMessage("gui.flag_enabled")));
            }
            plugin.getProtectionService().saveProtection(protection);
            openFlagsMenu(player, protection, asAdmin);
        } else {
            player.sendMessage(ChatColor.RED + "Error: Could not identify flag from item name: " + displayName);
            plugin.getLogger()
                    .warning("Flag match failed for: " + displayName + " (Normalized: " + normalizedName + ")");
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> replacePlaceholders(List<String> list, String... replacements) {
        List<String> replaced = new ArrayList<>();
        for (String line : list) {
            String temp = line;
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    temp = temp.replace(replacements[i], replacements[i + 1]);
                }
            }
            replaced.add(temp);
        }
        return replaced;
    }

    private void setEditingProtection(Player player, ProtectionBlock protection) {
        editingSessions.put(player.getUniqueId(), protection);
    }

    private ProtectionBlock getEditingProtection(Player player) {
        return editingSessions.get(player.getUniqueId());
    }

    // --- ADMIN MANAGER ---

    public void openAdminManager(Player player, int page) {
        List<ProtectionBlock> allProtections = plugin.getRegionManager().getAllProtections();
        int totalProtections = allProtections.size();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) totalProtections / itemsPerPage);

        if (page < 0)
            page = 0;
        if (page >= totalPages && totalPages > 0)
            page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Admin Manager (P." + (page + 1) + ")");

        // Fill bottom bar
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalProtections);

        for (int i = startIndex; i < endIndex; i++) {
            ProtectionBlock p = allProtections.get(i);
            String ownerName = Bukkit.getOfflinePlayer(p.getOwner()).getName();
            if (ownerName == null)
                ownerName = "Unknown";

            String locStr = String.format("%d, %d, %d (%s)",
                    p.getCenter().getBlockX(), p.getCenter().getBlockY(), p.getCenter().getBlockZ(),
                    p.getCenter().getWorld().getName());

            ItemStack item = createItem(plugin.getConfigManager().getProtectionMaterial(p.getLevel()),
                    ChatColor.GOLD + "ID: " + p.getId().toString().substring(0, 8),
                    ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + ownerName,
                    ChatColor.GRAY + "Level: " + ChatColor.WHITE + p.getLevel(),
                    ChatColor.GRAY + "Location: " + ChatColor.AQUA + locStr,
                    "",
                    ChatColor.GREEN + "Click to manage");

            // Hidden ID for click handling
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(ChatColor.BLACK + "FULLID:" + p.getId().toString());
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(i - startIndex, item);
        }

        // Navigation Buttons
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, ChatColor.YELLOW + "Previous Page",
                    ChatColor.GRAY + "Go to page " + page));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, createItem(Material.ARROW, ChatColor.YELLOW + "Next Page",
                    ChatColor.GRAY + "Go to page " + (page + 2)));
        }

        addBackButton(inv, 49);
        player.openInventory(inv);
    }

    public void openAdminProtectionDetails(Player player, ProtectionBlock protection) {
        adminViewers.add(player.getUniqueId());
        setEditingProtection(player, protection); // Set session
        Inventory inv = Bukkit.createInventory(null, 27,
                ChatColor.DARK_RED + "Admin: " + protection.getId().toString().substring(0, 8));
        fillBorders(inv);

        String ownerName = Bukkit.getOfflinePlayer(protection.getOwner()).getName();

        // Info Item
        ItemStack info = createItem(Material.PAPER, ChatColor.GOLD + "Info",
                ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + ownerName,
                ChatColor.GRAY + "Level: " + ChatColor.WHITE + protection.getLevel(),
                ChatColor.GRAY + "Location: " + ChatColor.AQUA + protection.getCenter().getBlockX() + ", "
                        + protection.getCenter().getBlockY() + ", " + protection.getCenter().getBlockZ());
        inv.setItem(13, info);

        // Teleport
        ItemStack tp = createItem(Material.ENDER_PEARL, ChatColor.AQUA + "Teleport",
                ChatColor.GRAY + "Teleport to center.");
        inv.setItem(10, tp);

        // Delete
        ItemStack delete = createItem(Material.TNT, ChatColor.RED + "DELETE PROTECTION",
                ChatColor.GRAY + "Irreversible action!");
        inv.setItem(16, delete);

        // Members (Reuse existing menu logic via click)
        ItemStack members = createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Manage Members",
                ChatColor.GRAY + "Edit members list.");
        inv.setItem(12, members);

        // Flags (Reuse existing menu logic via click)
        ItemStack flags = createItem(Material.REDSTONE_TORCH, ChatColor.LIGHT_PURPLE + "Manage Flags",
                ChatColor.GRAY + "Edit protection flags.");
        inv.setItem(14, flags);

        addBackButton(inv, 22);
        player.openInventory(inv);
    }

    // --- EVENTOS ---
    // (Updated onInventoryClick logic needs to be inserted carefully,
    // but since I can't easily inject into the middle of the huge method above,
    // I will append the logic here and rely on the user to manually merge or I will
    // rewrite the event handler if needed.
    // Wait, I am replacing the END of the file. I need to make sure I don't break
    // the class closing brace.
    // The previous tool call showed the end of the file.

    // Helper to extract ID from lore
    private UUID getProtectionIdFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("FULLID:")) {
                try {
                    return UUID.fromString(ChatColor.stripColor(line).replace("FULLID:", "").trim());
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private ChatColor getLevelColor(int level) {
        switch (level) {
            case 1:
                return ChatColor.GRAY; // Carbon
            case 2:
                return ChatColor.WHITE; // Hierro
            case 3:
                return ChatColor.GOLD; // Oro
            case 4:
                return ChatColor.AQUA; // Diamante
            case 5:
                return ChatColor.GREEN; // Esmeralda
            case 6:
                return ChatColor.DARK_PURPLE; // Obsidiana
            default:
                return ChatColor.YELLOW;
        }
    }
}
