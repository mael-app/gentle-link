package app.mael.gentleLink.database;

import app.mael.gentleLink.exception.DatabaseException;
import app.mael.gentleLink.model.AccountLink;
import app.mael.gentleLink.model.LinkCode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDatabaseAdapter implements DatabaseAdapter {
    protected static final Logger LOGGER = Logger.getLogger(AbstractDatabaseAdapter.class.getName());
    protected HikariDataSource dataSource;

    protected abstract HikariConfig createHikariConfig();
    
    protected abstract String getUpsertSyntax();

    @Override
    public void initialize() {
        HikariConfig config = createHikariConfig();
        dataSource = new HikariDataSource(config);
        createTables();
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void createTables() {
        String linkCodesTable = """
            CREATE TABLE IF NOT EXISTS link_codes (
                player_uuid VARCHAR(36) PRIMARY KEY,
                code VARCHAR(6) NOT NULL UNIQUE,
                expires_at TIMESTAMP NOT NULL
            )
        """;

        String accountLinksTable = """
            CREATE TABLE IF NOT EXISTS account_links (
                player_uuid VARCHAR(36) PRIMARY KEY,
                discord_id VARCHAR(20) NOT NULL UNIQUE,
                linked_at TIMESTAMP NOT NULL,
                is_manual BOOLEAN NOT NULL
            )
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(linkCodesTable);
            stmt.execute(accountLinksTable);
            LOGGER.info("Database tables created successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database tables", e);
            throw new DatabaseException("Failed to create tables", e);
        }
    }

    @Override
    public void createLinkCode(LinkCode linkCode) {
        String sql = getUpsertSyntax();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String uuidStr = linkCode.playerUuid().toString();
            Timestamp timestamp = Timestamp.valueOf(linkCode.expiresAt());
            
            stmt.setString(1, uuidStr);
            stmt.setString(2, linkCode.code());
            stmt.setTimestamp(3, timestamp);
            stmt.setString(4, linkCode.code());
            stmt.setTimestamp(5, timestamp);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create link code for player " + linkCode.playerUuid(), e);
            throw new DatabaseException("Failed to create link code", e);
        }
    }

    @Override
    public Optional<LinkCode> getLinkCode(String code) {
        String sql = "SELECT player_uuid, code, expires_at FROM link_codes WHERE code = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LinkCode(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("code"),
                        rs.getTimestamp("expires_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get link code: " + code, e);
            throw new DatabaseException("Failed to get link code", e);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<LinkCode> getLinkCodeByPlayer(UUID playerUuid) {
        String sql = "SELECT player_uuid, code, expires_at FROM link_codes WHERE player_uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LinkCode(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("code"),
                        rs.getTimestamp("expires_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get link code by player: " + playerUuid, e);
            throw new DatabaseException("Failed to get link code by player", e);
        }
        
        return Optional.empty();
    }

    @Override
    public void deleteLinkCode(String code) {
        String sql = "DELETE FROM link_codes WHERE code = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to delete link code: " + code, e);
            throw new DatabaseException("Failed to delete link code", e);
        }
    }

    @Override
    public void deleteExpiredLinkCodes() {
        String sql = "DELETE FROM link_codes WHERE expires_at < ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                LOGGER.info("Deleted " + deleted + " expired link codes");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to delete expired link codes", e);
            throw new DatabaseException("Failed to delete expired link codes", e);
        }
    }

    @Override
    public void createAccountLink(AccountLink accountLink) {
        String sql = "INSERT INTO account_links (player_uuid, discord_id, linked_at, is_manual) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountLink.playerUuid().toString());
            stmt.setString(2, accountLink.discordId());
            stmt.setTimestamp(3, Timestamp.valueOf(accountLink.linkedAt()));
            stmt.setBoolean(4, accountLink.isManual());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create account link for player " + accountLink.playerUuid(), e);
            throw new DatabaseException("Failed to create account link", e);
        }
    }

    @Override
    public Optional<AccountLink> getAccountLink(UUID playerUuid) {
        String sql = "SELECT player_uuid, discord_id, linked_at, is_manual FROM account_links WHERE player_uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AccountLink(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("discord_id"),
                        rs.getTimestamp("linked_at").toLocalDateTime(),
                        rs.getBoolean("is_manual")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get account link for player " + playerUuid, e);
            throw new DatabaseException("Failed to get account link", e);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<AccountLink> getAccountLinkByDiscord(String discordId) {
        String sql = "SELECT player_uuid, discord_id, linked_at, is_manual FROM account_links WHERE discord_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discordId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AccountLink(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("discord_id"),
                        rs.getTimestamp("linked_at").toLocalDateTime(),
                        rs.getBoolean("is_manual")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get account link by discord: " + discordId, e);
            throw new DatabaseException("Failed to get account link by discord", e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<AccountLink> getAllAccountLinks() {
        List<AccountLink> links = new ArrayList<>();
        String sql = "SELECT player_uuid, discord_id, linked_at, is_manual FROM account_links";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                links.add(new AccountLink(
                    UUID.fromString(rs.getString("player_uuid")),
                    rs.getString("discord_id"),
                    rs.getTimestamp("linked_at").toLocalDateTime(),
                    rs.getBoolean("is_manual")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get all account links", e);
            throw new DatabaseException("Failed to get all account links", e);
        }
        
        return links;
    }

    @Override
    public void deleteAccountLink(UUID playerUuid) {
        String sql = "DELETE FROM account_links WHERE player_uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to delete account link for player " + playerUuid, e);
            throw new DatabaseException("Failed to delete account link", e);
        }
    }

    @Override
    public boolean isPlayerLinked(UUID playerUuid) {
        return getAccountLink(playerUuid).isPresent();
    }
}

