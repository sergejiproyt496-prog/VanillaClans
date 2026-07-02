package com.vanilla.clans.data;

import com.vanilla.clans.VanillaClans;
import com.vanilla.clans.clan.Clan;
import com.vanilla.clans.clan.ClanMember;
import com.vanilla.clans.clan.ClanRole;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final VanillaClans plugin;
    private Connection connection;

    public DatabaseManager(VanillaClans plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "clans.db");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Не удалось подключиться к базе данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS clans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE NOT NULL," +
                    "tag TEXT UNIQUE NOT NULL," +
                    "leader TEXT NOT NULL," +
                    "balance REAL DEFAULT 0," +
                    "created_at INTEGER NOT NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS clan_members (" +
                    "uuid TEXT PRIMARY KEY," +
                    "clan_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "role TEXT NOT NULL," +
                    "joined_at INTEGER NOT NULL," +
                    "FOREIGN KEY(clan_id) REFERENCES clans(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS clan_wars (" +
                    "clan_id INTEGER NOT NULL," +
                    "enemy_id INTEGER NOT NULL," +
                    "PRIMARY KEY(clan_id, enemy_id)" +
                    ")");
        }
    }

    public int createClan(String name, String tag, UUID leader) {
        String sql = "INSERT INTO clans (name, tag, leader, balance, created_at) VALUES (?, ?, ?, 0, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, tag);
            ps.setString(3, leader.toString());
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void addMember(int clanId, UUID uuid, String name, ClanRole role) {
        String sql = "INSERT INTO clan_members (uuid, clan_id, name, role, joined_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, clanId);
            ps.setString(3, name);
            ps.setString(4, role.name());
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMember(UUID uuid) {
        String sql = "DELETE FROM clan_members WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMemberRole(UUID uuid, ClanRole role) {
        String sql = "UPDATE clan_members SET role = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBalance(int clanId, double balance) {
        String sql = "UPDATE clans SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setInt(2, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClan(int clanId) {
        try {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM clan_members WHERE clan_id = ?")) {
                ps.setInt(1, clanId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM clan_wars WHERE clan_id = ? OR enemy_id = ?")) {
                ps.setInt(1, clanId);
                ps.setInt(2, clanId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM clans WHERE id = ?")) {
                ps.setInt(1, clanId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWar(int clanId, int enemyId) {
        String sql = "INSERT OR IGNORE INTO clan_wars (clan_id, enemy_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, clanId);
            ps.setInt(2, enemyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeWar(int clanId, int enemyId) {
        String sql = "DELETE FROM clan_wars WHERE clan_id = ? AND enemy_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, clanId);
            ps.setInt(2, enemyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает все кланы, участников и войны из базы в память при старте плагина.
     */
    public Map<Integer, Clan> loadAllClans() {
        Map<Integer, Clan> clans = new HashMap<>();

        String sql = "SELECT * FROM clans";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Clan clan = new Clan(
                        id,
                        rs.getString("name"),
                        rs.getString("tag"),
                        UUID.fromString(rs.getString("leader")),
                        rs.getLong("created_at")
                );
                clan.setBalance(rs.getDouble("balance"));
                clans.put(id, clan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String memberSql = "SELECT * FROM clan_members";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(memberSql)) {
            while (rs.next()) {
                int clanId = rs.getInt("clan_id");
                Clan clan = clans.get(clanId);
                if (clan == null) continue;

                ClanMember member = new ClanMember(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        ClanRole.valueOf(rs.getString("role")),
                        rs.getLong("joined_at")
                );
                clan.addMember(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String warSql = "SELECT * FROM clan_wars";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(warSql)) {
            while (rs.next()) {
                int clanId = rs.getInt("clan_id");
                int enemyId = rs.getInt("enemy_id");
                Clan clan = clans.get(clanId);
                if (clan != null) {
                    clan.declareWar(enemyId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clans;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
