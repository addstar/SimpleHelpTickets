package me.odium.simplehelptickets.objects;

/**
 * Used to return a related set of values
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class Pair<T, U> {
    public final T object1;
    public final U object2;

    public Pair(T t, U u) {
        this.object1 = t;
        this.object2 = u;
    }
}
