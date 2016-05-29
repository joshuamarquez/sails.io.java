package me.joshuamarquez.sails.io;

public class SailsSocketResponse {

    public interface Listener {
        void onResponse(JWR response);
    }

}
