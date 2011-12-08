package org.openecard.client.ws;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface WSMarshaller {

    public Document str2doc(String docStr) throws SAXException;
    public Document str2doc(InputStream docStr) throws SAXException, IOException;
    public String doc2str(Node doc) throws TransformerException;

    public Object unmarshal(Node n) throws MarshallingTypeException, WSMarshallerException;
    public Document marshal(Object o) throws MarshallingTypeException;

    public SOAPMessage doc2soap(Document envDoc) throws SOAPException;
    public SOAPMessage add2soap(Document content) throws SOAPException;

}
