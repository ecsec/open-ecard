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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SOAPMessage {

    private final Document doc;
    private final String namespace;

    private final SOAPEnvelope env;
    private final SOAPHeader head;
    private final SOAPBody body;

    protected SOAPMessage(DocumentBuilder docBuilder, String namespace) throws SOAPException {
	doc = docBuilder.newDocument();
	this.namespace = namespace;

	// add envelope and that stuff
	Element envElem = doc.createElementNS(namespace, "Envelope");
	env = new SOAPEnvelope(envElem);
	doc.appendChild(envElem);
	Element headElem = env.addChildElement(new QName(namespace, "Header"));
	head = new SOAPHeader(headElem);
	Element bodyElem = env.addChildElement(new QName(namespace, "Body"));
	body = new SOAPBody(bodyElem);
    }

    protected SOAPMessage(Document doc) throws SOAPException {
	this.doc = doc;
	Element envElem = (Element) doc.getFirstChild();
	if (envElem == null) {
	    throw new SOAPException("No Envelope element in SOAP message.");
	}
	env = new SOAPEnvelope(envElem);

	namespace = MessageFactory.verifyNamespace(envElem.getNamespaceURI());

	// extract envelope and stuff from doc
	boolean headProcessed = false;
	Element headElem = null;
	boolean bodyProcessed = false;
	Element bodyElem = null;

	// extract info
	NodeList nodes = envElem.getChildNodes();
	for (int i=0; i < nodes.getLength(); i++) {
	    Node n = nodes.item(i);
	    if (n.getNodeType() == Node.ELEMENT_NODE) {
		Element e = (Element) n;
		if (e.getNamespaceURI().equals(namespace)) {
		    // head is next
		    if (!headProcessed && !bodyProcessed && "Header".equals(e.getLocalName())) {
			headProcessed = true;
			headElem = e;
		    } else if (!bodyProcessed && "Body".equals(e.getLocalName())) {
			bodyProcessed = true;
			bodyElem = e;
		    } else {
			throw new SOAPException("Undefined element (" + e.getLocalName() + ") in SOAP message.");
		    }
		} else {
		    throw new SOAPException("Undefined namespace (" + e.getNamespaceURI() + ") in SOAP message.");
		}
	    } else if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
		throw new SOAPException("Undefined node type in SOAP message.");
	    }
	}

	// check if all info is present, else create it
	if (! bodyProcessed) {
	    throw new SOAPException("No Body element present in SOAP message.");
	}
	if (! headProcessed) {
	    headElem = doc.createElementNS(namespace, "Header");
	    headElem = (Element) envElem.insertBefore(headElem, bodyElem);
	}

	head = new SOAPHeader(headElem);
	body = new SOAPBody(bodyElem);
    }


    public Document getDocument() {
	return doc;
    }

    public SOAPEnvelope getSOAPEnvelope() {
	return env;
    }

    public SOAPHeader getSOAPHeader() {
	return head;
    }

    public SOAPBody getSOAPBody() {
	return body;
    }

}
