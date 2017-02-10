package me.joshuamarquez.sails.io;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.joshuamarquez.sails.io.singleton.AnotherSailsSocketSingleton;
import me.joshuamarquez.sails.io.singleton.TestSailsSocketSingleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SingletonTest extends SailsServer {

    private TestSailsSocketSingleton testSailsSocketSingleton;
    private AnotherSailsSocketSingleton anotherSailsSocketSingleton;

    private String testURL = "http://test.com";
    private String anotherURL = "http://another.com";

    private final String url = "http://localhost:" + PORT;

    @Before
    public void setUp() throws Exception {
        testSailsSocketSingleton = TestSailsSocketSingleton.getInstance();
        anotherSailsSocketSingleton = AnotherSailsSocketSingleton.getInstance();
    }

    @Test
    public void differentURL() throws Exception {
        testSailsSocketSingleton.setUrl(testURL);
        anotherSailsSocketSingleton.setUrl(anotherURL);

        assertThat(testSailsSocketSingleton.getUrl(), not(anotherSailsSocketSingleton.getUrl()));
    }

    @Test
    public void differentHeader() throws Exception {
        testSailsSocketSingleton.setUrl(url);
        anotherSailsSocketSingleton.setUrl(url);

        testSailsSocketSingleton.socket();
        anotherSailsSocketSingleton.socket();

        testSailsSocketSingleton.setHeaders(new HashMap<String, String>(){
            {
                put("Header-1", "value-1");
            }
        });

        anotherSailsSocketSingleton.setHeaders(new HashMap<String, String>(){
            {
                put("Header-2", "value-2");
            }
        });

        assertThat(testSailsSocketSingleton.getHeaders().get("Header-1"), is("value-1"));
        assertThat(anotherSailsSocketSingleton.getHeaders().get("Header-2"), is("value-2"));
    }

    @Test
    public void socketConnection () throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        testSailsSocketSingleton.socket().on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                assertThat(testSailsSocketSingleton.socket().isConnected(), is(true));
                anotherSailsSocketSingleton.setUrl("http://localhost");
                assertThat(anotherSailsSocketSingleton.socket().isConnected(), is(false));
                values.offer("done");
            }
        });

        testSailsSocketSingleton.socket().connect();
        values.take();
        testSailsSocketSingleton.socket().disconnect();
    }

}
