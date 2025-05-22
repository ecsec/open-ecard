package org.openecard.sal.sc

import org.openecard.sal.iface.Dataset
import org.openecard.sal.iface.MissingAuthentications

class SmartcardDataset(
	override val name: String,
	override val application: SmartcardApplication,
) : Dataset {
	override val missingReadAuthentications: MissingAuthentications
		get() = TODO("Not yet implemented")
	override val missingWriteAuthentications: MissingAuthentications
		get() = TODO("Not yet implemented")

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun read(): UByteArray {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun write(): UByteArray {
		TODO("Not yet implemented")
	}
}
