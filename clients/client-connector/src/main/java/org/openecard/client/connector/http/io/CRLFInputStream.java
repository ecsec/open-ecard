package org.openecard.client.connector.http.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CRLFInputStream extends FilterInputStream {

    private byte b1 = 0x00;
    private byte b2 = 0x00;

    /**
     * Creates a new CRLFInputStream.
     *
     * @param inputStream InputStream
     */
    public CRLFInputStream(InputStream inputStream) {
	super(inputStream);
    }

    @Override
    public int read() throws IOException {
	return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
	return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
	int res = super.read(b, off, len);

	if (res == -1) {
	    // Return -1 if the end of the stream is reached
	    return -1;
	}

	b2 = b1;
	b1 = b[b.length - 1];

	if (b1 == 0x0A) {
	    if (b2 == 0x0D) {
		// Return -2 if a CRLF is reached
		return -2;
	    }
	}

	return res;
    }
}
