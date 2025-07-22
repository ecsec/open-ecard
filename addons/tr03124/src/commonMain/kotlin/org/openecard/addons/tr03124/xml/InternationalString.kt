package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
class InternationalString(
	@XmlValue
	val value: String,
	@XmlSerialName("lang", prefix = Namespaces.XML.PREFIX, namespace = Namespaces.XML.NS)
	val lang: String,
)
