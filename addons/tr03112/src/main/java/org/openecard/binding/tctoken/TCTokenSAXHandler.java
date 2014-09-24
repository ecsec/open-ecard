/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.binding.tctoken;

import generated.TCTokenType;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Implements a SAX handler to parse TCTokens.
 *
 * @author Moritz Horsch
 */
public class TCTokenSAXHandler extends DefaultHandler {

    private static final String TC_TOKEN_TYPE = "TCTokenType";
    private static final String SERVER_ADDRESS = "ServerAddress";
    private static final String SESSION_IDENTIFIER = "SessionIdentifier";
    private static final String ERROR_ADDRESS = "CommunicationErrorAddress";
    private static final String REFRESH_ADDRESS = "RefreshAddress";
    private static final String PATH_SECURITY_PROTOCOL = "PathSecurity-Protocol";
    private static final String BINDING = "Binding";
    private static final String PATH_SECURITY_PARAMETERS = "PathSecurity-Parameters";
    private static final String PSK = "PSK";

    private boolean read;
    private StringBuilder sb;
    private List<TCToken> tokens;
    private TCToken token;

    @Override
    public void startDocument() throws SAXException {
	tokens = new ArrayList<>();
	sb = new StringBuilder(2048);
    }

    @Override
    public void endDocument() throws SAXException {
	token = null;
	read = false;
	sb.delete(0, sb.length());
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	// Consider only the TCTokens.
	if (qName.equalsIgnoreCase(TC_TOKEN_TYPE)) {
	    read = true;
	    token = new TCToken();
	} else if (qName.equalsIgnoreCase(PATH_SECURITY_PARAMETERS)) {
	    token.setPathSecurityParameters(new TCTokenType.PathSecurityParameters());
	}
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
	String value = sb.toString();
	sb.delete(0, sb.length());

	if (qName.equalsIgnoreCase(TC_TOKEN_TYPE)) {
	    tokens.add(token);
	    token = new TCToken();
	    read = false;
	} else if (qName.equalsIgnoreCase(SESSION_IDENTIFIER)) {
	    token.setSessionIdentifier(value);
	} else if (qName.equalsIgnoreCase(SERVER_ADDRESS)) {
	    if (! value.isEmpty()) {
		// correct malformed URL
		if (! value.startsWith("https://") && ! value.startsWith("http://")) {
		    // protocol relative or completely missing scheme
		    if (value.startsWith("//")) {
			value = "https:" + value;
		    } else {
			value = "https://" + value;
		    }
		}
	    }

	    token.setServerAddress(value);
	} else if (qName.equalsIgnoreCase(REFRESH_ADDRESS)) {
	    token.setRefreshAddress(value);
	} else if (qName.equalsIgnoreCase(ERROR_ADDRESS)) {
	    token.setCommunicationErrorAddress(value);
	} else if (qName.equalsIgnoreCase(PATH_SECURITY_PROTOCOL)) {
	    token.setPathSecurityProtocol(value);
	} else if (qName.equalsIgnoreCase(BINDING)) {
	    token.setBinding(value);
	} else if (qName.equalsIgnoreCase(PSK)) {
	    try {
		// check that an even number of characters (2 per byte) is present
		if ((value.length() % 2) == 0) {
		    byte[] b = StringUtils.toByteArray(value.toUpperCase());
		    token.getPathSecurityParameters().setPSK(b);
		}
	    } catch (NumberFormatException ex) {
		// too bad, verifier will see the null value and react accordingly
	    }
	}
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
	// Read only the TCToken.
	if (read) {
	    for (int i = start; i < (start + length); i++) {
		// Ignore whitespaces and control characters like line breaks.
		if (!Character.isISOControl(ch[i]) && !Character.isWhitespace(ch[i])) {
		    sb.append(ch[i]);
		}
	    }
	}
    }

    /**
     * Returns the list of TCTokens.
     *
     * @return TCTokens
     */
    public List<TCToken> getTCTokens() {
	return tokens;
    }

}
