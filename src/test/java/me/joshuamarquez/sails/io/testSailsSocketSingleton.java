package me.joshuamarquez.sails.io;

public class testSailsSocketSingleton extends SailsIOClient {

    private static testSailsSocketSingleton instance;

    private testSailsSocketSingleton() { /* Empty constructor */ }

    public static testSailsSocketSingleton getInstance() {
        if (instance == null) {
            instance = new testSailsSocketSingleton();
        }

        return instance;
    }
}
