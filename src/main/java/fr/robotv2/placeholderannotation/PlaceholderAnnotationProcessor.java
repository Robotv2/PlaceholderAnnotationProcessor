package fr.robotv2.placeholderannotation;

import fr.robotv2.placeholderannotation.impl.PlaceholderAnnotationProcessorImpl;
import org.bukkit.OfflinePlayer;

import java.util.logging.Logger;

public interface PlaceholderAnnotationProcessor {

    static PlaceholderAnnotationProcessor create() {
        return new PlaceholderAnnotationProcessorImpl();
    }

    void registerExpansion(BasePlaceholderExpansion basePlaceholderExpansion);

    String process(OfflinePlayer offlinePlayer, String params);

    <T> ValueResolver<T> getValueResolver(Class<T> clazz);

    <T> void registerValueResolver(Class<? extends T> clazz, ValueResolver<? extends T> resolver);
}
