package app.mael.gentleLink.database;

import com.zaxxer.hikari.HikariConfig;

public class MariaDBDatabaseAdapter extends AbstractDatabaseAdapter {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MariaDBDatabaseAdapter(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    protected HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return config;
    }

    @Override
    protected String getUpsertSyntax() {
        return "INSERT INTO link_codes (player_uuid, code, expires_at) VALUES (?, ?, ?) " +
               "ON DUPLICATE KEY UPDATE code = ?, expires_at = ?";
    }
}

