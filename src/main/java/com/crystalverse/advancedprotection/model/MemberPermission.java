package com.crystalverse.advancedprotection.model;

/**
 * Granular permissions that can be assigned to individual members
 */
public enum MemberPermission {
    // Build permissions
    BUILD("Build & Break", "Allows building and breaking blocks"),
    PLACE_BLOCKS("Place Only", "Only allows placing blocks"),
    BREAK_BLOCKS("Break Only", "Only allows breaking blocks"),

    // Interaction permissions
    INTERACT("Interact", "Allows using doors, buttons, levers"),
    CONTAINER_ACCESS("Use Chests", "Allows opening chests, furnaces, etc"),

    // Management permissions
    MANAGE_MEMBERS("Manage Members", "Can add/remove members"),
    MANAGE_FLAGS("Change Flags", "Can modify protection flags"),
    MANAGE_SETTINGS("Settings", "Can rename and change settings"),

    // Special permissions
    TELEPORT("Teleport", "Can use /ap tp to this protection"),
    FULL_ACCESS("Full Access", "All permissions (Co-Owner)");

    private final String displayName;
    private final String description;

    MemberPermission(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this permission implies another permission
     * For example, FULL_ACCESS implies all others
     */
    public boolean implies(MemberPermission other) {
        if (this == FULL_ACCESS) {
            return true; // Full access implies everything
        }

        if (this == BUILD) {
            // BUILD implies PLACE_BLOCKS and BREAK_BLOCKS
            return other == PLACE_BLOCKS || other == BREAK_BLOCKS;
        }

        return this == other;
    }
}
