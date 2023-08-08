package fr.robotv2.placeholderannotation;

@FunctionalInterface
public interface ValueResolver<T> {
    T resolve(RequestIssuer issuer, String param);
}
