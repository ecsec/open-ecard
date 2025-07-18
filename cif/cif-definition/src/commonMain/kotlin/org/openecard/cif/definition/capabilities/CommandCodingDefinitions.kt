package org.openecard.cif.definition.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class CommandCodingDefinitions(
	val supportsCommandChaining: Boolean,
	val supportsExtendedLength: Boolean,
	val logicalChannel: LogicalChannelAssignment,
	val maximumLogicalChannels: Int,
) {
	enum class LogicalChannelAssignment {
		ASSIGN_BY_BOTH,
		ASSIGN_BY_CARD,
		ASSIGN_BY_INTERFACE,
		NO_LOGICAL_CHANNELS,
	}
}
