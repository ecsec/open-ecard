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

package org.openecard.ws.soap

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilder

/**
 *
 * @author Tobias Wich
 */
class SOAPMessage {
	val document: Document
	private val namespace: String

	val soapEnvelope: SOAPEnvelope
	val soapHeader: SOAPHeader
	val soapBody: SOAPBody

	internal constructor(docBuilder: DocumentBuilder, namespace: String) {
		document = docBuilder.newDocument()
		this.namespace = namespace

		// add envelope and that stuff
		val envElem = document.createElementNS(namespace, "Envelope")
		soapEnvelope = SOAPEnvelope(envElem)
		document.appendChild(envElem)
		val headElem = soapEnvelope.addChildElement(QName(namespace, "Header"))
		soapHeader = SOAPHeader(headElem)
		val bodyElem = soapEnvelope.addChildElement(QName(namespace, "Body"))
		soapBody = SOAPBody(bodyElem)
	}

	internal constructor(doc: Document) {
		this.document = doc
		val envElem =
			doc.firstChild as Element?
				?: throw SOAPException("No Envelope element in SOAP message.")
		soapEnvelope = SOAPEnvelope(envElem)

		namespace = MessageFactory.verifyNamespace(envElem.namespaceURI)

		// extract envelope and stuff from doc
		var headElem: Element? = null
		var bodyElem: Element? = null

		// extract info
		val nodes = envElem.childNodes
		for (i in 0 until nodes.length) {
			val n = nodes.item(i)
			if (n.nodeType == Node.ELEMENT_NODE) {
				val e = n as Element
				if (e.namespaceURI == namespace) {
					// head is next
					if (headElem == null && bodyElem == null && "Header" == e.localName) {
						headElem = e
					} else if (bodyElem == null && "Body" == e.localName) {
						bodyElem = e
					} else {
						throw SOAPException("Undefined element (" + e.localName + ") in SOAP message.")
					}
				} else {
					throw SOAPException("Undefined namespace (" + e.namespaceURI + ") in SOAP message.")
				}
			} else if (n.nodeType == Node.TEXT_NODE || n.nodeType == Node.CDATA_SECTION_NODE) {
				// throw new SOAPException("Undefined node type in SOAP message.");
				println("Undefined node type in SOAP message: " + n.nodeType + n.nodeName + n.nodeValue + n.textContent)
			}
		}

		// check if all info is present, else create it
		if (bodyElem == null) {
			throw SOAPException("No Body element present in SOAP message.")
		}
		if (headElem == null) {
			headElem = doc.createElementNS(namespace, "Header")
			headElem = envElem.insertBefore(headElem, bodyElem) as Element
		}

		soapHeader = SOAPHeader(headElem)
		soapBody = SOAPBody(bodyElem)
	}
}
