package org.openecard.client.connector.http.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openecard.client.connector.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class HTTPOutputStream {

    private static final Logger logger = LoggerFactory.getLogger(HTTPOutputStream.class);
    private ByteArrayOutputStream buffer;
    private OutputStream outputStream;

    /**
     * Creates a new HTTPOutputStream.
     *
     * @param outputStream OutputStream
     */
    public HTTPOutputStream(OutputStream outputStream) {
	this.outputStream = outputStream;
	this.buffer = new ByteArrayOutputStream();
    }

    /**
     * Writes the data to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void write(byte[] data) throws IOException {
	buffer.write(data);
    }

    /**
     * Writes the data to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void write(String data) throws IOException {
	write(data.getBytes(HTTPConstants.CHATSET));
    }

    /**
     * Writes the data to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void write(int data) throws IOException {
	write(String.valueOf(data));
    }

    /**
     * Writes a CRLF to the stream.
     *
     * @throws IOException
     */
    public void writeln() throws IOException {
	write(HTTPConstants.CRLF);
    }

    /**
     * Writes the data and appends a CRLF to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public synchronized void writeln(byte[] data) throws IOException {
	write(data);
	writeln();
    }

    /**
     * Writes the data and appends a CRLF to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void writeln(String data) throws IOException {
	writeln(data.getBytes(HTTPConstants.CHATSET));
    }

    /**
     * Writes the data and appends a CRLF to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void writeln(int data) throws IOException {
	writeln(String.valueOf(data));
    }

    /**
     * Closes the stream.
     *
     * @throws IOException
     */
    public synchronized void close() throws IOException {
	// <editor-fold defaultstate="collapsed" desc="log response">
	logger.debug("HTTP response:\n{}", new String(buffer.toByteArray()));
	// </editor-fold>
	outputStream.write(buffer.toByteArray());
	outputStream.flush();
	outputStream.close();
    }
}
