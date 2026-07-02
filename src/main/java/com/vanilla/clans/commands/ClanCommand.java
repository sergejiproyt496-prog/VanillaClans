package com.vanilla.clans.commands;

import com.vanilla.clans.VanillaClans;
import com.vanilla.clans.clan.Clan;
import com.vanilla.clans.clan.ClanManager;
import com.vanilla.clans.clan.ClanMember;
import com.vanilla.clans.clan.ClanRole;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor {

    private final VanillaClans plugin;
    private final ClanManager clanManager;

    public ClanCommand(VanillaClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду можно использовать только в игре.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "accept":
                handleAccept(player);
                break;
            case "deny":
                handleDeny(player);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "disband":
                handleDisband(player);
                break;
            case "promote":
                handlePromote(player, args);
                break;
            case "demote":
                handleDemote(player, args);
                break;
            case "war":
                handleWar(player, args);
                break;
            case "peace":
                handlePeace(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "top":
                handleTop(player);
                break;
            case "deposit":
                handleDeposit(player, args);
                break;
            case "withdraw":
                handleWithdraw(player, args);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══ " + ChatColor.YELLOW + "VanillaClans" + ChatColor.GOLD + " ═══");
        player.sendMessage(ChatColor.YELLOW + "/clan create <название> <тег> " + ChatColor.GRAY + "- создать клан");
        player.sendMessage(ChatColor.YELLOW + "/clan invite <игрок> " + ChatColor.GRAY + "- пригласить в клан");
        player.sendMessage(ChatColor.YELLOW + "/clan accept " + ChatColor.GRAY + "- принять приглашение");
        player.sendMessage(ChatColor.YELLOW + "/clan deny " + ChatColor.GRAY + "- отклонить приглашение");
        player.sendMessage(ChatColor.YELLOW + "/clan kick <игрок> " + ChatColor.GRAY + "- выгнать участника");
        player.sendMessage(ChatColor.YELLOW + "/clan leave " + ChatColor.GRAY + "- покинуть клан");
        player.sendMessage(ChatColor.YELLOW + "/clan disband " + ChatColor.GRAY + "- расформировать клан");
        player.sendMessage(ChatColor.YELLOW + "/clan promote <игрок> " + ChatColor.GRAY + "- повысить участника");
        player.sendMessage(ChatColor.YELLOW + "/clan demote <игрок> " + ChatColor.GRAY + "- понизить участника");
        player.sendMessage(ChatColor.YELLOW + "/clan war <клан> " + ChatColor.GRAY + "- объявить войну");
        player.sendMessage(ChatColor.YELLOW + "/clan peace <клан> " + ChatColor.GRAY + "- заключить мир");
        player.sendMessage(ChatColor.YELLOW + "/clan info [клан] " + ChatColor.GRAY + "- информация о клане");
        player.sendMessage(ChatColor.YELLOW + "/clan top " + ChatColor.GRAY + "- топ кланов");
        player.sendMessage(ChatColor.YELLOW + "/clan deposit <кол-во> " + ChatColor.GRAY + "- положить алмазы в банк клана");
        player.sendMessage(ChatColor.YELLOW + "/clan withdraw <кол-во> " + ChatColor.GRAY + "- забрать алмазы из банка клана");
        player.sendMessage(ChatColor.YELLOW + "/cc <сообщение> " + ChatColor.GRAY + "- клан-чат");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /clan create <название> <тег>");
            return;
        }

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Вы уже состоите в клане.");
            return;
        }

        String name = args[1];
        String tag = args[2];

        if (!clanManager.isValidName(name)) {
            player.sendMessage(ChatColor.RED + "Название клана: 3-16 символов, буквы/цифры/подчёркивание.");
            return;
        }

        if (!clanManager.isValidTag(tag)) {
            player.sendMessage(ChatColor.RED + "Тег клана: 2-6 символов, буквы/цифры.");
            return;
        }

        if (clanManager.nameExists(name)) {
            player.sendMessage(ChatColor.RED + "Клан с таким названием уже существует.");
            return;
        }

        if (clanManager.tagExists(tag)) {
            player.sendMessage(ChatColor.RED + "Клан с таким тегом уже существует.");
            return;
        }

        Clan clan = clanManager.createClan(name, tag, player.getUniqueId(), player.getName());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Не удалось создать клан. Попробуйте позже.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Клан " + ChatColor.GOLD + "[" + tag + "] " + name + ChatColor.GREEN + " успешно создан!");
    }

    private void handleInvite(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan invite <игрок>");
            return;
        }

        ClanMember sender = clan.getMember(player.getUniqueId());
        if (!sender.getRole().isAtLeast(ClanRole.OFFICER)) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно прав для приглашения игроков.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Игрок не найден или не в сети.");
            return;
        }

        if (clanManager.getClanByPlayer(target.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Этот игрок уже состоит в клане.");
            return;
        }

        clanManager.setInvite(target.getUniqueId(), clan.getId());
        player.sendMessage(ChatColor.GREEN + "Приглашение отправлено игроку " + target.getName() + ".");
        target.sendMessage(ChatColor.YELLOW + "Вас пригласили в клан " + ChatColor.GOLD + "[" + clan.getTag() + "] " + clan.getName()
                + ChatColor.YELLOW + ". Используйте /clan accept или /clan deny.");
    }

    private void handleAccept(Player player) {
        Integer clanId = clanManager.getInvite(player.getUniqueId());
        if (clanId == null) {
            player.sendMessage(ChatColor.RED + "У вас нет активных приглашений.");
            return;
        }

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Вы уже состоите в клане.");
            clanManager.clearInvite(player.getUniqueId());
            return;
        }

        Clan clan = clanManager.getClans().get(clanId);
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Этот клан больше не существует.");
            clanManager.clearInvite(player.getUniqueId());
            return;
        }

        clanManager.addMemberToClan(clan, player.getUniqueId(), player.getName());
        clanManager.clearInvite(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "Вы вступили в клан " + ChatColor.GOLD + "[" + clan.getTag() + "] " + clan.getName() + ChatColor.GREEN + "!");
        broadcastToClan(clan, ChatColor.YELLOW + player.getName() + " вступил(а) в клан!", player.getUniqueId());
    }

    private void handleDeny(Player player) {
        Integer clanId = clanManager.getInvite(player.getUniqueId());
        if (clanId == null) {
            player.sendMessage(ChatColor.RED + "У вас нет активных приглашений.");
            return;
        }
        clanManager.clearInvite(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Приглашение отклонено.");
    }

    private void handleKick(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan kick <игрок>");
            return;
        }

        ClanMember sender = clan.getMember(player.getUniqueId());
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[1]);
        ClanMember target = clan.getMember(targetOffline.getUniqueId());

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Этот игрок не состоит в вашем клане.");
            return;
        }

        if (!sender.getRole().canManage(target.getRole()) && !sender.getUuid().equals(target.getUuid())) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно прав, чтобы выгнать этого игрока.");
            return;
        }

        clanManager.removeMemberFromClan(clan, target.getUuid());
        player.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " исключён из клана.");
        broadcastToClan(clan, ChatColor.YELLOW + target.getName() + " был исключён из клана.", null);
    }

    private void handleLeave(Player player) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.getRole() == ClanRole.LEADER && clan.getSize() > 1) {
            player.sendMessage(ChatColor.RED + "Лидер не может покинуть клан, пока в нём есть другие участники. Передайте лидерство или расформируйте клан.");
            return;
        }

        clanManager.removeMemberFromClan(clan, player.getUniqueId());

        if (clan.getSize() == 0) {
            clanManager.disbandClan(clan);
            player.sendMessage(ChatColor.YELLOW + "Вы покинули клан. Клан был расформирован, так как в нём не осталось участников.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Вы покинули клан " + clan.getName() + ".");
            broadcastToClan(clan, ChatColor.YELLOW + player.getName() + " покинул(а) клан.", null);
        }
    }

    private void handleDisband(Player player) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.getRole() != ClanRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Только лидер может расформировать клан.");
            return;
        }

        broadcastToClan(clan, ChatColor.RED + "Клан был расформирован лидером.", null);
        clanManager.disbandClan(clan);
        player.sendMessage(ChatColor.GREEN + "Клан успешно расформирован.");
    }

    private void handlePromote(Player player, String[] args) {
        changeRole(player, args, true);
    }

    private void handleDemote(Player player, String[] args) {
        changeRole(player, args, false);
    }

    private void changeRole(Player player, String[] args, boolean promote) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan " + (promote ? "promote" : "demote") + " <игрок>");
            return;
        }

        ClanMember sender = clan.getMember(player.getUniqueId());
        if (sender.getRole() != ClanRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Только лидер может менять роли участников.");
            return;
        }

        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[1]);
        ClanMember target = clan.getMember(targetOffline.getUniqueId());
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Этот игрок не состоит в вашем клане.");
            return;
        }

        if (promote) {
            if (target.getRole() == ClanRole.OFFICER) {
                player.sendMessage(ChatColor.RED + "Этот игрок уже офицер. Передача лидерства не поддерживается этой командой.");
                return;
            }
            target.setRole(ClanRole.OFFICER);
        } else {
            if (target.getRole() == ClanRole.MEMBER) {
                player.sendMessage(ChatColor.RED + "Этот игрок уже на минимальной роли.");
                return;
            }
            target.setRole(ClanRole.MEMBER);
        }

        plugin.getDatabaseManager().updateMemberRole(target.getUuid(), target.getRole());
        player.sendMessage(ChatColor.GREEN + "Роль игрока " + target.getName() + " изменена на " + target.getRole().getDisplayName() + ".");
        broadcastToClan(clan, ChatColor.YELLOW + target.getName() + " теперь " + target.getRole().getDisplayName() + ".", null);
    }

    private void handleWar(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan war <клан>");
            return;
        }

        ClanMember sender = clan.getMember(player.getUniqueId());
        if (sender.getRole() != ClanRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Только лидер может объявлять войну.");
            return;
        }

        Clan enemy = clanManager.getClanByName(args[1]);
        if (enemy == null) {
            enemy = clanManager.getClanByTag(args[1]);
        }

        if (enemy == null) {
            player.sendMessage(ChatColor.RED + "Клан не найден.");
            return;
        }

        if (enemy.getId() == clan.getId()) {
            player.sendMessage(ChatColor.RED + "Нельзя объявить войну самому себе.");
            return;
        }

        clan.declareWar(enemy.getId());
        plugin.getDatabaseManager().addWar(clan.getId(), enemy.getId());

        broadcastToClan(clan, ChatColor.RED + "Ваш клан объявил войну клану [" + enemy.getTag() + "] " + enemy.getName() + "!", null);
        broadcastToClan(enemy, ChatColor.RED + "Клан [" + clan.getTag() + "] " + clan.getName() + " объявил вам войну!", null);
    }

    private void handlePeace(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan peace <клан>");
            return;
        }

        ClanMember sender = clan.getMember(player.getUniqueId());
        if (sender.getRole() != ClanRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Только лидер может заключать мир.");
            return;
        }

        Clan enemy = clanManager.getClanByName(args[1]);
        if (enemy == null) {
            enemy = clanManager.getClanByTag(args[1]);
        }

        if (enemy == null) {
            player.sendMessage(ChatColor.RED + "Клан не найден.");
            return;
        }

        clan.endWar(enemy.getId());
        plugin.getDatabaseManager().removeWar(clan.getId(), enemy.getId());

        broadcastToClan(clan, ChatColor.GREEN + "Ваш клан заключил мир с [" + enemy.getTag() + "] " + enemy.getName() + ".", null);
        broadcastToClan(enemy, ChatColor.GREEN + "Клан [" + clan.getTag() + "] " + clan.getName() + " заключил с вами мир.", null);
    }

    private void handleInfo(Player player, String[] args) {
        Clan clan;
        if (args.length >= 2) {
            clan = clanManager.getClanByName(args[1]);
            if (clan == null) clan = clanManager.getClanByTag(args[1]);
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Клан не найден.");
                return;
            }
        } else {
            clan = requireClan(player);
            if (clan == null) return;
        }

        player.sendMessage(ChatColor.GOLD + "═══ " + ChatColor.YELLOW + "[" + clan.getTag() + "] " + clan.getName() + ChatColor.GOLD + " ═══");
        player.sendMessage(ChatColor.YELLOW + "Лидер: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(clan.getLeader()).getName());
        player.sendMessage(ChatColor.YELLOW + "Участников: " + ChatColor.WHITE + clan.getSize());
        player.sendMessage(ChatColor.YELLOW + "Банк: " + ChatColor.WHITE + (int) clan.getBalance() + " алмазов");
        player.sendMessage(ChatColor.YELLOW + "В состоянии войны с: " + ChatColor.WHITE + clan.getWars().size() + " кланами");

        player.sendMessage(ChatColor.GOLD + "Участники:");
        for (ClanMember member : clan.getMembers().values()) {
            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + member.getName()
                    + ChatColor.GRAY + " (" + member.getRole().getDisplayName() + ")");
        }
    }

    private void handleTop(Player player) {
        List<Clan> sorted = clanManager.getClans().values().stream()
                .sorted(Comparator.comparingInt(Clan::getSize).reversed())
                .collect(Collectors.toList());

        player.sendMessage(ChatColor.GOLD + "═══ " + ChatColor.YELLOW + "Топ кланов" + ChatColor.GOLD + " ═══");
        int rank = 1;
        for (Clan clan : sorted) {
            if (rank > 10) break;
            player.sendMessage(ChatColor.YELLOW + "" + rank + ". " + ChatColor.GOLD + "[" + clan.getTag() + "] "
                    + ChatColor.WHITE + clan.getName() + ChatColor.GRAY + " - " + clan.getSize() + " участников");
            rank++;
        }
    }

    private void handleDeposit(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan deposit <кол-во алмазов>");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Некорректное количество.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Количество должно быть больше нуля.");
            return;
        }

        int have = countItems(player, org.bukkit.Material.DIAMOND);
        if (have < amount) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно алмазов. В инвентаре: " + have + ".");
            return;
        }

        removeItems(player, org.bukkit.Material.DIAMOND, amount);

        clan.deposit(amount);
        plugin.getDatabaseManager().updateBalance(clan.getId(), clan.getBalance());

        player.sendMessage(ChatColor.GREEN + "Вы положили " + amount + " алмазов в банк клана. Баланс: "
                + (int) clan.getBalance() + " алмазов.");
        broadcastToClan(clan, ChatColor.YELLOW + player.getName() + " пополнил(а) банк клана на " + amount + " алмазов.", player.getUniqueId());
    }

    private void handleWithdraw(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) return;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /clan withdraw <кол-во алмазов>");
            return;
        }

        ClanMember sender = clan.getMember(player.getUniqueId());
        if (!sender.getRole().isAtLeast(ClanRole.OFFICER)) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно прав, чтобы забирать алмазы из банка клана.");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Некорректное количество.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Количество должно быть больше нуля.");
            return;
        }

        if (clan.getBalance() < amount) {
            player.sendMessage(ChatColor.RED + "В банке клана недостаточно алмазов. Баланс: " + (int) clan.getBalance() + ".");
            return;
        }

        int freeSpace = countFreeSpace(player, org.bukkit.Material.DIAMOND);
        if (freeSpace < amount) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно места в инвентаре для " + amount + " алмазов.");
            return;
        }

        clan.withdraw(amount);
        plugin.getDatabaseManager().updateBalance(clan.getId(), clan.getBalance());

        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND, amount));

        player.sendMessage(ChatColor.GREEN + "Вы забрали " + amount + " алмазов из банка клана. Остаток: "
                + (int) clan.getBalance() + " алмазов.");
        broadcastToClan(clan, ChatColor.YELLOW + player.getName() + " забрал(а) " + amount + " алмазов из банка клана.", player.getUniqueId());
    }

    private int countItems(Player player, org.bukkit.Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, org.bukkit.Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int take = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - take);
                remaining -= take;
                if (item.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                } else {
                    player.getInventory().setItem(i, item);
                }
            }
        }
    }

    private int countFreeSpace(Player player, org.bukkit.Material material) {
        int free = 0;
        int maxStack = material.getMaxStackSize();
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == org.bukkit.Material.AIR) {
                free += maxStack;
            } else if (item.getType() == material) {
                free += maxStack - item.getAmount();
            }
        }
        return free;
    }

    private Clan requireClan(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Вы не состоите в клане.");
        }
        return clan;
    }

    private void broadcastToClan(Clan clan, String message, java.util.UUID exclude) {
        for (Map.Entry<java.util.UUID, ClanMember> entry : clan.getMembers().entrySet()) {
            if (exclude != null && entry.getKey().equals(exclude)) continue;
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
            }
        }
    }
}
