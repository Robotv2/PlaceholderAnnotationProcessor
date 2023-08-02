package fr.robotv2.placeholderannotation.impl;

import fr.robotv2.placeholderannotation.*;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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

        // Validate parameters
        if(params == null) {
            PAPUtil.debug("params is null");
            return null;
        }

        if(params.length != (getTypes().length - 1)) {
            PAPUtil.debug("param's length and type's length are not the same.");
            return null;
        }

        // Prepare types and objects
        final Class<?>[] types = Arrays.copyOfRange(getTypes(), 1, getTypes().length);
        final Object[] objects = new Object[params.length + 1];

        objects[0] = new RequestIssuerImpl(offlinePlayer);

        for(int i = 0; i < params.length; i++) {
            objects[i + 1] = this.processParam(params[i], types[i]);
        }

        // Invoke method with expansion class and processed objects
        return this.invokeMethod(objects);
    }

    // Process individual parameter into corresponding object
    private Object processParam(String param, Class<?> type) {

        final Object object;

        if(type.isEnum()) {
            object = this.getEnumValue(param, type);
        } else {
            object = Objects.requireNonNull(
                    processor.getValueResolver(type),
                    "resolver couldn't be found for " + param
            ).resolve(param);
        }

        return object;
    }

    // Get Enum value based on input parameter
    private Enum<?> getEnumValue(String param, Class<?> type) {

        if(!type.isEnum()) {
            return null;
        }

        final Class<? extends Enum> enumType = type.asSubclass(Enum.class);
        Map<String, Enum<?>> values = new HashMap<>();

        for (Enum<?> enumConstant : enumType.getEnumConstants()) {
            values.put(enumConstant.name().toLowerCase(), enumConstant);
        }

        return values.get(param.toLowerCase(Locale.ROOT));
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
