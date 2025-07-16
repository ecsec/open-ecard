package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.StatusWordResult
import org.openecard.sc.apdu.checkOk
import org.openecard.sc.iface.CardChannel

/**
 * Marker interface for security APDUS.
 */
interface SecurityCommandApdu : IsoCommandApdu

/**
 * Result of executing a security APDU when there is no response data defined.
 */
sealed interface SecurityCommandResult {
	fun success(): ResponseApdu

	val status: StatusWordResult

	val resultType: SecurityCommandResultType
}

class SecurityCommandSuccess(
	val response: ResponseApdu,
) : SecurityCommandResult {
	override fun success(): ResponseApdu = response

	override val status = response.status

	override val resultType: SecurityCommandResultType
		get() = SecurityCommandResultType.OK
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

	override val resultType: SecurityCommandResultType by lazy {
		if (retries == 0) {
			SecurityCommandResultType.AUTH_BLOCKED
		} else {
			SecurityCommandResultType.entries.find { status.type in it.statusCodes }
				?: SecurityCommandResultType.OTHER_ERROR
		}
	}

	val verificationFailed by lazy {
		resultType == SecurityCommandResultType.VERIFICATION_FAILED
	}

	val authDeactivated by lazy {
		resultType == SecurityCommandResultType.AUTH_DEACTIVATED
	}

	val authBlocked by lazy {
		if (retries == 0) {
			true
		} else if (resultType == SecurityCommandResultType.AUTH_BLOCKED) {
		}
	}

	/**
	 * Something else must be done before trying again, but the situation is fixable.
	 * In Pace e.g. this means the password is suspended.
	 */
	val conditionNotSatisifed by lazy {
		resultType == SecurityCommandResultType.CONDITION_NOT_SATISFIED
	}

	/**
	 * Something is not prepared correctly and has to be fixed before trying again.
	 * In Pace e.g. this means either password blocked, deactivated or suspended
	 */
	val secStatusNotSatisfied by lazy {
		resultType == SecurityCommandResultType.SEC_STATUS_NOT_SATISFIED
	}

	val unknownReference by lazy {
		resultType == SecurityCommandResultType.UNKNOWN_REFERENCE
	}
}

enum class SecurityCommandResultType(
	vararg val statusCodes: StatusWord,
) {
	OK(StatusWord.OK),
	COUNTER(StatusWord.COUNTER_ENCODED),
	VERIFICATION_FAILED(StatusWord.NVMEM_CHANGED_WARN),
	AUTH_DEACTIVATED(StatusWord.SELECT_FILE_DEACTIVATED, StatusWord.REFERENCE_DATA_UNUSABLE),
	AUTH_BLOCKED(StatusWord.AUTH_BLOCKED),
	CONDITION_NOT_SATISFIED(StatusWord.CONDITIONS_OF_USE_UNSATISFIED),
	SEC_STATUS_NOT_SATISFIED(StatusWord.SECURITY_STATUS_UNSATISFIED),
	UNKNOWN_REFERENCE(StatusWord.REFERENCED_DATA_NOT_FOUND),
	OTHER_ERROR(),
}

fun SecurityCommandApdu.transmit(channel: CardChannel): SecurityCommandResult =
	channel.transmit(this.apdu).checkSecurityCommandResponse()

fun ResponseApdu.checkSecurityCommandResponse(): SecurityCommandResult {
	try {
		this.checkOk()
		return SecurityCommandSuccess(this)
	} catch (ex: ApduProcessingError) {
		return SecurityCommandFailure(ex)
	}
}
