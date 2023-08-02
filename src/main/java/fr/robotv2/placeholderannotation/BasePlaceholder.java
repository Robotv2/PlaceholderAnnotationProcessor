package fr.robotv2.placeholderannotation;

import fr.robotv2.placeholderannotation.impl.RequestIssuerImpl;
import fr.robotv2.placeholderannotation.interfaces.ValueResolver;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BasePlaceholder {

    private final PlaceholderAnnotationProcessor processor;

    private final String identifier;
    private final BasePlaceholderExpansion expansionClazz;
    private final Method method;

    public BasePlaceholder(
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

    public String getIdentifier() {
        return identifier;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getTypes() {
        return getMethod().getParameterTypes();
    }

    public String process(OfflinePlayer offlinePlayer, String[] params) {

        if(params == null) {
            PAPUtil.debug("params is null");
            return null;
        }

        if(params.length != getTypes().length - 1) {
            PAPUtil.debug("param's length & type's length are not the same.");
            return null;
        }

        final Class<?>[] types = Arrays.copyOfRange(getTypes(), 1, getTypes().length);
        final Object[] objects = new Object[params.length + 1];
        objects[0] = new RequestIssuerImpl(offlinePlayer);

        for(int i = 0; i < params.length; i++) {

            final String param = params[i];
            final Class<?> type = types[i];
            final Object object;

            if(type.isEnum()) {
                final Class<? extends Enum> enumType = type.asSubclass(Enum.class);
                Map<String, Enum<?>> values = new HashMap<>();

                for (Enum<?> enumConstant : enumType.getEnumConstants()) {
                    values.put(enumConstant.name().toLowerCase(), enumConstant);
                }

                object = values.get(param.toLowerCase(Locale.ROOT));
            } else {
                final ValueResolver<?> resolver = processor.getValueResolver(type);
                if(resolver == null) {
                    throw new NullPointerException("resolver couldn't be found for " + param);
                }
                object = resolver.resolver(param);
            }

            objects[i] = object;
        }

        try {

            PAPUtil.debug("invoking");
            PAPUtil.debug(String.join(", ", params));
            PAPUtil.debug(Arrays.stream(objects).map(object -> object.getClass().getSimpleName()).collect(Collectors.joining(", ")));

            final Object result = method.invoke(
                    this.expansionClazz,
                    objects
            );

            if(!result.getClass().isAssignableFrom(String.class)) {
                throw new IllegalStateException(method.getName() + " return type is not a string. found: " + result.getClass().getSimpleName());
            }

            PAPUtil.debug("result here");
            return (String) result;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            exception.printStackTrace();
        }

        return null;
    }
}
