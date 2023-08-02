package fr.robotv2.placeholderannotation;

import fr.robotv2.placeholderannotation.impl.PlaceholderAnnotationProcessorImpl;
import fr.robotv2.placeholderannotation.interfaces.ValueResolver;
import org.bukkit.OfflinePlayer;

import java.util.logging.Logger;

public interface PlaceholderAnnotationProcessor {

    static PlaceholderAnnotationProcessor create() {
        return new PlaceholderAnnotationProcessorImpl();
    }

    static Logger logger() {
        return Logger.getLogger("PAP");
    }

    <T> ValueResolver<T> getValueResolver(Class<T> clazz);

    <T> void registerValueResolver(Class<? extends T> clazz, ValueResolver<? extends T> resolver);

    void register(BasePlaceholderExpansion basePlaceholderExpansion);

    String process(OfflinePlayer offlinePlayer, String params);
}
