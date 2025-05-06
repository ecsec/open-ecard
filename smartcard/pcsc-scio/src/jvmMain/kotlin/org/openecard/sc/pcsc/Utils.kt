package org.openecard.sc.pcsc

import jnasmartcardio.Smartcardio.EstablishContextException
import jnasmartcardio.Smartcardio.JnaCardException
import jnasmartcardio.Smartcardio.JnaCardNotPresentException
import jnasmartcardio.Smartcardio.JnaPCSCException
import org.openecard.sc.iface.InternalSystemError
import org.openecard.sc.iface.NoSmartcard
import org.openecard.sc.iface.SmartcardException
import org.openecard.sc.iface.UnsupportedFeature
import javax.smartcardio.CardException
import javax.smartcardio.CardNotPresentException

fun <R> mapScioError(block: () -> R): R {
	try {
		return block.invoke()
	} catch (ex: EstablishContextException) {
		throw ex.cause!!.toScException()
	} catch (ex: JnaCardNotPresentException) {
		throw ex.toScException()
	} catch (ex: CardNotPresentException) {
		throw ex.toScException()
	} catch (ex: JnaPCSCException) {
		throw ex.toScException()
	} catch (ex: JnaCardException) {
		throw UnsupportedFeature(ex.message, ex)
	} catch (ex: CardException) {
		throw InternalSystemError(ex.message, ex.cause)
	}
}

internal fun JnaPCSCException.toScException(): SmartcardException = mapToScException(code, message, this)

internal fun JnaCardNotPresentException.toScException(): SmartcardException = mapToScException(code, message, this)

internal fun CardNotPresentException.toScException(): SmartcardException = NoSmartcard(message, this)

fun mapToScException(
	code: Long,
	msg: String?,
	cause: Throwable,
): SmartcardException =
	when (code) {
		// TODO: handle all PCSC codes
		else ->
			InternalSystemError(msg, cause)
	}
