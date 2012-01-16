package org.openecard.client.ws.soap;

import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SOAPHeader extends SOAPElement {

    protected SOAPHeader(Element element) {
	super(element);
    }


    public Element addHeaderElement(QName elementName) throws SOAPException {
	return addChildElement(elementName);
    }

}
