package com.crystalverse.advancedprotection.model;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProtectionBlock {
    private final UUID id;
    private UUID owner;
    private Location center;
    private int level;
    private Set<UUID> members = new HashSet<>();
    private Set<ProtectionFlag> flags = new HashSet<>();
    private String customName; // Nombre personalizado de la protección

    // Sistema de permisos granulares
    private Map<UUID, MemberData> memberPermissions = new HashMap<>();

    // Sistema de impuestos/renta
    private long createdTimestamp;
    private long lastTaxPaid;
    private long nextTaxDue;
    private boolean rentEnabled = false;
    private double rentAmount = 0.0;

    public ProtectionBlock(UUID id, UUID owner, Location center, int level) {
        this.id = id;
        this.owner = owner;
        this.center = center;
        this.level = level;
        this.members = new HashSet<>();
        this.flags = new HashSet<>();
        this.customName = null;
        this.memberPermissions = new HashMap<>();
        this.createdTimestamp = System.currentTimeMillis();
        this.lastTaxPaid = this.createdTimestamp;
        this.nextTaxDue = 0; // Se calculará después
    }

    // Constructor vacío para Gson
    public ProtectionBlock() {
        this.id = UUID.randomUUID();
        this.members = new HashSet<>();
        this.flags = new HashSet<>();
        this.customName = null;
        this.memberPermissions = new HashMap<>();
        this.createdTimestamp = System.currentTimeMillis();
        this.lastTaxPaid = this.createdTimestamp;
        this.nextTaxDue = 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getCenter() {
        return center;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID member) {
        members.add(member);
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public Set<ProtectionFlag> getFlags() {
        return flags;
    }

    public void addFlag(ProtectionFlag flag) {
        flags.add(flag);
    }

    public void removeFlag(ProtectionFlag flag) {
        flags.remove(flag);
    }

    public boolean hasFlag(ProtectionFlag flag) {
        return flags.contains(flag);
    }

    private String particleType = "VILLAGER_HAPPY";

    public String getParticleType() {
        return particleType;
    }

    public void setParticleType(String particleType) {
        this.particleType = particleType;
    }

    private boolean hologramEnabled = true;

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public void setHologramEnabled(boolean hologramEnabled) {
        this.hologramEnabled = hologramEnabled;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    // ========== SISTEMA DE PERMISOS GRANULARES ==========

    public MemberData getMemberData(UUID memberId) {
        return memberPermissions.get(memberId);
    }

    public void setMemberPermissions(UUID memberId, MemberData data) {
        memberPermissions.put(memberId, data);
    }

    public void removeMemberPermissions(UUID memberId) {
        memberPermissions.remove(memberId);
    }

    public Map<UUID, MemberData> getAllMemberPermissions() {
        return new HashMap<>(memberPermissions);
    }

    public boolean hasMemberPermission(UUID memberId, MemberPermission permission) {
        // Owner siempre tiene todos los permisos
        if (owner.equals(memberId)) {
            return true;
        }

        MemberData data = memberPermissions.get(memberId);
        if (data == null) {
            return false;
        }

        return data.hasPermission(permission);
    }

    // ========== SISTEMA DE IMPUESTOS/RENTA ==========

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long timestamp) {
        this.createdTimestamp = timestamp;
    }

    public long getLastTaxPaid() {
        return lastTaxPaid;
    }

    public void setLastTaxPaid(long timestamp) {
        this.lastTaxPaid = timestamp;
    }

    public long getNextTaxDue() {
        return nextTaxDue;
    }

    public void setNextTaxDue(long timestamp) {
        this.nextTaxDue = timestamp;
    }

    public boolean isRentEnabled() {
        return rentEnabled;
    }

    public void setRentEnabled(boolean enabled) {
        this.rentEnabled = enabled;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double amount) {
        this.rentAmount = amount;
    }

    public boolean isTaxOverdue() {
        if (!rentEnabled || nextTaxDue == 0) {
            return false;
        }
        return System.currentTimeMillis() > nextTaxDue;
    }

    public long getTimeUntilTaxDue() {
        if (nextTaxDue == 0) {
            return Long.MAX_VALUE;
        }
        return nextTaxDue - System.currentTimeMillis();
    }
}
