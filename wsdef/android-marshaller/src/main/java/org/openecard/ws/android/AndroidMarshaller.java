/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

package org.openecard.ws.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WhitespaceFilter;
import org.openecard.ws.soap.MessageFactory;
import org.openecard.ws.soap.SOAPBody;
import org.openecard.ws.soap.SOAPException;
import org.openecard.ws.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;


/**
 * This class is a provisional and simple replacement for the JAXB-Marshaller
 * used in the applet and the rich client since JAXB is not available on
 * Android.
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
public class AndroidMarshaller implements WSMarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidMarshaller.class);

    public static final String XMLNS_PFX = XMLConstants.XMLNS_ATTRIBUTE;
    public static final String XMLNS_NS = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    public static final String XSI_PFX = "xsi";
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String ISO_PFX = "iso";
    public static final String ISO_NS = "urn:iso:std:iso-iec:24727:tech:schema";
    public static final String DSS_PFX = "dss";
    public static final String DSS_NS = "urn:oasis:names:tc:dss:1.0:core:schema";
    public static final String ECAPI_PFX = "ecapi";
    public static final String ECAPI_NS = "http://www.bsi.bund.de/ecard/api/1.1";

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Transformer transformer;
    private MessageFactory soapFactory;
    
    public AndroidMarshaller() {
	documentBuilderFactory = null;
	documentBuilder = null;
	transformer = null;
	soapFactory = null;
	try {
	    documentBuilderFactory = DocumentBuilderFactory.newInstance();
	    documentBuilderFactory.setNamespaceAware(true);
	    documentBuilderFactory.setIgnoringComments(true);
	    documentBuilderFactory.setExpandEntityReferences(false);
	    documentBuilder = documentBuilderFactory.newDocumentBuilder();
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	    // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
	    // "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	    soapFactory = MessageFactory.newInstance(documentBuilder);
	} catch (Exception ex) {
	    LOG.error(ex.getMessage(), ex);
	    System.exit(1); // non recoverable
	}
    }

    public static Element createElementIso(Document document, String name) {
	Element rootElement = document.createElementNS(ISO_NS, name);
	rootElement.setPrefix(ISO_PFX);
	return rootElement;
    }

    public static Element createElementDss(Document document, String name) {
	Element rootElement = document.createElementNS(DSS_NS, name);
	rootElement.setPrefix(DSS_PFX);
	return rootElement;
    }

    public static Element createElementEcapi(Document document, String name) {
	Element rootElement = document.createElementNS(ECAPI_NS, name);
	rootElement.setPrefix(ECAPI_PFX);
	return rootElement;
    }


    @Override
    public void addXmlTypeClass(Class xmlTypeClass) throws MarshallingTypeException {
	// not available in this implementation
    }

    @Override
    public void removeAllTypeClasses() {
	// not available in this implementation
    }

    @Override
    public synchronized String doc2str(Node doc) throws TransformerException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	transformer.transform(new DOMSource(doc), new StreamResult(out));
	String result;
	try {
	    result = out.toString("UTF-8");
	} catch (UnsupportedEncodingException ex) {
	    throw new TransformerException(ex);
	}
	return result;
    }

    @Override
    public Document marshal(Object o) throws MarshallingTypeException {
	return new Marshaller(documentBuilder).marshal(o);
    }


    @Override
    public synchronized Document str2doc(String docStr) throws SAXException {
	try {
	    // read dom as w3
	    StringReader strReader = new StringReader(docStr);
	    InputSource inSrc = new InputSource(strReader);
	    Document doc = documentBuilder.parse(inSrc);

	    WhitespaceFilter.filter(doc);
	    return doc;
	} catch (IOException ex) {
	    throw new SAXException(ex);
	}
    }

    @Override
    public synchronized Document str2doc(InputStream docStr) throws SAXException, IOException {
	// read dom as w3
	Document doc;
	try {
	    doc = documentBuilder.parse(docStr);
	    WhitespaceFilter.filter(doc);
	    return doc;
	} catch (IOException e) {
	    throw new SAXException(e);
	}
    }

    @Override
    public Object unmarshal(Node n) throws MarshallingTypeException, WSMarshallerException {
	Document newDoc = createDoc(n);
	String docStr;
	try {
	    docStr = doc2str(newDoc);
	} catch (TransformerException ex) {
	    throw new WSMarshallerException("Failed to transform DOM to text.", ex);
	}
	try {
	    return new Unmarshaller(documentBuilder).unmarshal(new StringReader(docStr));
	} catch (IOException | DatatypeConfigurationException | ParserConfigurationException | XmlPullParserException ex) {
	    LOG.error("Unable to unmarshal Node element.", ex);
	    throw new MarshallingTypeException(ex);
	}
    }


    @Override
    public <T> JAXBElement<T> unmarshal(Node n, Class<T> c) throws MarshallingTypeException,
	    WSMarshallerException {
	Object result = unmarshal(n);
	if (result instanceof JAXBElement) {
	    JAXBElement jaxbElem = (JAXBElement) result;
	    if (jaxbElem.getDeclaredType().equals(c)) {
		return jaxbElem;
	    }
	}
	throw new MarshallingTypeException(String.format("Invalid type requested for unmarshalling: '%s'", c));
    }

    private Document createDoc(Node n) throws WSMarshallerException {
	Document newDoc = null;
	if (n instanceof Document) {
	    newDoc = (Document) n;
	} else if (n instanceof Element) {
	    newDoc = documentBuilder.newDocument();
	    Node root = newDoc.importNode(n, true);
	    newDoc.appendChild(root);
	} else {
	    throw new WSMarshallerException("Only w3c Document and Element are accepted.");
	}
	return newDoc;
    }

    @Override
    public synchronized SOAPMessage doc2soap(Document envDoc) throws SOAPException {
	SOAPMessage msg = soapFactory.createMessage(envDoc);
	return msg;
    }

    @Override
    public synchronized SOAPMessage add2soap(Document content) throws SOAPException {
	SOAPMessage msg = soapFactory.createMessage();
	SOAPBody body = msg.getSOAPBody();
	body.addDocument(content);

	return msg;
    }

}
