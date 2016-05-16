package me.joshuamarquez.sails.io;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The JWR (JSON WebSocket Response) received from a Sails server.
 */
public class JWR {

    private int statusCode;
    private Map<String, String> headers;
    private String body;

    private JSONObject jsonObjectResponse;

    /**
     * @param response {@link Object}
     *         => :statusCode
     *         => :body
     *         => :headers
     */
    public JWR(JSONObject response) {
        try {
            jsonObjectResponse = response;

            statusCode = jsonObjectResponse.getInt("statusCode");

            headers = new HashMap<String, String>();

            JSONObject jsonHeaders = jsonObjectResponse.getJSONObject("headers");
            Iterator<?> keys = jsonHeaders.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                headers.put(key, jsonHeaders.getString(key));
            }

            body = jsonObjectResponse.getString("body");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public boolean isError() {
        return this.statusCode < 200 || this.statusCode >= 400;
    }

    public JSONObject getJsonObjectResponse() {
        return jsonObjectResponse;
    }

    @Override
    public String toString() {
        return String.format("Status :: %d\nHeaders Count :: %d\nBody :: %s",
                statusCode, headers.size(), body);
    }
}
