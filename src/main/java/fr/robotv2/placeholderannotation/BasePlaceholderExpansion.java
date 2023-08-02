package fr.robotv2.placeholderannotation;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class BasePlaceholderExpansion extends PlaceholderExpansion {

    private final PlaceholderAnnotationProcessor processor;

    public BasePlaceholderExpansion(PlaceholderAnnotationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        return processor.process(offlinePlayer, params);
    }
}
