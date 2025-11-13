package org.openecard.addons.tr03124.testutils

import org.w3c.dom.Document
import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

class SchemaValidator(
	val schema: Schema,
) {
	fun validate(xml: String) {
		val source = StreamSource(xml.reader())
		validate(source)
	}

	fun validate(xml: Document) {
		val source = DOMSource(xml)
		validate(source)
	}

	fun validate(xmlSource: Source) {
		schema.newValidator().validate(xmlSource)
	}

	companion object {
		fun load(): SchemaValidator {
			val fac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
			val schemaUrl = Companion::class.java.getResource("/schema/eCard.xsd")
			val schema = fac.newSchema(schemaUrl)
			return SchemaValidator(schema)
		}
	}
}
