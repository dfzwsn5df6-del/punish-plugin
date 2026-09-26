package de.yourserver.punish;

import java.util.UUID;

public class PunishmentRecord {
    private int id;
    private UUID uuid;
    private String playerName;
    private String ip;
    private String adminName;
    private PunishmentType type;
    private String reason;
    private long createdAt;
    private long expiresAt;
    private boolean active;
    private boolean resolved;

    public PunishmentRecord() {}

    public PunishmentRecord(int id, UUID uuid, String playerName, String ip, String adminName,
                           PunishmentType type, String reason, long createdAt, long expiresAt,
                           boolean active, boolean resolved) {
        this.id = id;
        this.uuid = uuid;
        this.playerName = playerName;
        this.ip = ip;
        this.adminName = adminName;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.active = active;
        this.resolved = resolved;
    }

    public int getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public String getIp() { return ip; }
    public String getAdminName() { return adminName; }
    public PunishmentType getType() { return type; }
    public String getReason() { return reason; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public boolean isResolved() { return resolved; }

    public boolean isExpired() {
        if (type.isPermanentBan()) return false;
        if (expiresAt <= 0) return false;
        return System.currentTimeMillis() > expiresAt;
    }

    public String getFormattedText() {
        String status = active ? "AKTIV" : (resolved ? "ERLEDIGT" : "INAKTIV");
        String suffix = isExpired() ? " §m[ABGELAUFEN]" : "";
        return type.getDisplayName() + " | " + reason + " | " + status + suffix;
    }
}
