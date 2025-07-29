package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EAC1OutputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac1Output(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("CertificateHolderAuthorizationTemplate")
	@XmlElement
	val certificateHolderAuthorizationTemplate: String?,
	@SerialName("CertificationAuthorityReference")
	@XmlElement
	val certificationAuthorityReference: String?,
	@SerialName("EFCardAccess")
	@XmlElement
	val efCardAccess: String?,
	@SerialName("IDPICC")
	@XmlElement
	val idPICC: String?,
	@SerialName("Challenge")
	@XmlElement
	val challenge: String?,
) : AuthenticationProtocolData
