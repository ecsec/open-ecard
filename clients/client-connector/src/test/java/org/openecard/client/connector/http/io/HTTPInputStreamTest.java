/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.connector.http.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPInputStreamTest {

    private static final byte[] CRLF = new byte[]{(byte) 0x0D, (byte) 0x0A};

    public HTTPInputStreamTest() {
    }

    @Test
    public void testReadLine() throws IOException {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	baos.write("GET /test.html HTTP/1.1".getBytes());
	baos.write(CRLF);
	baos.write("Allow: text/html".getBytes());
	baos.write(CRLF);
	baos.write(CRLF);

	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	HTTPInputStream instance = new HTTPInputStream(bais);

	assertEquals("GET /test.html HTTP/1.1", instance.readLine());
	assertEquals("Allow: text/html", instance.readLine());
	assertEquals("", instance.readLine());
	assertNull(instance.readLine());
	assertNull(instance.readLine());
    }
}
