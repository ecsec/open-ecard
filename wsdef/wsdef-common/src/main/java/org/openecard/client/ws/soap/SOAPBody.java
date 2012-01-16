package org.openecard.client.ws.soap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SOAPBody extends SOAPElement {

    protected SOAPBody(Element element) {
	super(element);
    }


    public void addDocument(Document document) throws SOAPException {
	Document doc = element.getOwnerDocument();
	Node newNode = doc.importNode(document.getDocumentElement(), true);
	element.appendChild(newNode);
    }

}
