package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.InterIndustryClassByte

interface IsoCommandApdu {
	val apdu: CommandApdu

	abstract class ChainableIsoCommandApdu<T : IsoCommandApdu>(
		val cla: InterIndustryClassByte = InterIndustryClassByte.Default,
	) : IsoCommandApdu {
		abstract fun setCommandChaining(): T
	}
}
