/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.ws.jaxb

import jakarta.xml.bind.JAXBElement
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.soap.MessageFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.JAXBException
import org.openecard.ws.marshal.MarshallingTypeException
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WhitespaceFilter
import org.openecard.ws.soap.SOAPException
import org.openecard.ws.soap.SOAPMessage
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.io.UnsupportedEncodingException
import javax.xml.XMLConstants
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.system.exitProcess

private val LOG = KotlinLogging.logger {}

/**
 * Implementation of a WSMarshaller utilizing JAXB and Javas default XML facilities.
 *
 * @author Tobias Wich
 */
class JAXBMarshaller : WSMarshaller {
    // Marshaller and Unmarshaller
    private val marshaller: MarshallerImpl

    // w3 factory
    private val w3Factory: DocumentBuilderFactory
    private val w3Builder: DocumentBuilder
    private val serializer: Transformer

    // soap
    private val soapFactory: MessageFactory

    /**
     * Creates a JAXBMarshaller capable of marshalling und unmarshalling all JAXB element types found in the classpath
     * resource classes.lst.
     */
    init {
        val tmpMarshaller: MarshallerImpl
        val tmpW3Factory: DocumentBuilderFactory
        val tmpW3Builder: DocumentBuilder
        val tmpSerializer: Transformer
        val tmpSoapFactory: MessageFactory

        try {
            tmpMarshaller = MarshallerImpl()

            // instantiate w3 stuff
            tmpW3Factory = DocumentBuilderFactory.newInstance()
			tmpW3Factory.isNamespaceAware = true
			tmpW3Factory.isIgnoringComments = true
            try {
                tmpW3Factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            } catch (ex: ParserConfigurationException) {
                //LOG.warn { "Failed to enable secure processing for DOM Builder." }
            }
            // XXE countermeasures
			tmpW3Factory.isExpandEntityReferences = false
            try {
				tmpW3Factory.isXIncludeAware = false
            } catch (ex: UnsupportedOperationException) {
                //LOG.warn { "Failed to disable XInclude support." }
            }
            try {
                tmpW3Factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            } catch (ex: IllegalArgumentException) {
                //LOG.warn { "Failed to disallow external DTD access." }
            }
            try {
                tmpW3Factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            } catch (ex: ParserConfigurationException) {
                //LOG.debug("Failed to disallow DTDs entirely.");
            }
            try {
                tmpW3Factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
                tmpW3Factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            } catch (ex: ParserConfigurationException) {
                //LOG.warn { "Failed to disable XEE mitigations." }
            }

            tmpW3Builder = tmpW3Factory.newDocumentBuilder()

            val tfactory = TransformerFactory.newInstance()
            try {
                tfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            } catch (ex: TransformerConfigurationException) {
                //LOG.warn { "Failed to enable secure processing for XML Transformer." }
            }
            // XXE countermeasures
            try {
                tfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            } catch (ex: IllegalArgumentException) {
                //LOG.warn { "Failed to disallow external DTD access." }
            }
            try {
                tfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "")
            } catch (ex: IllegalArgumentException) {
                //LOG.warn { "Failed to disallow external stylesheet access." }
            }
            try {
                tfactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            } catch (ex: TransformerConfigurationException) {
                //LOG.debug("Failed to disallow DTDs entirely.");
            }

            tmpSerializer = tfactory.newTransformer()
            try {
                tmpSerializer.setOutputProperty(OutputKeys.INDENT, "yes")
                tmpSerializer.setOutputProperty(OutputKeys.STANDALONE, "yes")
                tmpSerializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                tmpSerializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            } catch (ex: IllegalArgumentException) {
                //LOG.warn { "Failed to configure output formatting." }
            }

            // instantiate soap stuff
            tmpSoapFactory = MessageFactory.newInstance()
        } catch (ex: ParserConfigurationException) {
            LOG.error(ex) { "Failed to initialize XML components." }
            exitProcess(1) // non recoverable
        } catch (ex: TransformerConfigurationException) {
            LOG.error(ex) { "Failed to initialize XML components." }
            exitProcess(1)
        } catch (ex: IllegalArgumentException) {
            LOG.error(ex) { "Failed to initialize XML components." }
            exitProcess(1)
        } catch (ex: SOAPException) {
            LOG.error(ex) { "Failed to initialize XML components." }
            exitProcess(1)
        }

        marshaller = tmpMarshaller
        w3Factory = tmpW3Factory
        w3Builder = tmpW3Builder
        serializer = tmpSerializer
        soapFactory = tmpSoapFactory
    }


    ////////////////////////////////////////////////////////////////////////////
    // public functions to marshal and convert stuff
    ////////////////////////////////////////////////////////////////////////////
    @kotlin.Throws(MarshallingTypeException::class)
	override fun addXmlTypeClass(xmlTypeClass: Class<*>) {
        marshaller.addXmlClass(xmlTypeClass)
    }

    override fun removeAllTypeClasses() {
        marshaller.removeAllClasses()
    }


    @kotlin.jvm.Synchronized
    @kotlin.Throws(SAXException::class)
	override fun str2doc(docStr: String): Document {
        try {
            // read dom as w3
            val strReader = StringReader(docStr)
            val inSrc = InputSource(strReader)
            val doc = w3Builder.parse(inSrc)

            WhitespaceFilter.filter(doc)

            return doc
        } catch (ex: IOException) {
            throw SAXException(ex)
        }
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(SAXException::class, IOException::class)
	override fun str2doc(docStr: InputStream): Document {
        // read dom as w3
        val doc: Document = w3Builder.parse(docStr)

        WhitespaceFilter.filter(doc)

        return doc
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(TransformerException::class)
    override fun doc2str(doc: Node): String {
        val out = ByteArrayOutputStream()
        serializer.transform(DOMSource(doc), StreamResult(out))
        val result: String
        try {
            result = out.toString("UTF-8")
        } catch (ex: UnsupportedEncodingException) {
            throw TransformerException(ex)
        }
        return result
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(MarshallingTypeException::class, WSMarshallerException::class)
    override fun unmarshal(n: Node): Any {
        val newDoc = createDoc(n)
        val result: Any
        try {
            result = marshaller.getUnmarshaller().unmarshal(newDoc) //NOI18N
        } catch (ex: JAXBException) {
            throw MarshallingTypeException(ex)
        }
        return result
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(MarshallingTypeException::class, WSMarshallerException::class)
    override fun <T> unmarshal(n: Node, c: Class<T>): JAXBElement<T> {
        val newDoc = createDoc(n)
        val result: JAXBElement<T>
        try {
            result = marshaller.getUnmarshaller().unmarshal(newDoc, c) //NOI18N
        } catch (ex: JAXBException) {
            throw MarshallingTypeException(ex)
        }
        return result
    }

    @kotlin.Throws(WSMarshallerException::class)
    private fun createDoc(n: Node): Document {
        return when (n) {
			is Document -> {
				n
			}

			is Element -> {
				val newDoc = w3Builder.newDocument()
				val root: Node = newDoc.importNode(n, true)
				newDoc.appendChild(root)
				newDoc
			}

			else -> {
				throw WSMarshallerException("Only w3c Document and Element are accepted.")
			}
		}
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(MarshallingTypeException::class)
    override fun marshal(o: Any): Document {
        try {
            val d: Document = w3Builder.newDocument()
            marshaller.getMarshaller().marshal(o, d)
            return d
        } catch (ex: JAXBException) {
            throw MarshallingTypeException(ex)
        }
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(SOAPException::class)
    override fun doc2soap(envDoc: Document): SOAPMessage {
        val msg = soapFactory.createMessage(envDoc)
        return msg
    }

    @kotlin.jvm.Synchronized
    @kotlin.Throws(SOAPException::class)
    override fun add2soap(content: Document): SOAPMessage {
        val msg = soapFactory.createMessage()
        val body = msg.soapBody
        body.addDocument(content)

        return msg
    }

}
