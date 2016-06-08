# sails.io.java

This is an implementation of [sails.io.js](https://github.com/balderdashy/sails.io.js) in Java for communicating w/ [Sails](https://github.com/balderdashy/sails/) via sockets

## Installation

### Gradle

Add it as a gradle dependency, in `build.gradle`.

```groovy
compile 'me.joshuamarquez:sails.io.java:0.2.2'
```

### Maven

Add the following dependency to your `pom.xml`.

```xml
<dependency>
  <groupId>me.joshuamarquez</groupId>
  <artifactId>sails.io.java</artifactId>
  <version>0.2.2</version>
  <type>pom</type>
</dependency>
```

## Usage

#### Use global socket instance

```java
// Get global socket
SailsIOClient.getInstance().setUrl("http://localhost:1337");
SailsSocket sailsSocket = SailsIOClient.getInstance().socket();

// Connect global socket
sailsSocket.connect();

// Make GET request
sailsSocket.get("MyTAG", "/path", null, new SailsSocketResponse.Listener() {
    @Override
    public void onResponse(JWR response) {
        if (response.isError()) {
            // hadle error
        }

        System.out.println(respose.getBody().toString());
    }
});
```

**Note: A tag should be provided to each request in order to be possible to remove requests later if they are in request queue due to lack of connection.**

#### Create new socket instance

```java
SailsSocket sailsSocket = new SailsSocket(url);
sailsSocket.connect();
```

#### Set Global Socket Options

Set query options for global socket initial connection.

```java
SailsIOClient.getInstance().setOptions(new IO.Options(){ { query = "token=yQ29sFHAqw4.."; } });
```

## Properties

### Headers

Dictionary of headers to be sent by default with every request. Can be overridden the headers option in `.request()`.

Create headers.

```java
private Map<String, String> headers = new HashMap<String, String>() {
    {
        put("Accept", "application/json");
        put("x-header", "bar");
    }
};
```

#### Setting global headers

This headers will be append to all socket requests.

```java
// Set global socket headers
SailsIOClient.getInstance().setHeaders(headers);
```

#### Setting socket headers

This headers will be append to all requests made by this socket.

```java
// Set socket headers
SailsSocket sailsSocket = new SailsSocket(url, opts);
sailsSocket.setHeaders(headers);
```

#### Setting request headers

You can set or override global headers and socket headers by request.

```java
Map<String> headers = new HashMap<String, String>();
headers.put("x-test-header-one", "baz");

SailsSocket sailsSocket = new SailsSocket(url, opts);
sailsSocket.request("MyTAG", SailsSocketRequest.METHOD_GET, "/path", null, headers,
    new SailsSocketResponse.Listener() {
        @Override
        public void onResponse(JWR response) { }
    });
```

### Initial connection headers

Dictionary of headers to be sent with the initial connection to the server.

In server code, these can be accessed via `req.socket.handshake.headers` in controller actions or `socket.handshake.headers` in [socket lifecycle callbacks.](http://sailsjs.org/documentation/reference/configuration/sails-config-sockets)

```java
SailsSocket sailsSocket = new SailsSocket(url, opts);
sailsSocket.get("MyTAG", "/path", null, new SailsSocketResponse.Listener() {
    @Override
    public void onResponse(JWR response) { }
});

// Set iniaitialConnectionHeaders
sailsSocket.connect(new HashMap<String, List<String>>() {
    {
        put("cookie", Arrays.asList("foo=1;"));
    }
});
```

### Query

Query string to use with the initial connection to the server.

In server code, this can be accessed via `req.socket.handshake.query` in controller actions or `socket.handshake.query` in [socket lifecycle callbacks.](http://sailsjs.org/documentation/reference/configuration/sails-config-sockets)

```java
// Set query options
IO.Options options = new IO.Options();
options.query = "x-query={\"foo\":\"bar\"}";

SailsSocket sailsSocket = new SailsSocket(url, options);
sailsSocket.get("MyTAG", "/path", null, new SailsSocketResponse.Listener() {
    @Override
    public void onResponse(JWR response) { }
});
```

## SailsSocket Methods

### Basic methods

`.get`, `.post`, `.put` and `.delete` request methods are available. All request should include in this order:

* `TAG`: Set a tag on this request. Can be used to cancel all requests with this tag.
* `url`: destination URL.
* `params`: parameters to send with the request, can be `null`.
* `listener`: listener to call with the response when finished.

`.on()` and `.off()` [socket.io-client-java](https://github.com/socketio/socket.io-client-java) methods are also available.

All methods above use `.request(tag, method, url, params, headers, listener)` internally to build their request.

### Advanced methods

In addition to the basic communication / event listening methods, each SailsSocket instance exposes some additional methods.

#### `.removeRequestsByTag()`

Removes all requests in this queue with the given tag.

```java
sailsSocket.removeRequestsByTag("MyTAG");
```

#### `.removeAllRequests()`

Removes all pending request in queue.

```java
sailsSocket.removeAllRequests();
```

#### `.getSocket()`

Returns Socket.IO instance of SailsSocket.

```java
sailsSocket.getSocket();
```

#### `.isConnected()`

Determines whether the SailsSocket instance is currently connected to a server, returning true if a connection has been established.

```java
sailsSocket.isConnected();
```

#### `.disconnect()`

Disconnect a SailsSocket instance from the server. Will throw an error if the socket is already disconnected.

```java
sailsSocket.disconnect();
```

#### `.reconnect()`

Reconnect a SailsSocket instance to a server after being disconnected.

```java
sailsSocket.reconnect();
```

## JWR

The JWR (JSON WebSocket Response) received from a Sails server.

#### `.isError()`

`true` if response comes with status code of error type.

#### `.getStatusCode()`

Returns response status code as `int`.

#### `.getHeaders()`

Response headers `Map<String, String>`.

#### `.getBody()`

Response body as `Object`.

Example:

```java
sailsSocket.get("MyTAG", "/path", null, new SailsSocketResponse.Listener() {
    @Override
    public void onResponse(JWR response) {
        if (!response.isError()) {
          // success!
        } else {
            // Show Error
            System.out.println(String.format("Status: %d, Response: %s", response.getStatusCode(),
                response.getBody().toString()));
        }
    }
});
```

## TODO

* Overload socket RESTful methods to make `params` optional.
* Make tests for socket request queue.
* Gson support in JWR body
* Make `URL` and `Options` behave like headers (Global, Socket).

## License

MIT
