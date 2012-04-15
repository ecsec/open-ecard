/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.richclient.activation.tctoken;

import java.util.ArrayList;
import java.util.List;
import org.openecard.client.common.util.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Implements a SAX handler to parse TCTokens.
 *
 * @author Moritz Horsch <moritz.horsch at cdc.informatik.tu-darmstadt.de>
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
	} else if (qName.equalsIgnoreCase(TCToken.PathSecurityParameter.PATH_SECURITY_PARAMETER)) {
	    token.setPathSecurityParameter(new TCToken.PathSecurityParameter());
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
	    token.setServerAddress(value);
	} else if (qName.equalsIgnoreCase(TCToken.REFRESH_ADDRESS)) {
	    token.setRefreshAddress(value);
	} else if (qName.equalsIgnoreCase(TCToken.PATH_SECURITY_PROTOCOL)) {
	    token.setPathSecurityProtocol(value);
	} else if (qName.equalsIgnoreCase(TCToken.BINDING)) {
	    token.setBinding(value);
	} else if (qName.equalsIgnoreCase(TCToken.PathSecurityParameter.PSK)) {
	    System.out.println(value);
	    byte[] b = StringUtils.toByteArray(value.toUpperCase());
	    token.getPathSecurityParameter().setPSK(b);
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
