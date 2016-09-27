package me.joshuamarquez.sails.io;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SailsIOClient {

    public final static String SDK_VERSION_KEY = "__sails_io_sdk_version";
    public final static String SDK_VERSION_VALUE = "0.13.7";

    private SailsSocket sailsSocket;
    private static SailsIOClient instance;

    // Global Socket url
    private AtomicReference<String> url = new AtomicReference<>();

    private AtomicBoolean shouldResetNextConnection = new AtomicBoolean();

    // Global Socket options
    private AtomicReference<IO.Options> options = new AtomicReference<>(new IO.Options());

    // Global headers
    private Map<String, String> headers = Collections.emptyMap();

    private SailsIOClient() { /* No args constructor */ }

    public synchronized static SailsIOClient getInstance() {
        if (instance == null) {
            instance = new SailsIOClient();
        }
        return instance;
    }

    public SailsSocket socket() {
        if (url.get() == null) {
            throw new RuntimeException("Url must be initialized");
        }

        IO.Options nOptions = options.get();

        if (nOptions == null) {
            nOptions = new IO.Options();
        }

        boolean resetConnection = false;

        if (shouldResetNextConnection.get() && sailsSocket != null && !sailsSocket.isConnected()) {
            nOptions.forceNew = true;
            resetConnection = true;
        }

        if (sailsSocket == null || resetConnection) {
            sailsSocket = new SailsSocket(url.get(), options.get());
            shouldResetNextConnection.set(false);
        }

        return sailsSocket;
    }

    /*
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
     * @return url to connect socket
     */
    public String getUrl() {
        return url.get();
    }

    /**
     * @param url to connect socket
     */
    public void setUrl(String url) {
        if (sailsSocket != null && sailsSocket.isConnected()) {
            throw new RuntimeException("Can not change url while socket is connected");
        }

        if (url != null) this.url.set(url);
    }

    public void resetNextConnection() {
        shouldResetNextConnection.set(true);
    }

    /**
     * @return initial socket {@link IO.Options}
     */
    public IO.Options getOptions() {
        return options.get();
    }

    /**
     * @param options initial socket {@link IO.Options}
     */
    public void setOptions(IO.Options options) {
        if (sailsSocket != null && sailsSocket.isConnected()) {
            throw new RuntimeException("Can not change options while socket is connected");
        }

        if (options != null) this.options.set(options);
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
        final SailsSocketResponse.Listener listener = request.getListener();

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
