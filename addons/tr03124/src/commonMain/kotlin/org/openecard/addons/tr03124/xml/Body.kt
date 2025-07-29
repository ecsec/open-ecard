package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import javax.naming.Name

@Serializable
@XmlSerialName("Body", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
class Body(
	val content: RequestResponseBaseType,
	@XmlSerialName("DIDAuthenticate", namespace = Namespaces.ISO.NS)
	@XmlElement
	val didAuthenticate: String,
	@XmlSerialName("StartPAOSResponse", namespace = Namespaces.ISO.NS)
	@XmlElement
	val startPAOSResponse: String,
	@XmlSerialName("Transmit", namespace = Namespaces.ISO.NS)
	@XmlElement
	val transmit: String,
)
