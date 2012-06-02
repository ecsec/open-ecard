package org.openecard.client.connector.http.io;

import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openecard.client.common.util.ByteUtils;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPOutputStreamTest {

    private static final byte[] CRLF = new byte[]{(byte) 0x0D, (byte) 0x0A};
    private String testData = "Test data";

    public HTTPOutputStreamTest() {
    }

    @Test
    public void testWrite() throws Exception {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	HTTPOutputStream instance = new HTTPOutputStream(baos);

	instance.write(testData);
	instance.close();
	assertArrayEquals(testData.getBytes(), baos.toByteArray());
    }

    @Test
    public void testWriteln() throws Exception {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	HTTPOutputStream instance = new HTTPOutputStream(baos);

	instance.writeln(testData);
	instance.close();
	assertArrayEquals(ByteUtils.concatenate(testData.getBytes(), CRLF), baos.toByteArray());
    }
}
