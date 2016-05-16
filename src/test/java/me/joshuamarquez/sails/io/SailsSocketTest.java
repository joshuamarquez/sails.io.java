package me.joshuamarquez.sails.io;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SailsSocketTest {

    private final static int TIMEOUT = 7000;

    private final String url = "http://localhost:1337";

    @Test(timeout = TIMEOUT)
    public void connect() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();
        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                values.offer("done");
            }
        });

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

}