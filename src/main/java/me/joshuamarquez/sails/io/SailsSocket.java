package me.joshuamarquez.sails.io;

public class SailsSocket {

    private static SailsSocket instance;

    private SailsSocket() {

    }

    public synchronized static SailsSocket getInstance() {
        if (instance == null) {
            instance = new SailsSocket();
        }

        return instance;
    }

}
