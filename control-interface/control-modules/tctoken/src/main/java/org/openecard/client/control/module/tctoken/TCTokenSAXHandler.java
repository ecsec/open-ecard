/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.control.module.tctoken;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.common.util.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Implements a SAX handler to parse TCTokens.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenSAXHandler extends DefaultHandler {

    private boolean read;
    private StringBuilder sb;
    private List<TCToken> tokens;
    private TCToken token;

    @Override
    public void startDocument() throws SAXException {
	tokens = new ArrayList<TCToken>();
	sb = new StringBuilder();
    }

    @Override
    public void endDocument() throws SAXException {
	token = null;
	read = false;
	sb.delete(0, sb.length());
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
	// Consider only the TCTokens.
	if (qName.equalsIgnoreCase(TCToken.TC_TOKEN_TYPE)) {
	    read = true;
	    token = new TCToken();
	} else if (qName.equalsIgnoreCase(TCToken.PathSecurityParameters.PATH_SECURITY_PARAMETERS)) {
	    token.setPathSecurityParameters(new TCToken.PathSecurityParameters());
	}
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

	String value = sb.toString();
	sb.delete(0, sb.length());

	if (qName.equalsIgnoreCase(TCToken.TC_TOKEN_TYPE)) {
	    tokens.add(token);
	    token = new TCToken();
	    read = false;
	} else if (qName.equalsIgnoreCase(TCToken.SESSION_IDENTIFIER)) {
	    token.setSessionIdentifier(value);
	} else if (qName.equalsIgnoreCase(TCToken.SERVER_ADDRESS)) {
	    try {
		//FIXME: malformed URL hack
		if (!value.startsWith("https://") && !value.startsWith("http://")) {
		    value = "https://" + value;
		}

		token.setServerAddress(new URL(value));
	    } catch (MalformedURLException e) {
		throw new SAXException("Cannot parse the parameter ServerAddress", e);
	    }
	} else if (qName.equalsIgnoreCase(TCToken.REFRESH_ADDRESS)) {
	    try {
		token.setRefreshAddress(new URL(value));
	    } catch (MalformedURLException e) {
		throw new SAXException("Cannot parse the parameter RefreshAddress", e);
	    }
	} else if (qName.equalsIgnoreCase(TCToken.PATH_SECURITY_PROTOCOL)) {
	    token.setPathSecurityProtocol(value);
	} else if (qName.equalsIgnoreCase(TCToken.BINDING)) {
	    token.setBinding(value);
	} else if (qName.equalsIgnoreCase(TCToken.PathSecurityParameters.PSK)) {
	    byte[] b = StringUtils.toByteArray(value.toUpperCase());
	    token.getPathSecurityParameters().setPSK(b);
	}

    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
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
