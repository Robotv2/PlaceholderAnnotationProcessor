package fr.robotv2.placeholderannotation;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface RequestIssuer {

    /**
     * Checks if the player is online.
     *
     * @return true if the associated OfflinePlayer has an online player, false otherwise.
     */
    boolean isOnlinePlayer();

    /**
     * Checks if a valid OfflinePlayer instance has been given.
     *
     * @return true if the associated OfflinePlayer exists, false otherwise.
     */
    boolean isOfflinePlayer();

    /**
     * Provides the online associated player.
     *
     * @return Online Player if it exists and is online, otherwise null.
     */
    @Nullable
    Player getPlayer();

    /**
     * Provides the offline player given by PlaceholderAPI
     *
     * @return OfflinePlayer associated with the instance.
     */
    @Nullable
    OfflinePlayer getOfflinePlayer();
}
