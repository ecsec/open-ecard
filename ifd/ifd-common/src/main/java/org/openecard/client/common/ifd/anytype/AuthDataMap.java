package org.openecard.client.common.ifd.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.util.Helper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Helper class to make life with DIDAuthenticationDataTypes much easier.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AuthDataMap {

    private static final String isoNs = "urn:iso:std:iso-iec:24727:tech:schema";

    private final String protocol;
    private final HashMap<QName,Element> contentMap = new HashMap<QName,Element>();
    private final HashMap<QName,String> attributeMap;

    private final Document xmlDoc;

    public AuthDataMap(DIDAuthenticationDataType data) throws ParserConfigurationException {
	this.protocol = data.getProtocol();
	// read content
	List<Element> content = data.getAny();
	for (Element next : content) {
	    String name = next.getLocalName();
	    String ns   = next.getNamespaceURI();
	    QName qname = new QName(ns, name);
	    contentMap.put(qname, next);
	}
	// read other attributes
	attributeMap = new HashMap<QName,String>(data.getOtherAttributes());
	// save document so new elements can be created -- there must always be an element, or this thing won't work
	xmlDoc = content.isEmpty() ? loadXMLBuilder() : content.get(0).getOwnerDocument();
    }


    private Document loadXMLBuilder() throws ParserConfigurationException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	return builder.newDocument();
    }

    public AuthDataResponse createResponse(DIDAuthenticationDataType responseObj) {
	responseObj.setProtocol(protocol);
	return new AuthDataResponse(xmlDoc, responseObj);
    }


    public String getProtocol() {
	return protocol;
    }


    public boolean containsContent(QName qname) {
	return contentMap.containsKey(qname);
    }
    public boolean containsContent(String ns, String localName) {
	return containsContent(new QName(ns, localName));
    }
    public boolean containsContent(String localName) {
	return containsContent(new QName(isoNs, localName));
    }

    public Element getContent(QName qname) {
	return contentMap.get(qname);
    }
    public Element getContent(String ns, String localName) {
	return getContent(new QName(ns, localName));
    }
    public Element getContent(String localName) {
	return getContent(new QName(isoNs, localName));
    }

    public String getContentAsString(QName qname) {
	if (containsContent(qname)) {
	    Element content = getContent(qname);
	    String contentStr = content.getTextContent();
	    return contentStr;
	} else {
	    return null;
	}
    }
    public String getContentAsString(String ns, String localName) {
	return getContentAsString(new QName(ns, localName));
    }
    public String getContentAsString(String localName) {
	return getContentAsString(new QName(isoNs, localName));
    }

    public byte[] getContentAsBytes(QName qname) {
	if (containsContent(qname)) {
	    String content = getContentAsString(qname);
	    byte[] contentBytes = Helper.convStringWithWSToByteArray(content);
	    return contentBytes;
	} else {
	    return null;
	}
    }
    public byte[] getContentAsBytes(String ns, String localName) {
	return getContentAsBytes(new QName(ns, localName));
    }
    public byte[] getContentAsBytes(String localName) {
	return getContentAsBytes(new QName(isoNs, localName));
    }


    public boolean containsAttribute(QName qname) {
	return attributeMap.containsKey(qname);
    }
    public boolean containsAttribute(String ns, String name) {
	return containsAttribute(new QName(ns, name));
    }
    public boolean containsAttribute(String name) {
	return containsAttribute(new QName(isoNs, name));
    }

    public String getAttribute(QName qname) {
	return attributeMap.get(qname);
    }
    public String getAttribute(String ns, String name) {
	return getAttribute(new QName(ns, name));
    }
    public String getAttribute(String name) {
	return getAttribute(new QName(isoNs, name));
    }

}
