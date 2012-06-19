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

package org.openecard.client.ws.jaxb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WhitespaceFilter;
import org.openecard.client.ws.soap.MessageFactory;
import org.openecard.client.ws.soap.SOAPBody;
import org.openecard.client.ws.soap.SOAPException;
import org.openecard.client.ws.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class JAXBMarshaller implements WSMarshaller {

    private final JAXBContext jaxbCtx;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    // w3 factory
    private final DocumentBuilderFactory w3Factory;
    private final DocumentBuilder w3Builder;
    private final Transformer serializer;

    // soap
    private final MessageFactory soapFactory;

    public JAXBMarshaller() {
	this(new Class[0]);
    }
    
    public JAXBMarshaller(Class... additionalClasses) {
	JAXBContext tmpJaxbCtx = null;
	Marshaller tmpMarshaller = null;
	Unmarshaller tmpUnmarshaller = null;
	DocumentBuilderFactory tmpW3Factory = null;
	DocumentBuilder tmpW3Builder = null;
	Transformer tmpSerializer = null;
	MessageFactory tmpSoapFactory = null;

	try {
	    // prepare marshaller
	    Class[] jaxbClasses = getJaxbClasses();
	    LinkedList<Class> allClassesList = new LinkedList<Class>();
	    allClassesList.addAll(Arrays.asList(jaxbClasses));       // add predefined classes
	    allClassesList.addAll(Arrays.asList(additionalClasses)); // add additional classes from constructor call
	    tmpJaxbCtx = JAXBContext.newInstance(allClassesList.toArray(new Class[0]));

	    // prepare xml parser
	    // create marshaller and unmarshaller
	    tmpMarshaller = tmpJaxbCtx.createMarshaller();
	    tmpUnmarshaller = tmpJaxbCtx.createUnmarshaller();

	    // instantiate w3 stuff
	    tmpW3Factory = DocumentBuilderFactory.newInstance();
	    tmpW3Factory.setNamespaceAware(true);
	    tmpW3Factory.setIgnoringComments(true);
	    tmpW3Builder = tmpW3Factory.newDocumentBuilder();

	    TransformerFactory tfactory = TransformerFactory.newInstance();
	    tmpSerializer = tfactory.newTransformer();
	    tmpSerializer.setOutputProperty(OutputKeys.INDENT, "yes");
	    tmpSerializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            tmpSerializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    tmpSerializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	    // instantiate soap stuff
	    tmpSoapFactory = MessageFactory.newInstance();
	} catch (Exception ex) {
	    ex.printStackTrace(System.err);
	    System.exit(1); // non recoverable
	}

	jaxbCtx = tmpJaxbCtx;
	marshaller = tmpMarshaller;
	unmarshaller = tmpUnmarshaller;
	w3Factory = tmpW3Factory;
	w3Builder = tmpW3Builder;
	serializer = tmpSerializer;
	soapFactory = tmpSoapFactory;
    }

    private static Class[] getJaxbClasses() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
	List<Class> classes = new LinkedList<Class>();
        InputStream classListStream = cl.getResourceAsStream("classes.lst");
        InputStream classListStreamC = cl.getResourceAsStream("/classes.lst");

        if (classListStream == null && classListStreamC == null) {
            System.err.println("Error loading classes.lst");
        } else {
            // select the one stream that is set
            classListStream = (classListStream != null) ? classListStream : classListStreamC;

            try {
                LineNumberReader r = new LineNumberReader(new InputStreamReader(classListStream));
                String next;
                // read all entries from file
                while ((next = r.readLine()) != null) {
                    try {
                        // load class and see if it is a JAXB class
                        Class c = cl.loadClass(next);
                        if (c.getAnnotation(XmlType.class) != null) {
                            classes.add(c);
                        }
                    } catch (ClassNotFoundException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }

	return classes.toArray(new Class[classes.size()]);
    }


    ////////////////////////////////////////////////////////////////////////////
    // public functions to marshal and convert stuff
    ////////////////////////////////////////////////////////////////////////////


    public synchronized MessageFactory getSoapFactory() {
	return soapFactory;
    }

    @Override
    public synchronized Document str2doc(String docStr) throws SAXException {
        try {
            // read dom as w3
            StringReader strReader = new StringReader(docStr);
            InputSource inSrc = new InputSource(strReader);
            Document doc = w3Builder.parse(inSrc);

            WhitespaceFilter.filter(doc);

            return doc;
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    @Override
    public synchronized Document str2doc(InputStream docStr) throws SAXException, IOException {
	// read dom as w3
	Document doc = w3Builder.parse(docStr);

	WhitespaceFilter.filter(doc);

	return doc;
    }

    @Override
    public synchronized String doc2str(Node doc) throws TransformerException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	serializer.transform(new DOMSource(doc), new StreamResult(out));
	String result;
        try {
            result = out.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new TransformerException(ex);
        }
        return result;
    }

    @Override
    public synchronized Object unmarshal(Node n) throws MarshallingTypeException, WSMarshallerException {
	Document newDoc = null;
	if (n instanceof Document) {
	    newDoc = (Document) n;
	} else if(n instanceof Element) {
	    newDoc = w3Builder.newDocument();
	    Node root = newDoc.importNode(n, true);
	    newDoc.appendChild(root);
	} else {
	    throw new WSMarshallerException("Only w3c Document and Element are accepted.");
	}

	Object result;
        try {
            result = unmarshaller.unmarshal(newDoc); //NOI18N
        } catch (JAXBException ex) {
            throw new MarshallingTypeException(ex);
        }
	return result;
    }

    @Override
    public synchronized Document marshal(Object o) throws MarshallingTypeException {
	try {
	    StringWriter sw = new StringWriter();
	    XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
	    // wrap writer so specific ns prefixes are written out correctly
	    XMLStreamWriterWrapper xmlwrap = new XMLStreamWriterWrapper(xmlStreamWriter);
	    marshaller.marshal(o, xmlwrap);
	    return str2doc(sw.toString());
	} catch (Exception ex) {
	    throw new MarshallingTypeException(ex);
	}
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
