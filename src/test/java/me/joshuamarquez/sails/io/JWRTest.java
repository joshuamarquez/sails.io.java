package me.joshuamarquez.sails.io;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JWRTest {

    private JWR jwr;
    private JSONObject headers = new JSONObject() {
        {
            put("Content-Type", "application/json");
        }
    };
    private JSONObject jsonObjectResponse = new JSONObject() {
        {
            put("statusCode", 200);
            put("headers", headers);
            put("body", "Hello world!");
        }
    };

    @Before
    public void setUp() throws Exception {
        jwr = new JWR(jsonObjectResponse);
    }

    @Test
    public void publicMethods() throws Exception {
        // Catch-all test to find API-breaking changes.
        assertNotNull(JWR.class.getMethod("getStatusCode"));
        assertNotNull(JWR.class.getMethod("getHeaders"));
        assertNotNull(JWR.class.getMethod("getBody"));
        assertNotNull(JWR.class.getMethod("isError"));
        assertNotNull(JWR.class.getMethod("getJsonObjectResponse"));
    }

    @Test
    public void shouldReturnStatusCodeOk() throws Exception {
        assertEquals(null, jwr.getStatusCode(), 200);
    }

    @Test
    public void shouldReturnHeaderContentType() throws Exception {
        assertEquals(jwr.getHeaders().size(), 1);
        assertEquals(jwr.getHeaders().get("Content-Type"), "application/json");
    }

    @Test
    public void getBody() throws Exception {
        assertEquals(null, jwr.getBody(), "Hello world!");
    }

    @Test
    public void errorShouldBeFalse() throws Exception {
        assertFalse(jwr.isError());
    }

    @Test
    public void shouldGetOriginalJsonObjectResponse() throws Exception {
        assertEquals(null, jwr.getJsonObjectResponse(), jsonObjectResponse);
    }

    @Test
    public void shouldReturnResponseError() throws Exception {
        JWR jwr = new JWR(new JSONObject() {
            {
                put("statusCode", 500);
                put("headers", headers);
                put("body", "Hello world!");
            }
        });
        assertTrue(jwr.isError());
    }

}