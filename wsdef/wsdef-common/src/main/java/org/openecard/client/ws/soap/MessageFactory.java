/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ws.soap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MessageFactory {

    private final String protocol;
    private final DocumentBuilder domBuilder;

    private MessageFactory(String protocol, DocumentBuilder domBuilder) {
	this.protocol = protocol;
	this.domBuilder = domBuilder;
    }


    public static MessageFactory newInstance() throws SOAPException {
	return newInstance(SOAPConstants.DEFAULT_SOAP_PROTOCOL);
    }

    public static MessageFactory newInstance(DocumentBuilder domBuilder) throws SOAPException {
	return newInstance(SOAPConstants.DEFAULT_SOAP_PROTOCOL, domBuilder);
    }

    public static MessageFactory newInstance(String protocol) throws SOAPException {
	try {
	    DocumentBuilderFactory tmpW3Factory = DocumentBuilderFactory.newInstance();
	    tmpW3Factory.setNamespaceAware(true);
	    tmpW3Factory.setIgnoringComments(true);
	    DocumentBuilder tmpW3Builder = tmpW3Factory.newDocumentBuilder();

	    return newInstance(protocol, tmpW3Builder);
	} catch (ParserConfigurationException ex) {
	    throw new SOAPException(ex);
	}
    }

    public static MessageFactory newInstance(String protocol, DocumentBuilder domBuilder) throws SOAPException {
	return new MessageFactory(protocol, domBuilder);
    }


    public SOAPMessage createMessage() throws SOAPException {
	SOAPMessage msg = new SOAPMessage(domBuilder, MessageFactory.getNamespace(protocol));
	return msg;
    }

    public SOAPMessage createMessage(Document doc) throws SOAPException {
	SOAPMessage msg = new SOAPMessage(doc);
	return msg;
    }

    protected static String getNamespace(String protocol) throws SOAPException {
	if (protocol.equals(SOAPConstants.SOAP_1_1_PROTOCOL)) {
	    return SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE;
	} else if (protocol.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
	    return SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
	} else {
	    throw new SOAPException("Unsupported SOAP protocol.");
	}
    }

    protected static String verifyNamespace(String namespace) throws SOAPException {
	if (namespace.equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
	    return namespace;
	} else if (namespace.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
	    return namespace;
	} else {
	    throw new SOAPException("Unsupported SOAP protocol.");
	}
    }

}
