package fr.robotv2.placeholderannotation.impl;

import com.sun.tools.javac.util.Pair;
import fr.robotv2.placeholderannotation.BasePlaceholder;
import fr.robotv2.placeholderannotation.BasePlaceholderExpansion;
import fr.robotv2.placeholderannotation.PlaceholderAnnotationProcessor;
import fr.robotv2.placeholderannotation.RequestIssuer;
import fr.robotv2.placeholderannotation.annotations.Optional;
import fr.robotv2.placeholderannotation.annotations.RequireOnlinePlayer;
import fr.robotv2.placeholderannotation.util.PAPDebug;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

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

        if(hasRequestIssuer) {
            processedObjects[0] = issuer;
        }

        final int startingIndex = hasRequestIssuer ? 1 : 0; // Either 1 or 0 to avoid replacing first element.

        for(int i = 0; i < types.length; i++) {

            Class<?> type = Objects.requireNonNull(types[i]);
            Object object = null;

            if(type.isPrimitive()) {
                type = fromPrimitiveToWrapper(type);
            }

            if(i < params.length) {

                object = this.processParam(issuer, params[i], type);

            } else if(type.isAnnotationPresent(Optional.class)) {

                final Optional optionalAnnotation = type.getAnnotation(Optional.class);
                final String defaultArg = optionalAnnotation.defaultParameter();

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
                ? Enum.valueOf(type.asSubclass(Enum.class), param.toUpperCase(Locale.ROOT))
                : Objects.requireNonNull(processor.getValueResolver(type)).resolve(issuer, param);
    }

    // Invoke method with processed objects and handle potential exceptions
    private String invokeMethod(Object[] objects) {
        try {
            final Object result = method.invoke(this.expansionClazz, objects);

            if(result == null) {
                return null;
            }

            return result.getClass().isAssignableFrom(String.class) ? (String) result : result.toString();
        } catch (IllegalAccessException | ClassCastException exception) {
            PlaceholderAnnotationProcessor.getLogger().log(Level.SEVERE, "An exception occurred: ", exception);
        } catch (InvocationTargetException exception) {
            PlaceholderAnnotationProcessor.getLogger().log(Level.SEVERE, "An error occurred during the invocation of method: " + method.getName(), exception);
        }

        return null;
    }

    private Class<?> fromPrimitiveToWrapper(Class<?> primitive) {

        if (!primitive.isPrimitive()) {
            return primitive;
        }

        if (primitive.equals(boolean.class)) {
            return Boolean.class;
        } else if (primitive.equals(int.class)) {
            return Integer.class;
        } else if (primitive.equals(long.class)) {
            return Long.class;
        } else if (primitive.equals(double.class)) {
            return Double.class;
        } else if (primitive.equals(float.class)) {
            return Float.class;
        } else if (primitive.equals(byte.class)) {
            return Byte.class;
        } else if (primitive.equals(char.class)) {
            return Character.class;
        } else if (primitive.equals(short.class)) {
            return Short.class;
        } else if (primitive.equals(void.class)) {
            return Void.class;
        } else {
            throw new IllegalArgumentException("Class " + primitive + " is not a primitive type");
        }
    }
}
