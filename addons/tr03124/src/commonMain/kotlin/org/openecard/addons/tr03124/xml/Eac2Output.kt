package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EAC2OutputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac2Output(
	override val protocol: String,
) : AuthenticationProtocolData
