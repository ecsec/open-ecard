package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EAC2InputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac2Input(
	override val protocol: String,
) : AuthenticationProtocolData
