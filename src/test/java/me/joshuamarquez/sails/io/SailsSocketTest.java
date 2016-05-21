package me.joshuamarquez.sails.io;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SailsSocketTest extends SailsServer {

    private final String url = "http://localhost:" + PORT;

    private final String TAG = "SailsSocketTest";

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

    @Test
    public void shouldReturnStatusCodeNotFound() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/invalid_path", null, new SailsSocketResponse.Listener() {
            @Override
            public void onResponse(JWR response) {
                assertThat(response.getStatusCode(), is(404));
                assertTrue(response.isError());

                values.offer("done");
            }
        });

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

}