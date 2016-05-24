package me.joshuamarquez.sails.io;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.joshuamarquez.sails.io.util.SIJUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SailsSocketTest extends SailsServer {

    private final String url = "http://localhost:" + PORT;
    private final String TAG = "SailsSocketTest";

    private JSONObject EXPECTED_RESPONSES;

    private Map<String, String> headers = new HashMap<String, String>(){
        {
            put("x-test-header-one", "foo");
            put("x-test-header-two", "bar");
        }
    };

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

    @Test(timeout = TIMEOUT)
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

    @Test(timeout = TIMEOUT)
    public void testGet() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/hello", null, buildResponseListener("get /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testDelete() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.delete(TAG, "/hello", null, buildResponseListener("delete /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testPost() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.post(TAG, "/hello", null, buildResponseListener("post /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testPut() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.put(TAG, "/hello", null, buildResponseListener("put /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetJSON() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/someJSON", null, buildResponseListener("get /someJSON", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetError() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.get(TAG, "/someError", null, buildResponseListener("get /someError", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetHeaders() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());
        sailsSocket.setHeaders(headers);

        sailsSocket.get(TAG, "/headers", null, buildResponseListener("get /headers", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetHeadersOverride()throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsIOClient.getInstance().setHeaders(headers);
        SailsSocket sailsSocket = SailsIOClient.getInstance().socket(url, new IO.Options());

        sailsSocket.request(TAG, SailsSocketRequest.METHOD_GET, "/headersOverride", null,
                new HashMap<String, String>(){
                    {
                        put("x-test-header-one", "baz");
                    }
                }, buildResponseListener("get /headersOverride", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetHeadersRemove()throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsIOClient.getInstance().setHeaders(headers);
        SailsSocket sailsSocket = SailsIOClient.getInstance().socket(url, new IO.Options());

        sailsSocket.request(TAG, SailsSocketRequest.METHOD_GET, "/headersRemove", null,
                new HashMap<String, String>(){
                    {
                        put("x-test-header-one", null);
                    }
                }, buildResponseListener("get /headersRemove", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testOneSocketSession() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());

        sailsSocket.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
            @Override
            public void onResponse(JWR response) {
                assertEquals(Integer.parseInt(response.getBody().toString()), 1);

                sailsSocket.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
                    @Override
                    public void onResponse(JWR response) {
                        assertEquals(Integer.parseInt(response.getBody().toString()), 2);

                        values.offer("done");
                    }
                });
            }
        });

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testTwoSocketsNotSharingSession() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket1 = new SailsSocket(url, new IO.Options());
        SailsSocket sailsSocket2 = new SailsSocket(url, new IO.Options());

        sailsSocket1.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
            @Override
            public void onResponse(JWR response) {
                assertEquals(Integer.parseInt(response.getBody().toString()), 1);

                sailsSocket2.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
                    @Override
                    public void onResponse(JWR response) {
                        assertEquals(Integer.parseInt(response.getBody().toString()), 1);

                        values.offer("done");
                    }
                });
                sailsSocket2.connect();
            }
        });

        sailsSocket1.connect();
        values.take();
        sailsSocket1.disconnect();
        sailsSocket2.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testTwoSocketsSharingSession() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket1 = new SailsSocket(url, new IO.Options());
        SailsSocket sailsSocket2 = new SailsSocket(url, new IO.Options());

        // Make a request to Sails' built-in /__getcookie route
        HttpResponse<String>stringResponse = Unirest.get("http://localhost:1577/__getcookie").asString();
        String setCookieHeader = stringResponse.getHeaders().get("set-cookie").toString();
        // Get the cookie data from the set-cookie header
        String cookie = setCookieHeader.substring(setCookieHeader.indexOf("[") + 1, setCookieHeader.indexOf(";"));

        Map<String, List<String>> initialHeaders = new HashMap<String, List<String>>() {
            {
                put("cookie", Arrays.asList(cookie));
            }
        };

        sailsSocket1.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
            @Override
            public void onResponse(JWR response) {
                assertEquals(Integer.parseInt(response.getBody().toString()), 1);

                sailsSocket2.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
                    @Override
                    public void onResponse(JWR response) {
                        assertEquals(Integer.parseInt(response.getBody().toString()), 2);

                        values.offer("done");
                    }
                });
                sailsSocket2.connect(initialHeaders);
            }
        });

        sailsSocket1.connect(initialHeaders);
        values.take();
        sailsSocket1.disconnect();
        sailsSocket2.disconnect();
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