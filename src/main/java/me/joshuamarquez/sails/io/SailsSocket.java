package me.joshuamarquez.sails.io;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONObject;
import java.net.URISyntaxException;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static me.joshuamarquez.sails.io.SailsSocketRequest.*;

public class SailsSocket {

    private static final Logger logger = Logger.getLogger(SailsSocket.class.getName());

    private Socket socket;
    private IO.Options options;

    private boolean isConnecting;

    // Global headers
    private Map<String, String> headers = Collections.emptyMap();

    private Set<SailsSocketRequest> requestQueue;

    public SailsSocket(String url, IO.Options options) throws URISyntaxException {
        this.options = options;

        socket = IO.socket(url, this.options);

        requestQueue = new HashSet<SailsSocketRequest>();

        Emitter.Listener clearRequestQueue = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                drainRequestQueue();
            }
        };
        socket.once(Socket.EVENT_CONNECT, clearRequestQueue);
        socket.on(Socket.EVENT_RECONNECT, clearRequestQueue);
    }

    /**
     * Set HTTP headers to be sent in every request.
     *
     * @param headers
     */
    public void setHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers = headers;
        }
    }

    /**
     * Drains request queue sending each
     * request to {@link #emitFrom(SailsSocketRequest)}
     */
    private void drainRequestQueue() {
        synchronized (requestQueue) {
            if (!requestQueue.isEmpty()) {
                for (SailsSocketRequest request : requestQueue) {
                    emitFrom(request);
                }

                requestQueue.clear();
            }
        }
    }

    /**
     * Begin connecting private socket to the server.
     *
     * @return {@link SailsSocket}
     */
    public SailsSocket connect() {
        isConnecting = true;

        socket.connect();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnecting = false;

                logger.fine("Now connected to Sails.");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.warning("Socket was disconnected from Sails.");
            }
        }).on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.fine("Socket is trying to reconnect to Sails...");
            }
        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.fine("Socket reconnected successfully");
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnecting = false;

                logger.severe("Failed to connect socket (possibly due to failed `beforeConnect` on server)");
            }
        });

        return this;
    }

    /**
     * Reconnect the socket.
     *
     * @return {@link SailsSocket}
     */
    public SailsSocket reconnect() {
        if (this.isConnecting) {
            throw new Error("Cannot connect- socket is already connecting");
        }
        if (socket.connected()) {
            throw new Error("Cannot connect- socket is already connected");
        }
        socket.connect();

        return this;
    }

    /**
     * Disconnect the socket.
     *
     * @return {@link SailsSocket}
     */
    public SailsSocket disconnect() {
        this.isConnecting = false;

        if (!socket.connected()) {
            throw new Error("Cannot disconnect- socket is already disconnected");
        }
        socket.disconnect();

        return this;
    }

    /**
     *
     * Chainable method to unbind an event to the socket.
     *
     * @param  event event name
     * @param  fn {@link Emitter.Listener event handler listener
     * @return {@link SailsSocket}
     */
    public SailsSocket on(String event, Emitter.Listener fn) {
        socket.on(event, fn);

        return this;
    }

    /**
     *
     * Chainable method to bind an event to the socket.
     *
     * @param  event event name
     * @param  fn {@link Emitter.Listener event handler listener
     * @return {@link SailsSocket}
     */
    public SailsSocket off(String event, Emitter.Listener fn) {
        socket.off(event, fn);

        return this;
    }

    /**
     * Simulate a GET request to sails
     *
     * @param tag Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link SailsSocket#removeRequestsByTag(String)}.
     * @param url {@link String} destination URL
     * @param params {@link JSONObject} parameters to send with the request, can be null.
     * @param listener {@link SailsSocketResponse.Listener} listener to call when finished
     */
    public SailsSocket get(String tag, String url, JSONObject params, SailsSocketResponse.Listener listener) {
        request(tag, METHOD_GET, url, params, null, listener);

        return this;
    }

    /**
     * Simulate a POST request to sails
     *
     * @param tag Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link SailsSocket#removeRequestsByTag(String)}.
     * @param url {@link String} destination URL
     * @param params {@link JSONObject} parameters to send with the request, can be null.
     * @param listener {@link SailsSocketResponse.Listener} listener to call when finished
     */
    public SailsSocket post(String tag, String url, JSONObject params, SailsSocketResponse.Listener listener) {
        request(tag, METHOD_POST, url, params, null, listener);

        return this;
    }

    /**
     * Simulate a PUT request to sails
     *
     * @param tag Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link SailsSocket#removeRequestsByTag(String)}.
     * @param url {@link String} destination URL
     * @param params {@link JSONObject} parameters to send with the request, can be null.
     * @param listener {@link SailsSocketResponse.Listener} listener to call when finished
     */
    public SailsSocket put(String tag, String url, JSONObject params, SailsSocketResponse.Listener listener) {
        request(tag, METHOD_PUT, url, params, null, listener);

        return this;
    }

    /**
     * Simulate a DELETE request to sails
     *
     * @param tag Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link SailsSocket#removeRequestsByTag(String)}.
     * @param url {@link String} destination URL
     * @param params {@link JSONObject} parameters to send with the request, can be null.
     * @param listener {@link SailsSocketResponse.Listener} listener to call when finished
     */
    public SailsSocket delete(String tag, String url, JSONObject params, SailsSocketResponse.Listener listener) {
        request(tag, METHOD_DELETE, url, params, null, listener);

        return this;
    }

    /**
     * Simulate an HTTP request to sails
     *
     * @param tag Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link SailsSocket#removeRequestsByTag(String)}.
     * @param method {@link String} HTTP request method
     * @param url {@link String} destination URL
     * @param params {@link JSONObject} parameters to send with the request, can be null.
     * @param headers {@link Map} headers to be sent with the request, can be null.
     * @param listener {@link SailsSocketResponse.Listener} listener to call when finished
     */
    public SailsSocket request(String tag, String method, String url, JSONObject params, Map<String, String> headers,
                               SailsSocketResponse.Listener listener) {
        Map<String, String> requestHeaders = new HashMap<String, String>();

        // Merge global headers in
        if (headers != null && !headers.isEmpty()) {
            // Merge global headers into requestHeaders
            requestHeaders.putAll(this.headers);

            // Merge request headers headers into requestHeaders
            requestHeaders.putAll(headers);
        }

        // Build request
        SailsSocketRequest request =
                new SailsSocketRequest(tag, method, url, params, new JSONObject(requestHeaders), listener);

        // If this socket is not connected yet, queue up this request
        // instead of sending it (so it can be replayed when the socket comes online.)
        if (!socket.connected()) {
            synchronized (requestQueue) {
                requestQueue.add(request);
            }
        } else {
            emitFrom(request);
        }

        return this;
    }

    /**
     * Private method used by {@link SailsSocket#request}
     *
     * @param request {@link SailsSocketRequest}
     */
    private void emitFrom(SailsSocketRequest request) {
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

    /**
     * Removes all requests in this queue with the given tag.
     */
    protected void removeRequestsByTag(final String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag cannot be null");
        }

        synchronized (requestQueue) {
            for (SailsSocketRequest request : requestQueue) {
                if (request.getTag().equals(tag)) {
                    requestQueue.remove(request);
                }
            }
        }
    }

    /**
     * Removes all pending request in queue.
     */
    protected void removeAllRequests() {
        synchronized (requestQueue) {
            requestQueue.clear();
        }
    }

}
