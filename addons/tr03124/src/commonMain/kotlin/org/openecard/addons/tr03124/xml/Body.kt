package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Body", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
data class Body(
	val content: RequestResponseBaseType,
)
