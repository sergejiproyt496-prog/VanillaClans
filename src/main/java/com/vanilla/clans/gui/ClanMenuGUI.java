package com.vanilla.clans.gui;

import com.vanilla.clans.clan.Clan;
import com.vanilla.clans.clan.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Простое GUI-меню клана: показывает участников и их роли.
 * Открывается через inventory click listener (добавить отдельно при расширении).
 */
public class ClanMenuGUI {

    public static Inventory build(Clan clan) {
        int size = Math.max(9, ((clan.getSize() / 9) + 1) * 9);
        size = Math.min(size, 54);

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_AQUA + "Клан: " + clan.getName());

        for (ClanMember member : clan.getMembers().values()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(member.getUuid()));
                meta.setDisplayName(ChatColor.YELLOW + member.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Роль: " + ChatColor.WHITE + member.getRole().getDisplayName());
                meta.setLore(lore);

                skull.setItemMeta(meta);
            }
            inv.addItem(skull);
        }

        return inv;
    }
}
