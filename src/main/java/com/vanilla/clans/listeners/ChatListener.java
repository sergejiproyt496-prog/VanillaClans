package com.vanilla.clans.listeners;

import com.vanilla.clans.VanillaClans;
import com.vanilla.clans.clan.Clan;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final VanillaClans plugin;

    public ChatListener(VanillaClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Clan clan = plugin.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());

        if (clan == null) {
            return;
        }

        String tagPrefix = ChatColor.GOLD + "[" + clan.getTag() + "] " + ChatColor.RESET;
        event.setFormat(tagPrefix + event.getFormat());
    }
}
