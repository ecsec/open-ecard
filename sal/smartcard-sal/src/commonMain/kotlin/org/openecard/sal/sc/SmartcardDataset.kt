package org.openecard.sal.sc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.cif.definition.dataset.DatasetType
import org.openecard.sal.iface.Dataset
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.sc.acl.missingAuthentications
import org.openecard.sc.apdu.command.FileControlInformation
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.ReadRecord
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.info.EfStructure
import org.openecard.sc.iface.info.Fcp
import org.openecard.sc.iface.info.FileInfo
import org.openecard.utils.common.throwIf
import org.openecard.utils.common.toUShort

private val log = KotlinLogging.logger { }

class SmartcardDataset(
	override val name: String,
	override val application: SmartcardApplication,
	val ds: DataSetDefinition,
	val readAcl: BoolTreeOr<BoolTreeLeaf>,
	val writeAcl: BoolTreeOr<BoolTreeLeaf>,
) : Dataset {
	@OptIn(ExperimentalUnsignedTypes::class)
	private val file = SmartcardEf(application.channel, ds.path.v, ds.shortEf, ds.type, this)

	override val missingReadAuthentications: MissingAuthentications
		get() = readAcl.missingAuthentications(application.device)
	override val missingWriteAuthentications: MissingAuthentications
		get() = writeAcl.missingAuthentications(application.device)

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun read(): UByteArray =
		mapSmartcardError {
			throwIf(!missingReadAuthentications.isSolved) { MissingAuthentication("Read ACL is not satisfied") }
			file.read()
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun write(): UByteArray =
		mapSmartcardError {
			throwIf(!missingWriteAuthentications.isSolved) { MissingAuthentication("Write ACL is not satisfied") }
			file.write()
		}
}
