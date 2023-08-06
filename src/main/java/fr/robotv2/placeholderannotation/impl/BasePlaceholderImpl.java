package fr.robotv2.placeholderannotation.impl;

import fr.robotv2.placeholderannotation.BasePlaceholder;
import fr.robotv2.placeholderannotation.BasePlaceholderExpansion;
import fr.robotv2.placeholderannotation.PAPDebug;
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
        final Object[] objects = new Object[getTypes().length];

        final boolean hasRequestIssuer = getTypes()[0] != null && getTypes()[0].isAssignableFrom(RequestIssuer.class);
        final Class<?>[] types = hasRequestIssuer ? Arrays.copyOfRange(getTypes(), 1, getTypes().length) : getTypes();

        if(params.length != types.length) {
            PAPDebug.debug("param's length and type's length are not the same.");
            return null;
        }

        if(hasRequestIssuer) {
            objects[0] = new RequestIssuerImpl(offlinePlayer);
        }

        final int startingIndex = hasRequestIssuer ? 1 : 0; // Either 1 or 0 to avoid replacing first element.
        for(int i = 0; i < params.length; i++) {
            objects[i + startingIndex] = this.processParam(params[i], types[i]);
        }

        // Invoke method with expansion class and processed objects
        return this.invokeMethod(objects);
    }

    // Process individual parameter into corresponding object
    private Object processParam(String param, Class<?> type) {

        final Object object;

        if(type.isEnum()) {
            object = Enum.valueOf(type.asSubclass(Enum.class), param);
        } else {
            object = Objects.requireNonNull
                    (
                            processor.getValueResolver(type),
                            "resolver couldn't be found for " + param
                    ).resolve(param);
        }

        return object;
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
