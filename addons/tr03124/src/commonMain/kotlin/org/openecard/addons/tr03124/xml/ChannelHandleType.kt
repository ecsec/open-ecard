package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("ChannelHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class ChannelHandleType(
	@SerialName("ProtocolTerminationPoint")
	@XmlElement
	val protocolTerminationPoint: String? = null,
	@SerialName("SessionIdentifier")
	@XmlElement
	val sessionIdentifier: String? = null,
	@SerialName("Binding")
	@XmlElement
	val binding: String? = null,
	@SerialName("PathSecurity")
	@XmlElement
	val pathSecurity: PathSecurityType? = null,
)
