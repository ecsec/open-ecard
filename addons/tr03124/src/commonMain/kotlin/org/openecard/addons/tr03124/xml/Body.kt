package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Body", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
data class Body(
	val startPaos: StartPaos? = null,
	val startPaosResponse: StartPaosResponse? = null,
	val initializeFrameworkRequest: InitializeFrameworkRequest? = null,
	val initializeFrameworkResponse: InitializeFrameworkResponse? = null,
	val didAuthenticateRequest: DidAuthenticateRequest? = null,
	val didAuthenticateResponse: DidAuthenticateResponse? = null,
	val transmitRequest: TransmitRequest? = null,
	val transmitResponse: TransmitResponse? = null,
) {
	@Transient
	val content: RequestResponseBaseType =
		if (startPaos != null) {
			startPaos
		} else if (startPaosResponse != null) {
			startPaosResponse
		} else if (initializeFrameworkRequest != null) {
			initializeFrameworkRequest
		} else if (initializeFrameworkResponse != null) {
			initializeFrameworkResponse
		} else if (didAuthenticateRequest != null) {
			didAuthenticateRequest
		} else if (didAuthenticateResponse != null) {
			didAuthenticateResponse
		} else if (transmitRequest != null) {
			transmitRequest
		} else if (transmitResponse != null) {
			transmitResponse
		} else {
			throw SerializationException("Unknown data type in body")
		}
}
