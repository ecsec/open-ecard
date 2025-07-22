package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.xml.RequestType
import org.openecard.addons.tr03124.xml.ResponseType

interface EidServerInterface {
	suspend fun start(): RequestType

	fun getServerCertificate(): Any

	suspend fun respond(message: ResponseType): RequestType?
}
