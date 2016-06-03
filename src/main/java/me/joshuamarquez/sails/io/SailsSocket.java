package me.joshuamarquez.sails.io;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import io.socket.engineio.client.Transport;
import org.json.JSONObject;

import java.net.URISyntaxException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.joshuamarquez.sails.io.SailsSocketRequest.*;
import static me.joshuamarquez.sails.io.SailsIOClient.*;

public class SailsSocket {

    private static final Logger logger = Logger.getLogger(SailsSocket.class.getName());

    private Socket socket;
    private IO.Options options = new IO.Options();

    private boolean isConnecting;

    // Socket headers
    private Map<String, String> headers = Collections.emptyMap();

    private Set<SailsSocketRequest> requestQueue;

    public SailsSocket(String url) {
        this(url, null);
    }

    public SailsSocket(String url, IO.Options options) {
        // Set logger level to FINE
        logger.setLevel(Level.FINE);

        if (options != null) this.options = options;

        /**
         * Solves problem: "Sails v0.11.x is not compatible with the socket.io/sails.io.js
         * client SDK version you are using (0.9.0). Please see the v0.11 migration guide
         * on http://sailsjs.org for more information".
         *
         * https://github.com/balderdashy/sails/issues/2640
         */
        String sdkVersionQuery = String.join("=", SDK_VERSION_KEY, SDK_VERSION_VALUE);
        if (this.options.query == null) {
            this.options.query = sdkVersionQuery;
        } else {
            this.options.query = String.join("&", this.options.query, sdkVersionQuery);
        }

        try {
            socket = IO.socket(url, this.options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        requestQueue = new HashSet<SailsSocketRequest>();

        Emitter.Listener clearRequestQueue = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                drainRequestQueue();
            }
        };
        socket.on(Socket.EVENT_CONNECT, clearRequestQueue);
        socket.on(Socket.EVENT_RECONNECT, clearRequestQueue);
    }

    /**
     * Get headers to be sent in every request for this socket.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Set headers to be sent in every request for this socket.
     *
     * @param headers socket request headers
     * @return {@link SailsSocket}
     */
    public SailsSocket setHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers = headers;
        }

        return this;
    }

    /**
     * @param initialHeaders initial headers to be send on connection
     * @return {@link SailsSocket}
     */
    public SailsSocket setInitialConnectionHeaders(Map<String, List<String>> initialHeaders) {
        // Called upon transport creation.
        socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport)args[0];

                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        @SuppressWarnings("unchecked")
                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
                        // modify request headers
                        if (initialHeaders != null && !initialHeaders.isEmpty()) {
                            headers.putAll(initialHeaders);
                        }
                    }
                });
            }
        });

        return this;
    }

    /**
     * Drains request queue sending each
     * request to {@link SailsIOClient#emitFrom(Socket, SailsSocketRequest)}
     */
    private void drainRequestQueue() {
        synchronized (requestQueue) {
            if (!requestQueue.isEmpty()) {
                logger.fine("Draining request queue");

                for (SailsSocketRequest request : requestQueue) {
                    SailsIOClient.getInstance().emitFrom(socket, request);
                }

                requestQueue.clear();
            }
        }
    }

    /**
     * Begin connecting private socket to the server
     * with initial connection headers.
     *
     * @return {@link SailsSocket}
     */
    public SailsSocket connect(Map<String, List<String>> initialHeaders) {
        setInitialConnectionHeaders(initialHeaders);
        return connect();
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
            throw new RuntimeException("Cannot connect- socket is already connecting");
        }
        if (isConnected()) {
            throw new RuntimeException("Cannot connect- socket is already connected");
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

        if (!isConnected()) {
            throw new RuntimeException("Cannot disconnect- socket is already disconnected");
        }
        socket.disconnect();

        return this;
    }

    /**
     * @return whether socket is connected or not.
     */
    public boolean isConnected() {
        return socket.connected();
    }

    /**
     * Returns Socket.IO instance
     *
     * @return {@link Socket}
     */
    public Socket getSocket() {
        return socket;
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

        /**
         * Merge Globals, Socket headers and Request Headers into requestHeaders
         */

        // Merge Global headers
        requestHeaders.putAll(SailsIOClient.getInstance().getHeaders());

        // Merge Socket headers
        requestHeaders.putAll(this.headers);

        // Merge Request headers
        if (headers != null && !headers.isEmpty()) {
            requestHeaders.putAll(headers);
        }

        // Build request
        SailsSocketRequest request =
                new SailsSocketRequest(tag, method, url, params, new JSONObject(requestHeaders), listener);

        // If this socket is not connected yet, queue up this request
        // instead of sending it (so it can be replayed when the socket comes online.)
        if (!isConnected()) {
            synchronized (requestQueue) {
                requestQueue.add(request);
            }
        } else {
            SailsIOClient.getInstance().emitFrom(socket, request);
        }

        return this;
    }

    /**
     * Removes all requests in this queue with the given tag.
     */
    public void removeRequestsByTag(final String tag) {
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
    public void removeAllRequests() {
        synchronized (requestQueue) {
            requestQueue.clear();
        }
    }

}
