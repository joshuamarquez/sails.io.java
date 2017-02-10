package me.joshuamarquez.sails.io;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.joshuamarquez.sails.io.singleton.TestSailsSocketSingleton;
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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SailsSocketTest extends SailsServer {

    private final String url = "http://localhost:" + PORT;
    private final String TAG = "SailsSocketTest";

    private JSONObject EXPECTED_RESPONSES;

    private Map<String, String> headers = new HashMap<String, String>() {
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
        TestSailsSocketSingleton.getInstance().setUrl(url);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();
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
    public void isConnectedShouldReturnTrue() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();
        final SailsSocket sailsSocket = new SailsSocket(url);
        sailsSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                assertThat(sailsSocket.isConnected(), is(true));

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

        SailsSocket sailsSocket = new SailsSocket(url);
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
    public void shouldGetErrorWhenNotSettingUrl() throws Exception {
        try {
            TestSailsSocketSingleton.getInstance().socket();
        } catch(Exception e) {
            assertThat(e.getMessage(), is("Url must be initialized"));
        }
    }

    @Test(timeout = TIMEOUT)
    public void shouldGetErrorWhenSettingSocketUrl() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        TestSailsSocketSingleton.getInstance().setUrl(url);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();
        sailsSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    TestSailsSocketSingleton.getInstance().setUrl("http://127.0.0.1:" + PORT);
                } catch (Exception e) {
                    assertThat(e.getMessage(), is("Can not change url while socket is connected"));
                    values.offer("done");
                }
            }
        });

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void shouldNotGetErrorWhenSettingSocketUrl() throws Exception {
        TestSailsSocketSingleton.getInstance().setUrl("http://127.0.0.1:" + PORT);
        assertThat(TestSailsSocketSingleton.getInstance().getUrl(), not(url));
    }

    @Test(timeout = TIMEOUT)
    public void shouldGetErrorWhenSettingSocketOptions() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        TestSailsSocketSingleton.getInstance().setUrl(url);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();
        sailsSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    TestSailsSocketSingleton.getInstance().setOptions(new IO.Options());
                } catch (Exception e) {
                    assertThat(e.getMessage(), is("Can not change options while socket is connected"));
                    values.offer("done");
                }
            }
        });

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void shouldNotGetErrorWhenSettingSocketOptions() throws Exception {
        IO.Options options = new IO.Options(){
            {
                query = "foo=bar";
            }
        };
        TestSailsSocketSingleton.getInstance().setOptions(options);
        assertThat(TestSailsSocketSingleton.getInstance().getOptions().query, is("foo=bar"));
    }

    @Test(timeout = TIMEOUT)
    public void testGet() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        sailsSocket.get(TAG, "/hello", null, buildResponseListener("get /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testDelete() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        sailsSocket.delete(TAG, "/hello", null, buildResponseListener("delete /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testPost() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        JSONObject params = new JSONObject() { { put("foo", "posted!"); } };
        sailsSocket.post(TAG, "/hello", params, buildResponseListener("post /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testPut() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        JSONObject params = new JSONObject() { { put("foo", "putted!"); } };
        sailsSocket.put(TAG, "/hello", params, buildResponseListener("put /hello", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetJSON() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        sailsSocket.get(TAG, "/someJSON", null, buildResponseListener("get /someJSON", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetError() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        sailsSocket.get(TAG, "/someError", null, buildResponseListener("get /someError", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetHeaders() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        SailsSocket sailsSocket = new SailsSocket(url);
        sailsSocket.setHeaders(headers);

        sailsSocket.get(TAG, "/headers", null, buildResponseListener("get /headers", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testGetHeadersOverride()throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        TestSailsSocketSingleton.getInstance().setHeaders(headers);
        TestSailsSocketSingleton.getInstance().setUrl(url);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();

        sailsSocket.request(TAG, SailsSocketRequest.METHOD_GET, "/headersOverride", null,
                new HashMap<String, String>() {
                    {
                        put("x-test-header-one", "baz");
                    }
                }, buildResponseListener("get /headersOverride", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test
    public void connectNextUrl() throws Exception {
        TestSailsSocketSingleton.getInstance().resetNextConnection();
        TestSailsSocketSingleton.getInstance().setUrl("http://localhost:7331");
        TestSailsSocketSingleton.getInstance().socket().connect();

        Thread.sleep(5000);
        assertThat(TestSailsSocketSingleton.getInstance().socket().isConnected(), is(false));

        TestSailsSocketSingleton.getInstance().resetNextConnection();
    }

    @Test(timeout = TIMEOUT)
    public void testGetHeadersRemove()throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        TestSailsSocketSingleton.getInstance().setHeaders(headers);
        TestSailsSocketSingleton.getInstance().setUrl(url);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();

        sailsSocket.request(TAG, SailsSocketRequest.METHOD_GET, "/headersRemove", null,
                new HashMap<String, String>() {
                    {
                        put("x-test-header-one", null);
                    }
                }, buildResponseListener("get /headersRemove", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testQueryOption() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        IO.Options options = new IO.Options();
        options.query = "x-test-query-one={\"foo\":\"bar\"}";
        TestSailsSocketSingleton.getInstance().setUrl(url);
        TestSailsSocketSingleton.getInstance().setOptions(options);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();

        sailsSocket.get(TAG, "/queryJSON", null, buildResponseListener("get /queryJSON", values));

        sailsSocket.connect();
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testInitialHeaders() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        TestSailsSocketSingleton.getInstance().setUrl(url);
        SailsSocket sailsSocket = TestSailsSocketSingleton.getInstance().socket();
        sailsSocket.get(TAG, "/initHeaders", null, buildResponseListener("get /initHeaders", values));

        sailsSocket.connect(new HashMap<String, List<String>>() {
            {
                put("x-test-init-header-one", Arrays.asList("init-header-value"));
            }
        });
        values.take();
        sailsSocket.disconnect();
    }

    @Test(timeout = TIMEOUT)
    public void testOneSocketSession() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        final SailsSocket sailsSocket = new SailsSocket(url, new IO.Options());

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

        SailsSocket sailsSocket1 = new SailsSocket(url);
        final SailsSocket sailsSocket2 = new SailsSocket(url);

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

        SailsSocket sailsSocket1 = new SailsSocket(url);
        final SailsSocket sailsSocket2 = new SailsSocket(url);

        // Make a request to Sails' built-in /__getcookie route
        HttpResponse<String>stringResponse = Unirest.get("http://localhost:1577/__getcookie").asString();
        String setCookieHeader = stringResponse.getHeaders().get("set-cookie").toString();
        // Get the cookie data from the set-cookie header
        final String cookie = setCookieHeader.substring(setCookieHeader.indexOf("[") + 1, setCookieHeader.indexOf(";"));

        final Map<String, List<String>> initialHeaders = new HashMap<String, List<String>>() {
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

    @Test(timeout = TIMEOUT)
    public void testOneSocketNOSession() throws Exception {
        final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();

        final SailsSocket sailsSocket = new SailsSocket(url);

        sailsSocket.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
            @Override
            public void onResponse(JWR response) {
                assertEquals(response.getBody().toString(), "NO_SESSION");

                sailsSocket.get(TAG, "/count", null, new SailsSocketResponse.Listener() {
                    @Override
                    public void onResponse(JWR response) {
                        assertEquals(response.getBody().toString(), "NO_SESSION");

                        values.offer("done");
                    }
                });
            }
        });

        sailsSocket.connect(new HashMap<String, List<String>>() {
            {
                put("nosession", Arrays.asList("true"));
            }
        });
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
    private SailsSocketResponse.Listener buildResponseListener(final String routeAddress,
                                                               final BlockingQueue<Object> values) throws Exception {
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
        Object expectedBody = EXPECTED_RESPONSES.getJSONObject(routeAddress).get("body");

        if (expectedBody.toString().equals("null")) {
            assertNull(response.getBody());
        }

        else {
            assertThat(expectedBody.toString(), is(response.getBody().toString()));
        }

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