package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("InputAPDUInfo", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class InputAPDUInfoType(
	@SerialName("InputAPDU")
	@XmlElement
	val inputAPDU: PrintableUByteArray,
	@SerialName("AcceptableStatusCode")
	@XmlElement
	val acceptableStatusCode: List<PrintableUByteArray>?,
)
