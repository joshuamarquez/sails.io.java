package me.joshuamarquez.sails.io.singleton;

import me.joshuamarquez.sails.io.SailsIOClient;

public class AnotherSailsSocketSingleton extends SailsIOClient {

    private static AnotherSailsSocketSingleton instance;

    private AnotherSailsSocketSingleton() { /* Empty constructor */ }

    public static AnotherSailsSocketSingleton getInstance() {
        if (instance == null) {
            instance = new AnotherSailsSocketSingleton();
        }

        return instance;
    }
}
