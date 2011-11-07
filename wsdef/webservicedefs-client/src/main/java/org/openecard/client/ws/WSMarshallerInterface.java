package org.openecard.client.ws;

import java.io.IOException;
import java.io.InputStream;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface WSMarshallerInterface {

    public Document str2doc(String docStr) throws SAXException;
    public Document str2doc(InputStream docStr) throws SAXException, IOException;
    public String doc2str(Node doc) throws TransformerException;

    public Object unmarshal(Node n) throws UnsupportedDataTypeException, JAXBException;
    public Document marshal(Object o) throws JAXBException;

}
