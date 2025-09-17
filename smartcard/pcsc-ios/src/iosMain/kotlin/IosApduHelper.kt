import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.toCommandApdu
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable
import platform.CoreNFC.NFCISO7816APDU
import platform.Foundation.NSData

@Throws(Exception::class)
fun commandApduFromRawData(data: NSData): CommandApdu? = data.toUByteArray()?.toCommandApdu()

@Throws(Exception::class)
fun commandApduFromNFCISO7816APDU(
	iosApdu: NFCISO7816APDU,
	forceExtendedLength: Boolean = false,
): CommandApdu =
	CommandApdu(
		cla = iosApdu.instructionClass,
		ins = iosApdu.instructionCode,
		p1 = iosApdu.p1Parameter,
		p2 = iosApdu.p2Parameter,
		data =
			iosApdu.data?.toUByteArray()?.let { PrintableUByteArray(it) } ?: ubyteArrayOf().toPrintable(),
		le =
			if (iosApdu.expectedResponseLength < 0) {
				null
			} else {
				iosApdu.expectedResponseLength.toUShort()
			},
		forceExtendedLength = forceExtendedLength,
	)

fun CommandApdu.toIosApdu(): NFCISO7816APDU =
	NFCISO7816APDU(
		data = data.v.toNSData(),
		instructionClass = classByte.byte,
		instructionCode = ins,
		p2Parameter = p2,
		p1Parameter = p1,
		expectedResponseLength =
			when (val v = le?.toLong()) {
				null,
				0L,
				-> -1L
				else -> v
			},
	)
