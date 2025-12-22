package app.mael.gentleLink.database;

import com.zaxxer.hikari.HikariConfig;

import java.io.File;

public class SQLiteDatabaseAdapter extends AbstractDatabaseAdapter {
    private final File dataFolder;

    public SQLiteDatabaseAdapter(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    protected HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File dbFile = new File(dataFolder, "gentlelink.db");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return config;
    }

    @Override
    protected String getUpsertSyntax() {
        return "INSERT INTO link_codes (player_uuid, code, expires_at) VALUES (?, ?, ?) " +
               "ON CONFLICT(player_uuid) DO UPDATE SET code = ?, expires_at = ?";
    }
}

