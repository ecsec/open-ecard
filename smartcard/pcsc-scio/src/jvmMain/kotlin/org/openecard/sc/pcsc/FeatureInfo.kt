package org.openecard.sc.pcsc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.Feature
import java.nio.ByteBuffer

private val log = KotlinLogging.logger { }

internal enum class ControlCodeType(
	val code: Int,
) {
	VERIFY_PIN_START(0x01),
	VERIFY_PIN_FINISH(0x02),
	MODIFY_PIN_START(0x03),
	MODIFY_PIN_FINISH(0x04),
	GET_KEY_PRESSED(0x05),
	VERIFY_PIN_DIRECT(0x06),
	MODIFY_PIN_DIRECT(0x07),
	MCT_READER_DIRECT(0x08),
	MCT_UNIVERSAL(0x09),
	IFD_PIN_PROPERTIES(0x0A),
	ABORT(0x0B),
	SET_SPE_MESSAGE(0x0C),
	VERIFY_PIN_DIRECT_APP_ID(0x0D),
	MODIFY_PIN_DIRECT_APP_ID(0x0E),
	WRITE_DISPLAY(0x0F),
	GET_KEY(0x10),
	IFD_DISPLAY_PROPERTIES(0x11),
	GET_TLV_PROPERTIES(0x12),
	CCID_ESC_COMMAND(0x13),
	EXECUTE_PACE(0x20),
	;

	companion object {
		fun forCode(code: Int): ControlCodeType? = entries.find { it.code == code }
	}
}

internal class FeatureInfo(
	private val terminal: PcscTerminalConnection,
) {
	val featureMap: Map<ControlCodeType, Int> by lazy {
		val response = terminal.controlCommand(featureControlCode, byteArrayOf())
		buildFeatureMap(response)
	}

	val featureControlCode by lazy {
		scardCtlCode(3400)
	}

	fun scardCtlCode(code: Int): Int =
		if (isWindows) {
			(0x31 shl 16 or (code shl 2))
		} else {
			0x42000000 + code
		}

	private val isWindows: Boolean
		get() {
			val osName = System.getProperty("os.name").lowercase()
			return (osName.contains("windows"))
		}

	private fun buildFeatureMap(featureResponse: ByteArray): Map<ControlCodeType, Int> {
		val result = mutableMapOf<ControlCodeType, Int>()

		if ((featureResponse.size % 6) == 0) {
			var i = 0
			while (i < featureResponse.size) {
				val nextChunk = featureResponse.copyOfRange(i, i + 6)
				if (nextChunk.size == 6 && nextChunk[1].toInt() == 4) {
					val tagCode = nextChunk[0]
					val tag = tagCode.let { ControlCodeType.forCode(it.toInt()) }
					if (tag != null) {
						val codeData = nextChunk.copyOfRange(2, 6)
						val code = ByteBuffer.wrap(codeData).int
						result.put(tag, code)
					} else {
						log.warn { "Unknown control code $tagCode found" }
					}
				}
				i += 6
			}
		}

		return result
	}
}

internal fun Map<ControlCodeType, Int>.toFeatures(terminalConnection: PcscTerminalConnection): Set<Feature> {
	val result = mutableSetOf<Feature>()

	// verifyPin
	val verifyPinCode = get(ControlCodeType.VERIFY_PIN_DIRECT)
	if (verifyPinCode != null) {
		result.add(PcscVerifyPinFeature(terminalConnection, verifyPinCode))
	}

	return result
}
