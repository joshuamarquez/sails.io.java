package me.joshuamarquez.sails.io;

public interface WebSocketListener {

    void onResponse(WebSocketResponse response);

    void onError(Error error);

}
