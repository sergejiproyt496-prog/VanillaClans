package com.vanilla.clans.commands;

import com.vanilla.clans.VanillaClans;
import com.vanilla.clans.clan.Clan;
import com.vanilla.clans.clan.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ClanChatCommand implements CommandExecutor {

    private final VanillaClans plugin;

    public ClanChatCommand(VanillaClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду можно использовать только в игре.");
            return true;
        }

        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Вы не состоите в клане.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /cc <сообщение>");
            return true;
        }

        String message = String.join(" ", args);
        String formatted = ChatColor.DARK_AQUA + "[Клан] " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + message;

        for (Map.Entry<UUID, ClanMember> entry : clan.getMembers().entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target != null && target.isOnline()) {
                target.sendMessage(formatted);
            }
        }

        return true;
    }
}
