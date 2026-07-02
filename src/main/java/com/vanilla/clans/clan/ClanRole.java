package com.vanilla.clans.clan;

public enum ClanRole {

    LEADER("Лидер", 3),
    OFFICER("Офицер", 2),
    MEMBER("Участник", 1);

    private final String displayName;
    private final int rank;

    ClanRole(String displayName, int rank) {
        this.displayName = displayName;
        this.rank = rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRank() {
        return rank;
    }

    public boolean canManage(ClanRole target) {
        return this.rank > target.rank;
    }

    public boolean isAtLeast(ClanRole other) {
        return this.rank >= other.rank;
    }
}
