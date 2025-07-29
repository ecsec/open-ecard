package org.openecard.cif.dsl.api.capabilities

import org.openecard.cif.definition.capabilities.CommandCodingDefinitions
import org.openecard.cif.dsl.api.CifScope

interface CommandCodingScope : CifScope {
	var supportsCommandChaining: Boolean
	var supportsExtendedLength: Boolean
	var logicalChannel: CommandCodingDefinitions.LogicalChannelAssignment
	var maximumLogicalChannels: Int
}
