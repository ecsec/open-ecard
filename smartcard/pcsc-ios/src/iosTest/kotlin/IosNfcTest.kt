import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.toPrintable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosNfcTest {
	@Test
	fun getFactory() {
		assertEquals("IosNFC", IosTerminalFactory().name)
	}

	@Test
	fun createISO7816APDU() {
		CommandApdu(
			0x00.toUByte(),
			0xb0.toUByte(),
			0x00.toUByte(),
			0x00.toUByte(),
			ubyteArrayOf(0xbe.toUByte(), 0xef.toUByte()).toPrintable(),
			255.toUShort(),
			false,
		).toIosApdu()
	}

	@Test
	fun createISO7816APDU_DataNull() {
		CommandApdu(
			0x00.toUByte(),
			0xb0.toUByte(),
			0x00.toUByte(),
			0x00.toUByte(),
			ubyteArrayOf().toPrintable(),
			255.toUShort(),
			false,
		).toIosApdu()
	}
}
