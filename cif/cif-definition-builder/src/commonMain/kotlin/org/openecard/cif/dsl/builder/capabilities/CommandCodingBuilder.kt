package org.openecard.cif.dsl.builder.capabilities

import org.openecard.cif.definition.capabilities.CommandCodingDefinitions
import org.openecard.cif.dsl.api.capabilities.CommandCodingScope
import org.openecard.cif.dsl.builder.Builder

class CommandCodingBuilder :
	CommandCodingScope,
	Builder<CommandCodingDefinitions> {
	private var _supportsCommandChaining: Boolean? = null
	override var supportsCommandChaining: Boolean
		get() = checkNotNull(_supportsCommandChaining)
		set(value) {
			_supportsCommandChaining = value
		}

	private var _supportsExtendedLength: Boolean? = null
	override var supportsExtendedLength: Boolean
		get() = checkNotNull(_supportsExtendedLength)
		set(value) {
			_supportsExtendedLength = value
		}

	private var _logicalChannel: CommandCodingDefinitions.LogicalChannelAssignment? = null
	override var logicalChannel: CommandCodingDefinitions.LogicalChannelAssignment
		get() = checkNotNull(_logicalChannel)
		set(value) {
			_logicalChannel = value
		}

	private var _maximumLogicalChannels: Int? = null
	override var maximumLogicalChannels: Int
		get() = checkNotNull(_maximumLogicalChannels)
		set(value) {
			_maximumLogicalChannels = value
		}

	override fun build(): CommandCodingDefinitions =
		CommandCodingDefinitions(
			supportsCommandChaining = supportsCommandChaining,
			supportsExtendedLength = supportsExtendedLength,
			logicalChannel = logicalChannel,
			maximumLogicalChannels = maximumLogicalChannels,
		)
}
