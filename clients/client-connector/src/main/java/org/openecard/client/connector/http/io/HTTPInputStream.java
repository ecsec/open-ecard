package org.openecard.client.connector.http.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.connector.http.HTTPConstants;
import org.openecard.client.connector.io.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPInputStream {

    private static final Logger logger = LoggerFactory.getLogger(HTTPInputStream.class);
    private byte[] buffer;
    private int bufferPosition = 0;

    /**
     * Creates a new HTTPInputStream.
     *
     * @param inputStream InputStream
     * @throws IOException If the HTTP request is malformed
     */
    public HTTPInputStream(InputStream inputStream) throws IOException {
	fillBuffer(inputStream);
	// <editor-fold defaultstate="collapsed" desc="log request">
	logger.debug("HTTP request:\n{}", new String(buffer));
	// </editor-fold>
    }

    private synchronized void fillBuffer(InputStream inputStream) throws IOException {
	LimitedInputStream is = new LimitedInputStream(inputStream);
	CRLFInputStream cr = new CRLFInputStream(is);

	ByteArrayOutputStream bais = new ByteArrayOutputStream();
	byte[] buf = new byte[1];
	while (true) {
	    int num = cr.read(buf);

	    if (num == -1) {
		// Break because end of the stream is reached
		throw new IllegalArgumentException("Malformed HTTP request");
	    }

	    bais.write(buf);

	    if (num == -2) {
		// Break because a CRLF is reached
		num = cr.read(buf);
		bais.write(buf);
		if (num == -1) {
		    // Break because end of the stream is reached
		    throw new IllegalArgumentException("Malformed HTTP request");
		}
		num = cr.read(buf);
		bais.write(buf);
		if (num == -2) {
		    // Break because a CRLF is reached
		    break;
		}
	    }
	}


	buffer = bais.toByteArray();
    }

    private int findCRLF() {
	for (int i = bufferPosition; i < buffer.length - 1; i++) {
	    if (buffer[i] == HTTPConstants.CR) {
		if (buffer[i + 1] == HTTPConstants.LF) {
		    return i;
		}
	    }
	}
	if (bufferPosition < buffer.length - 1) {
	    return buffer.length;
	}
	return -1;
    }

    /**
     * Reads a line from the stream.
     * The end of line is indicated by a CRLF.
     *
     * @return Line
     */
    public String readLine() {
	int position = findCRLF();
	if (position != -1) {
	    byte[] line = ByteUtils.copy(buffer, bufferPosition, position - bufferPosition);
	    bufferPosition = position + 2;
	    return new String(line);
	}
	return null;
    }
}
