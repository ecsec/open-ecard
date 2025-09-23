package org.openecard.utils.serialization

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.io.UnsupportedEncodingException
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XmlUtils {
	private val w3Factory: DocumentBuilderFactory by lazy {
		val tmpW3Factory = DocumentBuilderFactory.newInstance()
		tmpW3Factory.isNamespaceAware = true
		tmpW3Factory.isIgnoringComments = true
		try {
			tmpW3Factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
		} catch (ex: ParserConfigurationException) {
			// LOG.warn { "Failed to enable secure processing for DOM Builder." }
		}
		// XXE countermeasures
		tmpW3Factory.isExpandEntityReferences = false
		try {
			tmpW3Factory.isXIncludeAware = false
		} catch (ex: UnsupportedOperationException) {
			// LOG.warn { "Failed to disable XInclude support." }
		}
		try {
			tmpW3Factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
		} catch (ex: IllegalArgumentException) {
			// LOG.warn { "Failed to disallow external DTD access." }
		}
		try {
			tmpW3Factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
		} catch (ex: ParserConfigurationException) {
			// LOG.debug("Failed to disallow DTDs entirely.");
		}
		try {
			tmpW3Factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
			tmpW3Factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
		} catch (ex: ParserConfigurationException) {
			// LOG.warn { "Failed to disable XEE mitigations." }
		}

		tmpW3Factory
	}

	fun w3Builder(): DocumentBuilder = w3Factory.newDocumentBuilder()

	private val transformerFactory: TransformerFactory by lazy {
		val tfactory = TransformerFactory.newInstance()
		try {
			tfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
		} catch (ex: TransformerConfigurationException) {
			// LOG.warn { "Failed to enable secure processing for XML Transformer." }
		}
		// XXE countermeasures
		try {
			tfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
		} catch (ex: IllegalArgumentException) {
			// LOG.warn { "Failed to disallow external DTD access." }
		}
		try {
			tfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "")
		} catch (ex: IllegalArgumentException) {
			// LOG.warn { "Failed to disallow external stylesheet access." }
		}
		try {
			tfactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
		} catch (ex: TransformerConfigurationException) {
			// LOG.debug("Failed to disallow DTDs entirely.");
		}

		tfactory
	}

	fun transformer(): Transformer {
		val tmpSerializer = transformerFactory.newTransformer()
		try {
			tmpSerializer.setOutputProperty(OutputKeys.INDENT, "yes")
			tmpSerializer.setOutputProperty(OutputKeys.STANDALONE, "yes")
			tmpSerializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
			tmpSerializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
		} catch (ex: IllegalArgumentException) {
			// LOG.warn { "Failed to configure output formatting." }
		}
		return tmpSerializer
	}

	companion object {
		@Throws(SAXException::class)
		fun DocumentBuilder.str2doc(docStr: String): Document {
			try {
				// read dom as w3
				val strReader = StringReader(docStr)
				val inSrc = InputSource(strReader)
				val doc = this.parse(inSrc)

				return doc
			} catch (ex: IOException) {
				throw SAXException(ex)
			}
		}

		@Throws(SAXException::class, IOException::class)
		fun DocumentBuilder.str2doc(docStr: InputStream): Document {
			// read dom as w3
			val doc: Document = this.parse(docStr)

			return doc
		}

		@Throws(TransformerException::class)
		fun Transformer.doc2str(doc: Node): String {
			val out = ByteArrayOutputStream()
			this.transform(DOMSource(doc), StreamResult(out))
			val result: String
			try {
				result = out.toString("UTF-8")
			} catch (ex: UnsupportedEncodingException) {
				throw TransformerException(ex)
			}
			return result
		}
	}
}
