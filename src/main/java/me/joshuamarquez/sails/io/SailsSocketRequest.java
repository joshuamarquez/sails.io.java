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

    public final static String METHOD_GET = "get";
    public final static String METHOD_POST = "post";
    public final static String METHOD_PUT = "put";
    public final static String METHOD_DELETE = "delete";

    private String tag;

    /**
     * Makes request with no params.
     *
     * @param method HTTP request method [optional]
     * @param url destination URL
     */
    public SailsSocketRequest(String tag, String method, String url, SailsSocketResponse.Listener listener) {
        this(tag, method, url, null, null, listener);
    }

    /**
     * Makes request with params.
     *
     * @param method HTTP request method [optional]
     * @param url destination URL
     * @param params parameters to send with the request [optional]
     */
    public SailsSocketRequest(String tag, String method, String url, JSONObject params,
                              SailsSocketResponse.Listener listener) {
        this(tag, method, url, params, null, listener);
    }

    /**
     * Makes request with params and headers.
     *
     * @param method HTTP request method [optional]
     * @param url destination URL
     * @param params parameters to send with the request [optional]
     * @param headers headers to send with the request [optional]
     */
    public SailsSocketRequest(String tag, String method, String url, JSONObject params, JSONObject headers,
                            SailsSocketResponse.Listener listener) {
        if (params == null) {
            this.params = new JSONObject();
        }

        if (headers == null) {
            this.headers = new JSONObject();
        }

        this.tag = tag;
        this.method = method;
        this.url = url;
        this.params = params;
        this.headers = headers;
        this.listener = listener;
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

    public String getTag() {
        return tag;
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