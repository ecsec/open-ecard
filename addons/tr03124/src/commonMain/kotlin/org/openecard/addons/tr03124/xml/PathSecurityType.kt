package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.SerializableElement
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("PathSecurity", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class PathSecurityType(
	@SerialName("Protocol")
	@XmlElement
	val pathSecurityProtocol: String?,
	@SerialName("Parameters")
	@XmlElement
	val parameters: SerializableElement?,
)
