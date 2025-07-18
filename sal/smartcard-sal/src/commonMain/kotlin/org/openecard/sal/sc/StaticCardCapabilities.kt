package org.openecard.sal.sc

import org.openecard.cif.definition.capabilities.CardCapabilitiesDefinition
import org.openecard.cif.definition.capabilities.CommandCodingDefinitions
import org.openecard.cif.definition.capabilities.DataCodingDefinitions
import org.openecard.cif.definition.capabilities.SelectionMethodsDefinition
import org.openecard.sc.iface.CardCapabilities
import org.openecard.sc.iface.CommandCoding
import org.openecard.sc.iface.DataCoding
import org.openecard.sc.iface.LogicalChannelAssignment
import org.openecard.sc.iface.SelectionMethods

class StaticCardCapabilities(
	capabilities: CardCapabilitiesDefinition,
) : CardCapabilities {
	override val selectionMethods: SelectionMethods by lazy { StaticSelectionMethods(capabilities.selectionMethods) }
	override val dataCoding: DataCoding? by lazy { capabilities.dataCoding?.let { StaticDataCoding(it) } }
	override val commandCoding: CommandCoding? by lazy { capabilities.commandCoding?.let { StaticCommandCoding(it) } }

	class StaticSelectionMethods(
		selection: SelectionMethodsDefinition,
	) : SelectionMethods {
		override val selectDfByFullName: Boolean = selection.selectDfByFullName
		override val selectDfByPartialName: Boolean = selection.selectDfByPartialName
		override val selectDfByPath: Boolean = selection.selectDfByPath
		override val selectDfByFileId: Boolean = selection.selectDfByFileId
		override val selectDfImplicit: Boolean = selection.selectDfImplicit
		override val supportsShortEf: Boolean = selection.supportsShortEf
		override val supportsRecordNumber: Boolean = selection.supportsRecordNumber
		override val supportsRecordIdentifier: Boolean = selection.supportsRecordIdentifier
	}

	class StaticDataCoding(
		dataCoding: DataCodingDefinitions,
	) : DataCoding {
		override val tlvEfs: Boolean = dataCoding.tlvEfs
		override val writeOneTime: Boolean = dataCoding.writeOneTime
		override val writeProprietary: Boolean = dataCoding.writeProprietary
		override val writeOr: Boolean = dataCoding.writeOr
		override val writeAnd: Boolean = dataCoding.writeAnd
		override val ffValidAsTlvFirstByte: Boolean = dataCoding.ffValidAsTlvFirstByte
		override val dataUnitsQuartets: Int = dataCoding.dataUnitsQuartets
	}

	class StaticCommandCoding(
		commandCoding: CommandCodingDefinitions,
	) : CommandCoding {
		override val supportsCommandChaining: Boolean = commandCoding.supportsCommandChaining
		override val supportsExtendedLength: Boolean = commandCoding.supportsExtendedLength
		override val logicalChannel: LogicalChannelAssignment by lazy { commandCoding.logicalChannel.toScType() }
		override val maximumLogicalChannels: Int = commandCoding.maximumLogicalChannels
	}
}

internal fun CommandCodingDefinitions.LogicalChannelAssignment.toScType(): LogicalChannelAssignment =
	when (this) {
		CommandCodingDefinitions.LogicalChannelAssignment.ASSIGN_BY_BOTH -> LogicalChannelAssignment.ASSIGN_BY_BOTH
		CommandCodingDefinitions.LogicalChannelAssignment.ASSIGN_BY_CARD -> LogicalChannelAssignment.ASSIGN_BY_CARD
		CommandCodingDefinitions.LogicalChannelAssignment.ASSIGN_BY_INTERFACE -> LogicalChannelAssignment.ASSIGN_BY_INTERFACE
		CommandCodingDefinitions.LogicalChannelAssignment.NO_LOGICAL_CHANNELS -> LogicalChannelAssignment.NO_LOGICAL_CHANNELS
	}
