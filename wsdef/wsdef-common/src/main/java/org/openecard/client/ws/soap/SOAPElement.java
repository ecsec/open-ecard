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

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SOAPElement {

    protected final Element element;

    protected SOAPElement(Element element) {
	this.element = element;
    }


    public List<Element> getChildElements() {
	LinkedList<Element> result = new LinkedList<Element>();
	NodeList nodes = element.getChildNodes();
	for (int i=0; i < nodes.getLength(); i++) {
	    Node n = nodes.item(i);
	    if (org.w3c.dom.Node.ELEMENT_NODE == n.getNodeType()) {
		result.add((Element)n);
	    }
	}
	return result;
    }

    public Element addChildElement(Element parent, QName elementName) throws SOAPException {
	Document doc = element.getOwnerDocument();
	// check if the document is the same
	if (doc != parent.getOwnerDocument()) {
	    throw new SOAPException("Given nodes have different owner documents.");
	}

	Element e = doc.createElementNS(elementName.getNamespaceURI(), elementName.getLocalPart());
	return (Element) parent.appendChild(e);
    }

    public Element addChildElement(QName elementName) throws SOAPException {
	return addChildElement(element, elementName);
    }

}
