package app.mael.gentleLink;

import app.mael.gentleLink.command.LinkCommand;
import app.mael.gentleLink.command.UnlinkCommand;
import app.mael.gentleLink.database.DatabaseAdapter;
import app.mael.gentleLink.database.MariaDBDatabaseAdapter;
import app.mael.gentleLink.database.SQLiteDatabaseAdapter;
import app.mael.gentleLink.listener.PlayerConnectionListener;
import app.mael.gentleLink.service.LinkService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private DatabaseAdapter database;
    private LinkService linkService;
    private PlayerConnectionListener connectionListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        initializeDatabase();
        initializeServices();
        registerCommands();
        registerListeners();
        startCleanupTask();
        
        getLogger().info("GentleLink a été activé avec succès!");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.shutdown();
        }
        
        getLogger().info("GentleLink a été désactivé.");
    }

    private void initializeDatabase() {
        FileConfiguration config = getConfig();
        String dbType = config.getString("database.type", "SQLITE").toUpperCase();

        if (dbType.equals("MARIADB") && config.getBoolean("database.mariadb.enabled", false)) {
            String host = config.getString("database.mariadb.host", "localhost");
            int port = config.getInt("database.mariadb.port", 3306);
            String dbName = config.getString("database.mariadb.database", "gentlelink");
            String username = config.getString("database.mariadb.username", "root");
            String password = config.getString("database.mariadb.password", "password");
            
            database = new MariaDBDatabaseAdapter(host, port, dbName, username, password);
            getLogger().info("Connexion à la base de données MariaDB...");
        } else {
            database = new SQLiteDatabaseAdapter(getDataFolder());
            getLogger().info("Utilisation de SQLite comme base de données.");
        }

        database.initialize();
        getLogger().info("Base de données initialisée avec succès.");
    }

    private void initializeServices() {
        linkService = new LinkService(database);
    }

    private void registerCommands() {
        LinkCommand linkCommand = new LinkCommand(linkService, this);
        getCommand("link").setExecutor(linkCommand);
        getCommand("link").setTabCompleter(linkCommand);
        
        UnlinkCommand unlinkCommand = new UnlinkCommand(linkService, this);
        getCommand("unlink").setExecutor(unlinkCommand);
        getCommand("unlink").setTabCompleter(unlinkCommand);
        
        getLogger().info("Commandes enregistrées avec succès.");
    }

    private void registerListeners() {
        FileConfiguration config = getConfig();
        String chatMessage = config.getString("messages.not_linked.chat", 
            "<red><bold>Vous devez lier votre compte Discord!</bold></red>\n<yellow>Utilisez /link</yellow>");
        String titleMessage = config.getString("messages.not_linked.title", 
            "<red><bold>Compte Non Lié</bold></red>");
        String subtitleMessage = config.getString("messages.not_linked.subtitle", 
            "<yellow>Utilisez /link pour continuer</yellow>");
        
        connectionListener = new PlayerConnectionListener(linkService, chatMessage, titleMessage, subtitleMessage);
        Bukkit.getPluginManager().registerEvents(connectionListener, this);
        
        getLogger().info("Listeners enregistrés avec succès.");
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            linkService.cleanupExpiredCodes();
        }, 20L * 60L, 20L * 60L);
        
        getLogger().info("Tâche de nettoyage des codes expirés démarrée (toutes les 60 secondes).");
    }

    public LinkService getLinkService() {
        return linkService;
    }

    public DatabaseAdapter getDatabase() {
        return database;
    }

    public PlayerConnectionListener getConnectionListener() {
        return connectionListener;
    }
}
