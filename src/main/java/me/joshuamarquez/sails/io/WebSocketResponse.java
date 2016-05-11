package me.joshuamarquez.sails.io;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WebSocketResponse {

    private int statusCode;
    private Map<String, String> headers;
    private Object body;

    public WebSocketResponse(Object response) {
        JSONObject jsonResponse = (JSONObject) response;

        try {
            statusCode = jsonResponse.getInt("statusCode");

            headers = new HashMap<String, String>();

            JSONObject jsonHeaders = jsonResponse.getJSONObject("headers");
            Iterator<?> keys = jsonHeaders.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                headers.put(key, jsonHeaders.getString(key));
            }

            body = jsonResponse.get("body");
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

    public Object getBody() {
        return body;
    }

}