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

package org.openecard.ws.marshal;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;
import org.openecard.ws.soap.SOAPException;
import org.openecard.ws.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * Interface for a JAXB type based marshaller and unmarshaller, as well as XML document converters and SOAP helpers.
 * After its creation, the WSMarshaller instance supports a predefined classes it can marshal and unmarshal.
 *
 * @author Tobias Wich
 */
public interface WSMarshaller {

    /**
     * Add the given class to the marshaller and unmarshaller, so that it can emit and consume instances of the type.
     * The given class must be a JAXB element type, meaning it must contain a class level annotation of type {@link
     * javax.xml.bind.annotation.XmlElement}.
     * <p>
     * An implementation may ignore this function if it supports all types needed inside this implementation. The JAXB
     * implementation has no problem, so on the desktop this is no problem.
     *
     * @param xmlTypeClass Class of the JAXB element type.
     * @throws MarshallingTypeException If the type can not be added.
     */
    void addXmlTypeClass(Class<?> xmlTypeClass) throws MarshallingTypeException;
    /**
     * Remove all JAXB element types from this instance.
     * New types must be added first before this instance is usable for marshalling and unmarshalling again.
     */
    void removeAllTypeClasses();

    /**
     * Converts a string containing an XML document into a DOM document.
     * If the string does not contain a preamble, UTF-8 is assumed to be the encoding of the document.
     *
     * @param docStr String containing the XML document.
     * @return DOM instance of the given XML document.
     * @throws SAXException If the XML document contains errors.
     */
    Document str2doc(String docStr) throws SAXException;
    /**
     * Converts an InputStream containing an XML document into a DOM document.
     * If the stream does not contain a preamble, UTF-8 is assumed to be the encoding of the document.
     *
     * @param docStr InputStream containing the XML document.
     * @return DOM instance of the given XML document.
     * @throws SAXException If the XML document contains errors.
     * @throws java.io.IOException If the stream produced an error while reading.
     */
    Document str2doc(InputStream docStr) throws SAXException, IOException;
    /**
     * Converts a DOM node into a string containing the XML document.
     * The resulting string will contain a preamble with encoding set to UTF-8.
     *
     * @param doc The DOM node which should be converted.
     * @return String containing the XML document.
     * @throws TransformerException If the XML document could not be serialized.
     */
    String doc2str(Node doc) throws TransformerException;

    /**
     * Unmarshal the given document node.
     *
     * @param n The DOM node to unmarshal.
     * @return The JAXB object representing the given DOM node.
     * @throws MarshallingTypeException If the given node represents an unsupported JAXB type.
     * @throws WSMarshallerException If the given node is neither a {@link org.w3c.dom.Document}, nor an {@link
     *   org.w3c.dom.Element}.
     */
    Object unmarshal(Node n) throws MarshallingTypeException, WSMarshallerException;
    /**
     * Unmarshal the given document node.
     *
     * @param <T> JAXB type of the root element.
     * @param n The DOM node to unmarshal.
     * @param c Class instance of the root element's type.
     * @return The JAXB object representing the given DOM node.
     * @throws MarshallingTypeException If the given node represents an unsupported JAXB type.
     * @throws WSMarshallerException If the given node is neither a {@link org.w3c.dom.Document}, nor an {@link
     *   org.w3c.dom.Element}.
     */
    <T> JAXBElement<T> unmarshal(Node n, Class<T> c) throws MarshallingTypeException, WSMarshallerException;
    /**
     * Marshal the given JAXB object.
     *
     * @param o JAXB object to marshal.
     * @return Document representing the given JAXB object.
     * @throws MarshallingTypeException If the given object is an unsupported JAXB type.
     */
    Document marshal(Object o) throws MarshallingTypeException;

    /**
     * Converts a DOM document representing a SOAP message to a SOAPMessage instance.
     * The SOAPMessage type is similar to the one in {@link <a href="http://saaj.java.net/">SAAJ</a>}.
     *
     * @param envDoc DOM document with a SOAP envelope element.
     * @return SOAPMessage instance representing the given SOAP document.
     * @throws SOAPException If the given document is not a SOAP document.
     */
    SOAPMessage doc2soap(Document envDoc) throws SOAPException;
    /**
     * Creates a SOAPMessage instance and adds the given content wrapped in a SOAP body.
     * The SOAPMessage type is similar to the one in {@link <a href="http://saaj.java.net/">SAAJ</a>}.
     *
     * @param content Document with the content that will be added to the SOAP body of the new SOAPMessage instance.
     * @return Freshly allocated SOAPMessage instance with the given document wrapped in the SOAP body.
     * @throws SOAPException If the SOAPMessage could not be created or the document could not be added.
     */
    SOAPMessage add2soap(Document content) throws SOAPException;

}
