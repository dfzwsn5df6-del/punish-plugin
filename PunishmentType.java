package de.yourserver.punish;

public enum PunishmentType {
    AUSZEIT("Auszeit"),
    HAUSVERBOT("Hausverbot"),
    CHEATING("Cheating"),
    HACKING("Hacking"),
    SICHERHEITSBAN("Sicherheitsban"),
    CHAT_VERHALTEN("Chat Verhalten"),
    CHAT_BAN("Chat Ban"),
    VERWARNUNG("Verwarnung");

    private final String displayName;

    PunishmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PunishmentType fromString(String value) {
        if (value == null) return null;

        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        for (PunishmentType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        return null;
    }

    public boolean isPermanentBan() {
        return this == HAUSVERBOT || this == HACKING || this == SICHERHEITSBAN;
    }

    public boolean isTemporaryBan() {
        return this == AUSZEIT || this == CHEATING;
    }

    public boolean isIpBan() {
        return this == SICHERHEITSBAN;
    }

    public boolean isChatPunishment() {
        return this == CHAT_VERHALTEN || this == CHAT_BAN || this == VERWARNUNG;
    }
}
