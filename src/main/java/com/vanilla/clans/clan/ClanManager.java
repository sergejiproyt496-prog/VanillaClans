package com.vanilla.clans.clan;

import com.vanilla.clans.VanillaClans;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ClanManager {

    private final VanillaClans plugin;
    private final Map<Integer, Clan> clans = new HashMap<>();
    private final Map<UUID, Integer> memberClanIndex = new HashMap<>();

    // приглашения: приглашённый игрок -> id клана
    private final Map<UUID, Integer> pendingInvites = new HashMap<>();

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я0-9_]{3,16}$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я0-9]{2,6}$");

    public ClanManager(VanillaClans plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        clans.clear();
        memberClanIndex.clear();

        Map<Integer, Clan> loaded = plugin.getDatabaseManager().loadAllClans();
        clans.putAll(loaded);

        for (Clan clan : clans.values()) {
            for (UUID uuid : clan.getMembers().keySet()) {
                memberClanIndex.put(uuid, clan.getId());
            }
        }

        plugin.getLogger().info("Загружено кланов: " + clans.size());
    }

    public boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    public boolean isValidTag(String tag) {
        return TAG_PATTERN.matcher(tag).matches();
    }

    public boolean nameExists(String name) {
        return clans.values().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }

    public boolean tagExists(String tag) {
        return clans.values().stream().anyMatch(c -> c.getTag().equalsIgnoreCase(tag));
    }

    public Clan createClan(String name, String tag, UUID leader, String leaderName) {
        int id = plugin.getDatabaseManager().createClan(name, tag, leader);
        if (id == -1) return null;

        Clan clan = new Clan(id, name, tag, leader, System.currentTimeMillis());
        ClanMember member = new ClanMember(leader, leaderName, ClanRole.LEADER, System.currentTimeMillis());
        clan.addMember(member);

        plugin.getDatabaseManager().addMember(id, leader, leaderName, ClanRole.LEADER);

        clans.put(id, clan);
        memberClanIndex.put(leader, id);

        return clan;
    }

    public void disbandClan(Clan clan) {
        for (UUID uuid : clan.getMembers().keySet()) {
            memberClanIndex.remove(uuid);
        }
        clans.remove(clan.getId());
        plugin.getDatabaseManager().deleteClan(clan.getId());
    }

    public Clan getClanByPlayer(UUID uuid) {
        Integer clanId = memberClanIndex.get(uuid);
        if (clanId == null) return null;
        return clans.get(clanId);
    }

    public Clan getClanByName(String name) {
        return clans.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Clan getClanByTag(String tag) {
        return clans.values().stream()
                .filter(c -> c.getTag().equalsIgnoreCase(tag))
                .findFirst()
                .orElse(null);
    }

    public void addMemberToClan(Clan clan, UUID uuid, String name) {
        ClanMember member = new ClanMember(uuid, name, ClanRole.MEMBER, System.currentTimeMillis());
        clan.addMember(member);
        memberClanIndex.put(uuid, clan.getId());
        plugin.getDatabaseManager().addMember(clan.getId(), uuid, name, ClanRole.MEMBER);
    }

    public void removeMemberFromClan(Clan clan, UUID uuid) {
        clan.removeMember(uuid);
        memberClanIndex.remove(uuid);
        plugin.getDatabaseManager().removeMember(uuid);
    }

    public void setInvite(UUID player, int clanId) {
        pendingInvites.put(player, clanId);
    }

    public Integer getInvite(UUID player) {
        return pendingInvites.get(player);
    }

    public void clearInvite(UUID player) {
        pendingInvites.remove(player);
    }

    public Map<Integer, Clan> getClans() {
        return clans;
    }
}
