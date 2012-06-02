package org.openecard.client.connector.http;

/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class HTTPConstants {

    // Carriage Return "\r"
    public static final byte CR = (byte) 0x0D;
    // Line Feed "\n"
    public static final byte LF = (byte) 0x0A;
    // Carriage Return Line Feed "\r\n"
    public static final byte[] CRLF = new byte[]{CR, LF};
    // Charset
    public static final String CHATSET = "UTF-8";
    // Version
    public static final String VERSION = "HTTP/1.1";
}
