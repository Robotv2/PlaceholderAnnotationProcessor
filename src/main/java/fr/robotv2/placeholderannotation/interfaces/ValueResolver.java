package fr.robotv2.placeholderannotation.interfaces;

@FunctionalInterface
public interface ValueResolver<T> {
    T resolver(String param);
}
