package de.yourserver.punish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    private final PunishPlugin plugin;
    private final Map<UUID, Boolean> frozenPlayers = new ConcurrentHashMap<>();

    public DataManager(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS punishments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    ip TEXT,
                    admin_name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    created_at LONG NOT NULL,
                    expires_at LONG,
                    active INTEGER NOT NULL DEFAULT 1,
                    resolved INTEGER NOT NULL DEFAULT 0
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ip_history (
                    uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    ip TEXT NOT NULL,
                    last_seen LONG NOT NULL
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS frozen_players (
                    uuid TEXT PRIMARY KEY,
                    frozen INTEGER NOT NULL DEFAULT 1
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/punish.db");
    }

    public void saveIpHistory(Player player) {
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "0.0.0.0";
        String uuid = player.getUniqueId().toString();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                INSERT OR REPLACE INTO ip_history(uuid, player_name, ip, last_seen)
                VALUES (?, ?, ?, ?)
            """);
            ps.setString(1, uuid);
            ps.setString(2, player.getName());
            ps.setString(3, ip);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setFrozen(UUID uuid, boolean frozen) {
        frozenPlayers.put(uuid, frozen);

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                INSERT OR REPLACE INTO frozen_players(uuid, frozen)
                VALUES (?, ?)
            """);
            ps.setString(1, uuid.toString());
            ps.setInt(2, frozen ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isFrozen(UUID uuid) {
        Boolean val = frozenPlayers.get(uuid);
        if (val != null) return val;

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT frozen FROM frozen_players WHERE uuid = ?
            """);
            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean frozen = rs.getInt("frozen") == 1;
                frozenPlayers.put(uuid, frozen);
                return frozen;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void addPunishment(UUID uuid, String playerName, String ip, String adminName,
                              PunishmentType type, String reason) {
        long now = System.currentTimeMillis();
        long expiresAt = computeExpiresAt(type);

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO punishments(uuid, player_name, ip, admin_name, type, reason, created_at, expires_at, active, resolved)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, 0)
            """);
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, ip);
            ps.setString(4, adminName);
            ps.setString(5, type.name());
            ps.setString(6, reason);
            ps.setLong(7, now);
            ps.setLong(8, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long computeExpiresAt(PunishmentType type) {
        long now = System.currentTimeMillis();

        if (type == PunishmentType.AUSZEIT) {
            return now + (1000L * 60L * 15L);
        }

        if (type == PunishmentType.CHEATING) {
            return now + (1000L * 60L * 60L * 24L * 60L); // 2 Monate ~ 60 Tage
        }

        if (type.isPermanentBan()) {
            return -1L;
        }

        return now;
    }

    public List<PunishmentRecord> getPunishmentsForPlayer(UUID uuid) {
        List<PunishmentRecord> list = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT * FROM punishments WHERE uuid = ? ORDER BY created_at DESC
            """);
            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new PunishmentRecord(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("ip"),
                        rs.getString("admin_name"),
                        PunishmentType.valueOf(rs.getString("type")),
                        rs.getString("reason"),
                        rs.getLong("created_at"),
                        rs.getLong("expires_at"),
                        rs.getInt("active") == 1,
                        rs.getInt("resolved") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<String> getAllPunishmentLines(UUID uuid) {
        List<String> output = new ArrayList<>();
        List<PunishmentRecord> records = getPunishmentsForPlayer(uuid);

        if (records.isEmpty()) {
            output.add("§cKeine Bestrafungen gefunden.");
            return output;
        }

        for (PunishmentRecord record : records) {
            String line = "§7[" + record.getType().getDisplayName() + "] §f" + record.getReason() + " §8von " + record.getAdminName();

            boolean expired = record.isExpired();
            boolean active = record.isActive();

            if (expired || record.isResolved()) {
                output.add("§m" + line + " §7(Abgesessen / erledigt)");
            } else if (active) {
                output.add("§a" + line + " §7(Aktiv)");
            } else {
                output.add("§8" + line + " §7(Inaktiv)");
            }
        }

        return output;
    }

    public List<String> findAltsByIp(String ip) {
        List<String> result = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT DISTINCT player_name FROM ip_history WHERE ip = ?
            """);
            ps.setString(1, ip);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("player_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean hasActiveBan(UUID uuid) {
        List<PunishmentRecord> list = getPunishmentsForPlayer(uuid);

        for (PunishmentRecord record : list) {
            if (!record.isActive()) continue;

            if (record.getType().isPermanentBan()) return true;
            if (record.getType().isTemporaryBan() && record.getExpiresAt() > System.currentTimeMillis()) return true;
        }

        return false;
    }

    public String getActiveBanReason(UUID uuid) {
        List<PunishmentRecord> list = getPunishmentsForPlayer(uuid);

        for (PunishmentRecord record : list) {
            if (!record.isActive()) continue;

            if (record.getType().isPermanentBan()) {
                return record.getType().getDisplayName() + " | " + record.getReason();
            }

            if (record.getType().isTemporaryBan() && record.getExpiresAt() > System.currentTimeMillis()) {
                return record.getType().getDisplayName() + " | " + record.getReason();
            }
        }

        return null;
    }

    public void unbanPlayer(UUID uuid) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                UPDATE punishments SET active = 0, resolved = 1 WHERE uuid = ? AND active = 1
            """);
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unbanPlayerByName(String name) {
        String targetUUID = Bukkit.getOfflinePlayer(name).getUniqueId().toString();
        unbanPlayer(UUID.fromString(targetUUID));
    }

    public void removeExpiredPunishments() {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                UPDATE punishments
                SET active = 0, resolved = 1
                WHERE active = 1 AND expires_at > 0 AND expires_at < ?
            """);
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
