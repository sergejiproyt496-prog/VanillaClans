package com.vanilla.clans.clan;

import java.util.UUID;

public class ClanMember {

    private final UUID uuid;
    private String name;
    private ClanRole role;
    private long joinedAt;

    public ClanMember(UUID uuid, String name, ClanRole role, long joinedAt) {
        this.uuid = uuid;
        this.name = name;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClanRole getRole() {
        return role;
    }

    public void setRole(ClanRole role) {
        this.role = role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }
}
