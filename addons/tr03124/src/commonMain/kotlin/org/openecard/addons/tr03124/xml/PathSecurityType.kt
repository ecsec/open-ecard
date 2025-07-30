package org.openecard.addons.tr03124.xml

import io.ktor.http.Parameters
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.cif.definition.CardProtocol

@Serializable
@XmlSerialName("PathSecurity", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class PathSecurityType(
	@SerialName("Protocol")
	@XmlElement
	val pathSecurityProtocol: String?,
	@SerialName("Parameters")
	@XmlElement
	@Serializable
	@Contextual
	val parameters: Any?,
)
