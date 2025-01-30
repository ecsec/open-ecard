# Module TLS

Stream based extension to the Apache http-core library and a TLS based HTTP client.

Instead of being socket based, the [org.apache.http.HttpClientConnection] implementation in this package can operate directly on Java's standard [java.io.InputStream] and [java.io.OutputStream] class.

See [http-core Tutorial](https://hc.apache.org/httpcomponents-core-ga/tutorial/html/fundamentals.html)
