package fr.robotv2.placeholderannotation;

import fr.robotv2.placeholderannotation.impl.RequestIssuerImpl;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BasePlaceholder {

    private final PlaceholderAnnotationProcessor processor;

    private final String identifier;
    private final Class<? extends PlaceholderExpansion> expansionClazz;
    private final Method method;

    public BasePlaceholder(
            PlaceholderAnnotationProcessor processor,
            String identifier,
            Class<? extends PlaceholderExpansion> expansionClazz,
            Method method
    ) {
        this.processor = processor;
        this.identifier = identifier;
        this.expansionClazz = expansionClazz;
        this.method = method;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getTypes() {
        return getMethod().getParameterTypes();
    }

    public String apply(OfflinePlayer offlinePlayer, String[] params) {

        if(params == null) {
            return null;
        }

        if(params.length != getTypes().length) {
            return null;
        }

        final Class<?>[] types = getTypes();
        final Object[] objects = new Object[params.length + 1];
        objects[0] = new RequestIssuerImpl(offlinePlayer);

        for(int i = 0; i < params.length; i++) {
            final String param = params[i];
            final Class<?> type = types[i];
            final Object object = processor.getValueResolver(type).resolver(param);
            objects[i + 1] = object;
        }

        try {
            final Object result = method.invoke(
                    this.expansionClazz,
                    objects
            );

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
