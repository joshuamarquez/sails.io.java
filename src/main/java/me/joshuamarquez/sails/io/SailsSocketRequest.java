package me.joshuamarquez.sails.io;

import org.json.JSONException;
import org.json.JSONObject;

public class SailsSocketRequest {

    private String method;
    private String url;
    private JSONObject params;
    private JSONObject headers;

    private SailsSocketResponse.Listener listener;

    private final String KEY_METHOD = "method";
    private final String KEY_URL = "url";
    private final String KEY_PARAMS = "params";
    private final String KEY_HEADERS = "headers";

    public final String METHOD_GET = "get";
    public final String METHOD_POST = "post";
    public final String METHOD_PUT = "put";
    public final String METHOD_DELETE = "delete";

    /**
     * Makes request with no params.
     *
     * @param method HTTP request method [optional]
     * @param url destination URL
     */
    public SailsSocketRequest(String method, String url, SailsSocketResponse.Listener listener) {
        this(method, url, null, null, listener);
    }

    /**
     * Makes request with params.
     *
     * @param method HTTP request method [optional]
     * @param url destination URL
     * @param params parameters to send with the request [optional]
     */
    public SailsSocketRequest(String method, String url, JSONObject params, SailsSocketResponse.Listener listener) {
        this(method, url, params, null, listener);
    }

    /**
     * Makes request with params and headers.
     *
     * @param method HTTP request method [optional]
     * @param url destination URL
     * @param params parameters to send with the request [optional]
     * @param headers headers to send with the request [optional]
     */
    public SailsSocketRequest(String method, String url, JSONObject params, JSONObject headers,
                            SailsSocketResponse.Listener listener) {
        this.method = method;
        this.url = url;

        this.listener = listener;

        if (params == null) {
            this.params = new JSONObject();
        } else {
            this.params = params;
        }

        if (headers == null) {
            this.headers = new JSONObject();
        } else {
            this.headers = headers;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public JSONObject getParams() {
        return params;
    }

    public JSONObject getHeaders() {
        return headers;
    }

    public SailsSocketResponse.Listener getListener() {
        return listener;
    }

    public JSONObject toJSONObject() throws JSONException{
        JSONObject request = new JSONObject();

        request.put(KEY_METHOD, this.method);
        request.put(KEY_URL, this.url);
        request.put(KEY_PARAMS, this.params);
        request.put(KEY_HEADERS, this.headers);

        return request;
    }

}