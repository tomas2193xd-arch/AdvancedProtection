package com.crystalverse.advancedprotection.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ProtectionLog {

    public enum LogType {
        BLOCK_BREAK_DENIED("Block break attempt"),
        BLOCK_PLACE_DENIED("Block place attempt"),
        INTERACT_DENIED("Interaction attempt"),
        PVP_DENIED("PvP attempt"),
        MEMBER_ADDED("Member added"),
        MEMBER_REMOVED("Member removed"),
        FLAG_CHANGED("Flag modified"),
        PROTECTION_CREATED("Protection created"),
        PROTECTION_DELETED("Protection deleted"),
        PROTECTION_UPGRADED("Protection upgraded"),
        PLAYER_ENTERED("Player entered zone"),
        PLAYER_EXITED("Player exited zone");

        private final String description;

        LogType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final UUID protectionId;
    private final LogType type;
    private final UUID player;
    private final String playerName;
    private final LocalDateTime timestamp;
    private final String details;

    public ProtectionLog(UUID protectionId, LogType type, UUID player, String playerName, String details) {
        this.protectionId = protectionId;
        this.type = type;
        this.player = player;
        this.playerName = playerName;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public UUID getProtectionId() {
        return protectionId;
    }

    public LogType getType() {
        return type;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return playerName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return timestamp.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s): %s",
                getFormattedTimestamp(),
                type.getDescription(),
                playerName,
                player.toString().substring(0, 8),
                details != null ? details : "");
    }
}
