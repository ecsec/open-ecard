package org.openecard.sal.sc

import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.sal.iface.Dataset
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.sc.acl.missingAuthentications

class SmartcardDataset(
	override val name: String,
	override val application: SmartcardApplication,
	val ds: DataSetDefinition,
	val readAcl: BoolTreeOr<BoolTreeLeaf>,
	val writeAcl: BoolTreeOr<BoolTreeLeaf>,
) : Dataset {
	override val missingReadAuthentications: MissingAuthentications
		get() = readAcl.missingAuthentications(application.device)
	override val missingWriteAuthentications: MissingAuthentications
		get() = writeAcl.missingAuthentications(application.device)

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun read(): UByteArray {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun write(): UByteArray {
		TODO("Not yet implemented")
	}
}
