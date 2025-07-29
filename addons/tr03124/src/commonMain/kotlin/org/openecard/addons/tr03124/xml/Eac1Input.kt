package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EAC1InputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac1Input(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("Certificate")
	@XmlElement
	val certificate: String?,
	@SerialName("CertificateDescription")
	@XmlElement
	val certificateDescription: String?,
	@SerialName("RequiredCHAT")
	@XmlElement
	val requiredChat: String?,
	@SerialName("OptionalCHAT")
	@XmlElement
	val optionalChat: String?,
	@SerialName("AuthenticatedAuxiliaryData")
	@XmlElement
	val authenticatedAuxiliaryData: String?,
) : AuthenticationProtocolData
