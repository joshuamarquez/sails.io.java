package me.joshuamarquez.sails.io.singleton;

import me.joshuamarquez.sails.io.SailsIOClient;

public class TestSailsSocketSingleton extends SailsIOClient {

    private static TestSailsSocketSingleton instance;

    private TestSailsSocketSingleton() { /* Empty constructor */ }

    public static TestSailsSocketSingleton getInstance() {
        if (instance == null) {
            instance = new TestSailsSocketSingleton();
        }

        return instance;
    }
}
