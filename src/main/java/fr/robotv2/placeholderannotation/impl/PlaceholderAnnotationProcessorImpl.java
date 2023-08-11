package fr.robotv2.placeholderannotation.impl;

import fr.robotv2.placeholderannotation.*;
import fr.robotv2.placeholderannotation.annotations.DefaultPlaceholder;
import fr.robotv2.placeholderannotation.annotations.Placeholder;
import fr.robotv2.placeholderannotation.util.PAPDebug;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderAnnotationProcessorImpl implements PlaceholderAnnotationProcessor{

    private final Map<Class<?>, ValueResolver<?>> resolvers = new HashMap<>();

    private BasePlaceholder defaultPlaceholder = null;
    private final Map<String, BasePlaceholder> placeholders = new HashMap<>();

    public PlaceholderAnnotationProcessorImpl() {
        this.registerDefaultValueResolver();
    }

    @Override
    public <T> ValueResolver<T> getValueResolver(Class<T> clazz) {

        final ValueResolver<?> resolver = resolvers.get(clazz);

        if (resolver == null) {
            return null;
        }

        try {
            return (ValueResolver<T>) resolver;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The class " + clazz + " is not of type " + resolver.getClass(), e);
        }
    }

    @Override
    public <T> void registerValueResolver(Class<? extends T> clazz, ValueResolver<? extends T> resolver) {
        resolvers.put(clazz, resolver);
    }

    @Override
    public void registerExpansion(BasePlaceholderExpansion basePlaceholderExpansion) {

        final Method[] methods = basePlaceholderExpansion.getClass().getDeclaredMethods();

        for(Method method : methods) {

            if(!method.isAnnotationPresent(Placeholder.class)
                    && method.isAnnotationPresent(DefaultPlaceholder.class)) {
                continue;
            }

            final Class<?>[] types = method.getParameterTypes();

            for(Class<?> type : types) {
                checkType(type, method);
            }

            final String identifier =
                    method.isAnnotationPresent(Placeholder.class) ?
                    method.getAnnotation(Placeholder.class).identifier() : "";

            final BasePlaceholder basePlaceholder = new BasePlaceholderImpl(
                    this,
                    identifier,
                    basePlaceholderExpansion,
                    method
            );

            if(method.isAnnotationPresent(DefaultPlaceholder.class)) {
                if(defaultPlaceholder == null) {
                    this.defaultPlaceholder = basePlaceholder;
                } else {
                    throw new IllegalStateException("only one method can have the annotation @DefaultPlaceholder.");
                }
            }

            PAPDebug.debug("Registering placeholder with identifier: " + identifier);
            this.placeholders.put(identifier, basePlaceholder);
        }
    }

    @Override
    public String process(OfflinePlayer offlinePlayer, String params) {

        final String[] args = params.split("_");
        final String identifier = args[0];

        final BasePlaceholder basePlaceholder;
        final String[] paramsArgs;

        if(placeholders.containsKey(identifier)) {
            basePlaceholder = placeholders.get(identifier);
            paramsArgs = Arrays.copyOfRange(args, 1, args.length);
        } else if(defaultPlaceholder != null) {
            basePlaceholder = defaultPlaceholder;
            paramsArgs = args;
        } else {
            return null;
        }

        PAPDebug.debug("Args Found : " + String.join(", ", paramsArgs));
        return basePlaceholder.process(offlinePlayer, paramsArgs);
    }


    private void checkType(Class<?> type, Method method) {

        if(type == RequestIssuer.class) {
            return;
        }

        if(type.isPrimitive()) {
            PlaceholderAnnotationProcessor.getLogger().warning("primitive types (" + type.getSimpleName() + " ) has been found on method " + method.getName());
            PlaceholderAnnotationProcessor.getLogger().warning("Primitive types are supported but are not recommended. Please consider switching with their wrapper object instead.");
        }

        if(this.getValueResolver(type) == null && !type.isEnum()) {
            throw new NullPointerException("No resolver found for type class: " + type.getSimpleName());
        }
    }

    private void registerDefaultValueResolver() {
        resolvers.put(String.class, (ValueResolver<String>) (issuer, param) -> param);
        resolvers.put(Integer.class, (ValueResolver<Integer>) (issuer, param) -> Integer.parseInt(param));
        resolvers.put(Long.class, (ValueResolver<Long>) (issuer, param) -> Long.parseLong(param));
        resolvers.put(Double.class, (ValueResolver<Double>) (issuer, param) -> Double.parseDouble(param));
        resolvers.put(Float.class, (ValueResolver<Float>) (issuer, param) -> Float.parseFloat(param));
        resolvers.put(Byte.class, (ValueResolver<Byte>) (issuer, param) -> Byte.parseByte(param));
        resolvers.put(Void.class, (ValueResolver<Void>) (issuer, param) -> null);
        resolvers.put(Boolean.class, (ValueResolver<Boolean>) (issuer, param) -> Boolean.parseBoolean(param));
        resolvers.put(Character.class, (ValueResolver<Character>) (issuer, param) -> param.charAt(0));
        resolvers.put(Short.class, (ValueResolver<Short>) (issuer, param) -> Short.parseShort(param));
        resolvers.put(Player.class, (ValueResolver<Player>) (issuer, param) -> Bukkit.getPlayer(param));
        resolvers.put(OfflinePlayer.class, (ValueResolver<OfflinePlayer>) (issuer, param) -> {
            final Player player = Bukkit.getPlayer(param);
            if(player != null && player.isOnline()) {
                return player;
            } else {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(param);
                return offlinePlayer.hasPlayedBefore() ? offlinePlayer : null;
            }
        });
        resolvers.put(World.class, (issuer, param) -> Bukkit.getWorld(param));
    }
}
