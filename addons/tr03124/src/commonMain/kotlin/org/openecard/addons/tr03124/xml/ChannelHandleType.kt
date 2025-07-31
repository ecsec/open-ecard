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
	val protocolTerminationPoint: String?,
	@SerialName("SessionIdentifier")
	@XmlElement
	val sessionIdentifier: String?,
	@SerialName("Binding")
	@XmlElement
	val binding: String?,
	@SerialName("PathSecurity")
	@XmlElement
	val pathSecurity: PathSecurityType?,
)
