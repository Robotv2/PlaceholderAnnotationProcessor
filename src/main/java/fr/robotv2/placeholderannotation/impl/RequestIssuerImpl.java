package fr.robotv2.placeholderannotation.impl;

import fr.robotv2.placeholderannotation.RequestIssuer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class RequestIssuerImpl implements RequestIssuer {

    private final OfflinePlayer offlinePlayer;

    public RequestIssuerImpl(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
    }

    public boolean isOnlinePlayer() {
        return offlinePlayer.getPlayer() != null && offlinePlayer.getPlayer().isOnline();
    }

    public boolean isOfflinePlayer() {
        return offlinePlayer != null;
    }

    @Nullable
    public Player getPlayer() {
        return isOnlinePlayer() ? offlinePlayer.getPlayer() : null;
    }

    @Nullable
    public OfflinePlayer getOfflinePlayer() {
        return this.offlinePlayer;
    }
}
