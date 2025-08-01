package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("EAC1InputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class Eac1Input(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("Certificate")
	@XmlElement
	val certificate: List<PrintableUByteArray>,
	@SerialName("CertificateDescription")
	@XmlElement
	val certificateDescription: PrintableUByteArray,
	@SerialName("RequiredCHAT")
	@XmlElement
	val requiredCHAT: PrintableUByteArray?,
	@SerialName("OptionalCHAT")
	@XmlElement
	val optionalCHAT: PrintableUByteArray?,
	@SerialName("AuthenticatedAuxiliaryData")
	@XmlElement
	val authenticatedAuxiliaryData: PrintableUByteArray?,
	@SerialName("TransactionInfo")
	@XmlElement
	val transactionInfo: String?,
	@SerialName("AcceptedEIDType")
	@XmlElement
	val acceptedEIDType: List<String> = emptyList(),
) : AuthenticationRequestProtocolData
