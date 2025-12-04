package com.crystalverse.advancedprotection.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Representa los permisos de un miembro específico en una protección
 */
public class MemberData {
    private final UUID memberId;
    private final Set<MemberPermission> permissions;
    private String roleName; // Nombre personalizado del rol (opcional)
    private long addedTimestamp; // Cuando fue agregado

    public MemberData(UUID memberId) {
        this.memberId = memberId;
        this.permissions = EnumSet.noneOf(MemberPermission.class);
        this.addedTimestamp = System.currentTimeMillis();
    }

    public MemberData(UUID memberId, Set<MemberPermission> permissions) {
        this.memberId = memberId;
        this.permissions = EnumSet.copyOf(permissions);
        this.addedTimestamp = System.currentTimeMillis();
    }

    public UUID getMemberId() {
        return memberId;
    }

    public Set<MemberPermission> getPermissions() {
        return EnumSet.copyOf(permissions);
    }

    public void addPermission(MemberPermission permission) {
        permissions.add(permission);
    }

    public void removePermission(MemberPermission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(MemberPermission permission) {
        // Verificar permisos directos o implicados
        for (MemberPermission p : permissions) {
            if (p.implies(permission)) {
                return true;
            }
        }
        return false;
    }

    public void clearPermissions() {
        permissions.clear();
    }

    public void setPermissions(Set<MemberPermission> newPermissions) {
        permissions.clear();
        permissions.addAll(newPermissions);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getAddedTimestamp() {
        return addedTimestamp;
    }

    public void setAddedTimestamp(long timestamp) {
        this.addedTimestamp = timestamp;
    }

    /**
     * Aplica un rol predefinido
     */
    public void applyRole(String role) {
        permissions.clear();
        this.roleName = role;

        switch (role.toLowerCase()) {
            case "visitor":
                permissions.add(MemberPermission.INTERACT);
                break;

            case "builder":
                permissions.add(MemberPermission.BUILD);
                permissions.add(MemberPermission.INTERACT);
                permissions.add(MemberPermission.CONTAINER_ACCESS);
                break;

            case "moderator":
                permissions.add(MemberPermission.BUILD);
                permissions.add(MemberPermission.INTERACT);
                permissions.add(MemberPermission.CONTAINER_ACCESS);
                permissions.add(MemberPermission.MANAGE_MEMBERS);
                break;

            case "co-owner":
                permissions.add(MemberPermission.FULL_ACCESS);
                break;

            default:
                // Member básico
                permissions.add(MemberPermission.BUILD);
                permissions.add(MemberPermission.INTERACT);
                break;
        }
    }

    /**
     * Serializa los permisos a String para guardar en DB
     */
    public String serializePermissions() {
        if (permissions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (MemberPermission p : permissions) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(p.name());
        }
        return sb.toString();
    }

    /**
     * Deserializa permisos desde String de DB
     */
    public static Set<MemberPermission> deserializePermissions(String serialized) {
        Set<MemberPermission> perms = EnumSet.noneOf(MemberPermission.class);
        if (serialized == null || serialized.isEmpty()) {
            return perms;
        }

        String[] parts = serialized.split(",");
        for (String part : parts) {
            try {
                perms.add(MemberPermission.valueOf(part.trim()));
            } catch (IllegalArgumentException e) {
                // Ignorar permisos inválidos
            }
        }
        return perms;
    }
}
