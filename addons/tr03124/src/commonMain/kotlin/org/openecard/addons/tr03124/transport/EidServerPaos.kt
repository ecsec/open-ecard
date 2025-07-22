package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.TcToken
import org.openecard.addons.tr03124.xml.RequestType
import org.openecard.addons.tr03124.xml.ResponseType
import org.openecard.addons.tr03124.xml.StartPaos
import kotlin.random.Random

internal class EidServerPaos(
	val token: TcToken,
	val startPaos: StartPaos,
	private val random: Random = Random.Default,
) : EidServerInterface {
	override suspend fun start(): RequestType {
		TODO("Not yet implemented")
	}

	override fun getServerCertificate(): Any {
		TODO("Not yet implemented")
	}

	override suspend fun respond(message: ResponseType): RequestType? {
		TODO("Not yet implemented")
	}
}
