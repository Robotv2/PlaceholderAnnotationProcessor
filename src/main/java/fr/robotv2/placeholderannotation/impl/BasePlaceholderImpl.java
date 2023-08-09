package fr.robotv2.placeholderannotation.impl;

import fr.robotv2.placeholderannotation.BasePlaceholder;
import fr.robotv2.placeholderannotation.BasePlaceholderExpansion;
import fr.robotv2.placeholderannotation.annotations.Optional;
import fr.robotv2.placeholderannotation.annotations.RequireOnlinePlayer;
import fr.robotv2.placeholderannotation.util.PAPDebug;
import fr.robotv2.placeholderannotation.PlaceholderAnnotationProcessor;
import fr.robotv2.placeholderannotation.RequestIssuer;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class BasePlaceholderImpl implements BasePlaceholder {

    private final PlaceholderAnnotationProcessor processor;

    private final String identifier;
    private final BasePlaceholderExpansion expansionClazz;
    private final Method method;

    public BasePlaceholderImpl(
            PlaceholderAnnotationProcessor processor,
            String identifier,
            BasePlaceholderExpansion expansionClazz,
            Method method
    ) {
        this.processor = processor;
        this.identifier = identifier;
        this.expansionClazz = expansionClazz;
        this.method = method;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String process(OfflinePlayer offlinePlayer, String[] params) {

        // Prepare types and objects
        final Object[] processedObjects = new Object[getTypes().length];
        final Class<?>[] methodTypes = this.getTypes();

        final boolean hasRequestIssuer = methodTypes[0] != null && methodTypes[0].isAssignableFrom(RequestIssuer.class);

        final Class<?>[] types = hasRequestIssuer ? Arrays.copyOfRange(methodTypes, 1, methodTypes.length) : methodTypes;
        final RequestIssuer issuer = new RequestIssuerImpl(offlinePlayer);

        if(method.isAnnotationPresent(RequireOnlinePlayer.class) && !issuer.isOnlinePlayer()) {
            return null;
        }

        if(hasRequestIssuer) {
            processedObjects[0] = issuer;
        }

        final int startingIndex = hasRequestIssuer ? 1 : 0; // Either 1 or 0 to avoid replacing first element.

        for(int i = 0; i < types.length; i++) {

            final Class<?> type = Objects.requireNonNull(types[i]);
            Object object = null;

            if(i < params.length) {

                object = this.processParam(issuer, params[i], type);

            } else if(type.isAnnotationPresent(Optional.class)) {

                final Optional optionalAnnotation = type.getAnnotation(Optional.class);
                final String defaultArg = optionalAnnotation.defaultArg();

                if(defaultArg != null && !defaultArg.isEmpty()) {
                    object = this.processParam(issuer, defaultArg, type);
                } else {
                    PAPDebug.debug("Missing default argument for optional parameter in " + method.getName() + " method.");
                }

            } else {
                PAPDebug.debug("Missing argument for " + method.getName() + " method. Need " + getTypes().length + " parameter(s) - Found " + params.length + " parameter(s).");
                return null;
            }

            processedObjects[i + startingIndex] = object;
        }

        // Invoke method with expansion class and processed objects
        return this.invokeMethod(processedObjects);
    }

    // Process individual parameter into corresponding object
    private Object processParam(RequestIssuer issuer, String param, Class<?> type) {
        return type.isEnum()
                ? Enum.valueOf(type.asSubclass(Enum.class), param)
                : Objects.requireNonNull(processor.getValueResolver(type)).resolve(issuer, param);
    }

    // Invoke method with processed objects and handle potential exceptions
    private String invokeMethod(Object[] objects) {
        try {
            final Object result = method.invoke(this.expansionClazz, objects);

            if(result == null) {
                return null;
            }

            if(!result.getClass().isAssignableFrom(String.class)) {
                throw new IllegalStateException(method.getName() + " return type is not a string. found: " + result.getClass().getSimpleName());
            }

            return (String) result;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            exception.printStackTrace();
        }

        return null;
    }
}
