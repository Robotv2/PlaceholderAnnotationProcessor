package fr.robotv2.placeholderannotation;

import fr.robotv2.placeholderannotation.impl.PlaceholderAnnotationProcessorImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.Logger;

public interface PlaceholderAnnotationProcessor {

    static PlaceholderAnnotationProcessor create() {
        return new PlaceholderAnnotationProcessorImpl();
    }

    static Logger getLogger() {
        return PluginLogger.getLogger("PAP");
    }

    void registerExpansion(BasePlaceholderExpansion basePlaceholderExpansion);

    String process(OfflinePlayer offlinePlayer, String params);

    <T> ValueResolver<T> getValueResolver(Class<T> clazz);

    <T> void registerValueResolver(Class<? extends T> clazz, ValueResolver<? extends T> resolver);
}
