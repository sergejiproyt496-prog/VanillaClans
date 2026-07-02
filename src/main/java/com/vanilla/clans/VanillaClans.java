package com.vanilla.clans;

import com.vanilla.clans.clan.ClanManager;
import com.vanilla.clans.commands.ClanCommand;
import com.vanilla.clans.commands.ClanChatCommand;
import com.vanilla.clans.data.DatabaseManager;
import com.vanilla.clans.listeners.ChatListener;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaClans extends JavaPlugin {

    private static VanillaClans instance;

    private DatabaseManager databaseManager;
    private ClanManager clanManager;

    @Override
    public void onEnable() {
        instance = this;

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();

        this.clanManager = new ClanManager(this);
        this.clanManager.loadAll();

        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("cc").setExecutor(new ClanChatCommand(this));

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("VanillaClans включен успешно!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("VanillaClans выключен.");
    }

    public static VanillaClans getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }
}
