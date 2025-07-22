package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Envelope", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
class Envelope(
	val header: Header?,
	val body: Body,
)
