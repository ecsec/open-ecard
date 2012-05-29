package org.openecard.client.connector.io;

/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CustomStringBuilder {

    private static final String CRLF = "\r\n";
    private StringBuilder sb = new StringBuilder();

    public CustomStringBuilder() {
    }

    public void append(String str) {
	sb.append(str);
    }

    public void appendln(String str) {
	append(str);
	appendln();
    }

    public void appendln() {
	sb.append(CRLF);
    }
}
