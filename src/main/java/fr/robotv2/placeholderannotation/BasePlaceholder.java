package fr.robotv2.placeholderannotation;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public interface BasePlaceholder {

    String getIdentifier();

    Method getMethod();

    default Class<?>[] getTypes() {
        return getMethod().getParameterTypes();
    }

    String process(OfflinePlayer offlinePlayer, @NotNull String[] params);
}
