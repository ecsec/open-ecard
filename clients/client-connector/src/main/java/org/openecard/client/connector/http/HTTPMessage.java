package org.openecard.client.connector.http;

import java.util.ArrayList;
import java.util.List;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.GeneralHeader;
import org.openecard.client.connector.http.header.StartLine;


/**
 * Implements a HTTP message.
 * See RFC 2616, section 4 HTTP Message.
 * See http://tools.ietf.org/html/rfc2616#section-4
 *
 * HTTP-message = Request | Response
 *
 * generic-message = start-line
 * (message-header CRLF)
 * CRLF
 * [ message-body ]
 * start-line = Request-Line | Status-Line
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPMessage {

    /**
     * Stores the general-headers of a HTTP message.
     */
    protected List<GeneralHeader> generalHeaders = new ArrayList<GeneralHeader>();
    /**
     * Stores the entity-headers of a HTTP message.
     */
    protected List<EntityHeader> entityHeaders = new ArrayList<EntityHeader>();
    /**
     * Stores the start-line of a HTTP message.
     */
    protected StartLine startLine;
    /**
     * Stores the message body of a HTTP message.
     */
    protected String messageBody;
}
