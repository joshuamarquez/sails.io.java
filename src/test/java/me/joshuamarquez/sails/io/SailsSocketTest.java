package me.joshuamarquez.sails.io;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.joshuamarquez.sails.io.util.SIJUtils;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SailsSocketTest extends SailsServer {

    private final String url = "http://localhost:" + PORT;
    private final String TAG = "SailsSocketTest";

    private JSONObject EXPECTED_RESPONSES;

    @Before
    public void setUp() throws Exception {
        EXPECTED_RESPONSES = SIJUtils.getExpectedResponses();
    }

    @Test(timeout = TIMEOUT)
    public void connect() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        // Get global sails socket
        SailsSocket sailsSocket = SailsIOClient.getInstance().socket(url, new IO.Options());
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

    @Test
    public void testGet() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/hello", null, buildResponseListener("get /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test
    public void testDelete() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.delete(TAG, "/hello", null, buildResponseListener("delete /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test
    public void testPost() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.post(TAG, "/hello", null, buildResponseListener("post /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test
    public void testPut() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.put(TAG, "/hello", null, buildResponseListener("put /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test
    public void testGetJSON() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/someJSON", null, buildResponseListener("get /someJSON", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test
    public void testGetError() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/someError", null, buildResponseListener("get /someError", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    /**
     * Creates new Response Listener
     *
     * @param routeAddress route address for assertion
     * @param values
     * @return {@link SailsSocketResponse.Listener}
     * @throws Exception
     */
    private SailsSocketResponse.Listener buildResponseListener(String routeAddress,
                                                               BlockingQueue<Object> values) throws Exception {
        return new SailsSocketResponse.Listener() {
            @Override
            public void onResponse(JWR response) {
                assertResponse(routeAddress, response);

                values.offer("done");
            }
        };
    }

    private void assertResponse(String routeAddress, JWR response) {
        // Ensure JWR JSON Object is not null
        assertNotNull(response.getJsonObjectResponse());

        System.out.println(response.getJsonObjectResponse().toString());

        // Ensure body is the correct value
        Object expectedBody;
        try {
            expectedBody = EXPECTED_RESPONSES.getJSONObject(routeAddress).get("body");
        } catch (JSONException e) {
            expectedBody = null;
        }

        assertThat(expectedBody.toString(), is(response.getBody().toString()));

        // Ensure JWR statusCode is correct
        int expectedStatusCode;
        try {
            expectedStatusCode = EXPECTED_RESPONSES.getJSONObject(routeAddress).getInt("statusCode");
        } catch (JSONException e) {
            expectedStatusCode = 200;
        }

        assertThat(expectedStatusCode, is(response.getStatusCode()));
    }

}