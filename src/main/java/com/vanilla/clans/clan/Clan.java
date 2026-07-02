package com.vanilla.clans.clan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Clan {

    private int id;
    private String name;
    private String tag;
    private UUID leader;
    private double balance;
    private long createdAt;

    private final Map<UUID, ClanMember> members = new HashMap<>();
    private final Map<Integer, Boolean> wars = new HashMap<>(); // clanId -> true (враждебны)

    public Clan(int id, String name, String tag, UUID leader, long createdAt) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.balance = 0;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public boolean withdraw(double amount) {
        if (balance < amount) return false;
        balance -= amount;
        return true;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<UUID, ClanMember> getMembers() {
        return members;
    }

    public void addMember(ClanMember member) {
        members.put(member.getUuid(), member);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public ClanMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    public int getSize() {
        return members.size();
    }

    public boolean isAtWarWith(int clanId) {
        return wars.getOrDefault(clanId, false);
    }

    public void declareWar(int clanId) {
        wars.put(clanId, true);
    }

    public void endWar(int clanId) {
        wars.remove(clanId);
    }

    public Map<Integer, Boolean> getWars() {
        return wars;
    }
}
