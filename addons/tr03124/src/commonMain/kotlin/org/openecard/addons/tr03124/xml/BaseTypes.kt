package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
sealed interface RequestResponseBaseType

@Serializable
sealed interface RequestType : RequestResponseBaseType {
	@XmlSerialName("RequestID", namespace = Namespaces.DSS.NS, prefix = Namespaces.DSS.PREFIX)
	@XmlElement(false)
	val requestId: String?

	@XmlSerialName("Profile", namespace = Namespaces.DSS.NS, prefix = Namespaces.DSS.PREFIX)
	@XmlElement(false)
	val profile: String?
}

@Serializable
sealed interface ResponseType : RequestResponseBaseType {
	val result: Result

	@XmlSerialName("RequestID", namespace = Namespaces.DSS.NS, prefix = Namespaces.DSS.PREFIX)
	@XmlElement(false)
	val requestId: String?

	@XmlSerialName("Profile", namespace = Namespaces.DSS.NS, prefix = Namespaces.DSS.PREFIX)
	@XmlElement(false)
	val profile: String?
}
