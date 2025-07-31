package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("EAC1OutputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac1Output(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("CertificateHolderAuthorizationTemplate")
	@XmlElement
	val certificateHolderAuthorizationTemplate: PrintableUByteArray?,
	@SerialName("CertificationAuthorityReference")
	@XmlElement
	val certificationAuthorityReference: List<String>?,
	@SerialName("EFCardAccess")
	@XmlElement
	val efCardAccess: PrintableUByteArray,
	@SerialName("IDPICC")
	@XmlElement
	val idPICC: PrintableUByteArray,
	@SerialName("Challenge")
	@XmlElement
	val challenge: PrintableUByteArray,
) : AuthenticationProtocolData
