package oarlib.core;

/**
 * To adhere to the factory pattern.
 * Created by Oliver Lum on 7/26/2014.
 */
public interface Factory<T> {
    T instantiate();
}
