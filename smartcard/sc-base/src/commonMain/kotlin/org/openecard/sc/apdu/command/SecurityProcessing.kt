package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.StatusWordResult
import org.openecard.sc.iface.CardChannel

interface SecurityCommandApdu : IsoCommandApdu

sealed interface SecurityCommandResult {
	fun success(): ResponseApdu

	val status: StatusWordResult
}

class SecurityCommandSuccess(
	val response: ResponseApdu,
) : SecurityCommandResult {
	override fun success(): ResponseApdu = response

	override val status = response.status
}

class SecurityCommandFailure(
	val ex: ApduProcessingError,
) : SecurityCommandResult {
	override val status = ex.status

	override fun success(): ResponseApdu = throw ex

	val retries: Int? by lazy {
		if (status.type == StatusWord.COUNTER_ENCODED) {
			status.parameter?.toInt()
		} else {
			null
		}
	}

	val verificationFailed by lazy {
		status.type == StatusWord.NVMEM_CHANGED_WARN
	}

	val authDeactivated by lazy {
		status.type in listOf(StatusWord.SELECT_FILE_DEACTIVATED, StatusWord.REFERENCE_DATA_UNUSABLE)
	}

	val authBlocked by lazy {
		if (retries == 0) {
			true
		} else if (status.type in listOf(StatusWord.AUTH_BLOCKED)) {
		}
	}

	/**
	 * Something else must be done before trying again, but the situation is fixable.
	 * In Pace e.g. this means the password is suspended.
	 */
	val conditionNotSatisifed by lazy { status.type == StatusWord.CONDITIONS_OF_USE_UNSATISFIED }

	/**
	 * Something is not prepared correctly and has to be fixed before trying again.
	 * In Pace e.g. this means either password blocked, deactivated or suspended
	 */
	val secStatusNotSatisfied by lazy {
		status.type == StatusWord.SECURITY_STATUS_UNSATISFIED
	}
}

fun SecurityCommandApdu.transmit(channel: CardChannel): SecurityCommandResult {
	try {
		val resp = channel.transmit(this.apdu)
		return SecurityCommandSuccess(resp)
	} catch (ex: ApduProcessingError) {
		return SecurityCommandFailure(ex)
	}
}
