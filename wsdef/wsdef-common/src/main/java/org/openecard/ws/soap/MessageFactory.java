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

package org.openecard.ws.soap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 *
 * @author Tobias Wich
 */
public class MessageFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MessageFactory.class);

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
	    final DocumentBuilderFactory tmpW3Factory = DocumentBuilderFactory.newInstance();
	    tmpW3Factory.setNamespaceAware(true);
	    tmpW3Factory.setIgnoringComments(true);
	    tmpW3Factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	    // XXE countermeasures
	    tmpW3Factory.setExpandEntityReferences(false);
	    tmpW3Factory.setXIncludeAware(false);
	    tmpW3Factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	    try {
		tmpW3Factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
	    } catch (ParserConfigurationException ex) {
		LOG.warn("Failed to disallow DTDs entirely.");
	    }
	    tmpW3Factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
	    tmpW3Factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

	    final DocumentBuilder tmpW3Builder = tmpW3Factory.newDocumentBuilder();

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
