package me.joshuamarquez.sails.io;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

public class SailsIOClient {

    public final static String SDK_VERSION_KEY = "__sails_io_sdk_version";
    public final static String SDK_VERSION_VALUE = "0.13.7";

    private static SailsSocket globalSailsSocket;
    private static SailsIOClient instance;

    // Global headers
    private Map<String, String> headers = Collections.emptyMap();

    private SailsIOClient() { /* No args constructor */ }

    public synchronized static SailsIOClient getInstance() {
        if (instance == null) {
            instance = new SailsIOClient();
        }

        return instance;
    }

    public synchronized SailsSocket socket(String url, IO.Options options) throws URISyntaxException {
        if (globalSailsSocket == null) {
            globalSailsSocket = new SailsSocket(url, options);
        }

        return globalSailsSocket;
    }

    /**
     * Get HTTP headers to be sent in every request for all sockets.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @param headers HTTP headers to be sent in every request for all sockets.
     */
    public void setHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers = headers;
        }
    }

    /**
     * Private method used by {@link SailsSocket#request}
     *
     * @param request {@link SailsSocketRequest}
     */
    void emitFrom(Socket socket, SailsSocketRequest request) {
        // Name of the appropriate socket.io listener on the server
        // ( === the request method or "verb", e.g. 'get', 'post', 'put', etc. )
        String sailsEndpoint = request.getMethod();

        // Since Listener is embedded in request, retrieve it.
        SailsSocketResponse.Listener listener = request.getListener();

        socket.emit(sailsEndpoint, request.toJSONObject(), new Ack() {
            @Override
            public void call(Object... args) {
                // Send back jsonWebSocketResponse
                if (listener != null) {
                    listener.onResponse(new JWR((JSONObject) args[0]));
                }
            }
        });
    }
}
