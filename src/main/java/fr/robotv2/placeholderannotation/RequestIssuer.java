package fr.robotv2.placeholderannotation;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface RequestIssuer {

    /**
     * @return whether an offline player instance has been given.
     */
    boolean isConsole();

    /**
     * @return whether an actually valid online player has been given.
     */
    boolean isOnlinePlayer();

    /**
     * @return whether an actually valid offline player has been given.
     */
    boolean isOfflinePlayer();

    /**
     * @return the online player object. null if no currently online player could be found.
     */
    @Nullable
    Player getOnlinePlayer();

    /**
     * @return the offline player object. null if no offline player could be found.
     */
    @Nullable
    OfflinePlayer getOfflinePlayer();
}
