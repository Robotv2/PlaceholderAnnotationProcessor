package fr.robotv2.placeholderannotation.impl;

import fr.robotv2.placeholderannotation.*;
import fr.robotv2.placeholderannotation.annotations.Placeholder;
import fr.robotv2.placeholderannotation.ValueResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderAnnotationProcessorImpl implements PlaceholderAnnotationProcessor{

    private final Map<Class<?>, ValueResolver<?>> resolvers = new HashMap<>();
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
    public void register(BasePlaceholderExpansion basePlaceholderExpansion) {

        final Method[] methods = basePlaceholderExpansion.getClass().getDeclaredMethods();

        PAPDebug.debug("Register expansion. Found " + methods.length + " methods.");

        for(Method method : methods) {

            if(!method.isAnnotationPresent(Placeholder.class)) {
                continue;
            }

            final Class<?>[] types = method.getParameterTypes();

            for(Class<?> type : types) {
                checkType(type);
            }

            final String identifier = method.getAnnotation(Placeholder.class).identifier();

            final BasePlaceholder basePlaceholder = new BasePlaceholderImpl(
                    this,
                    identifier,
                    basePlaceholderExpansion,
                    method
            );

            PAPDebug.debug("Registering placeholder");
            this.placeholders.put(identifier, basePlaceholder);
        }
    }

    public String process(OfflinePlayer offlinePlayer, String params) {

        final String[] args = params.split("_");
        final String identifier = args[0];

        if(!placeholders.containsKey(identifier)) {
            PAPDebug.debug("No identifier found for : " + identifier);
            return null;
        }

        final BasePlaceholder basePlaceholder = placeholders.get(identifier);
        final String[] paramsArgs = Arrays.copyOfRange(args, 1, args.length);

        PAPDebug.debug("Args Found : " + String.join(", ", paramsArgs));

        return basePlaceholder.process(offlinePlayer, paramsArgs);
    }


    private void checkType(Class<?> type) {

        if(type == RequestIssuer.class) {
            return;
        }

        if(type.isPrimitive()) {
            throw new IllegalArgumentException("PAP does not support primitive types.");
        }

        if(this.getValueResolver(type) == null && !type.isEnum()) {
            throw new NullPointerException("No resolver found for type class: " + type.getSimpleName());
        }
    }

    private void registerDefaultValueResolver() {
        resolvers.put(String.class, (ValueResolver<String>) param -> param);
        resolvers.put(Integer.class, (ValueResolver<Integer>) param -> new BigInteger(param).intValue());
        resolvers.put(Long.class, (ValueResolver<Long>) param -> new BigInteger(param).longValue());
        resolvers.put(Double.class, (ValueResolver<Double>) param -> new BigDecimal(param).doubleValue());
        resolvers.put(Float.class, (ValueResolver<Float>) param -> new BigDecimal(param).floatValue());
        resolvers.put(Byte.class, (ValueResolver<Byte>) param -> new BigInteger(param).byteValueExact());
        resolvers.put(Void.class, (ValueResolver<Void>) param -> null);
        resolvers.put(Player.class, (ValueResolver<Player>) Bukkit::getPlayer);
        resolvers.put(OfflinePlayer.class, (ValueResolver<OfflinePlayer>) param -> {
            final Player player = Bukkit.getPlayer(param);
            if(player != null && player.isOnline()) {
                return player;
            } else {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(param);
                return offlinePlayer.hasPlayedBefore() ? offlinePlayer : null;
            }
        });
        resolvers.put(World.class, Bukkit::getWorld);
    }
}
