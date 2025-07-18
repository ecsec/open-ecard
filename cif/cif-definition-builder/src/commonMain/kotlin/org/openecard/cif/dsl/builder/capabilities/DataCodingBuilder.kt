package org.openecard.cif.dsl.builder.capabilities

import org.openecard.cif.definition.capabilities.DataCodingDefinitions
import org.openecard.cif.dsl.api.capabilities.DataCodingScope
import org.openecard.cif.dsl.builder.Builder

class DataCodingBuilder :
	DataCodingScope,
	Builder<DataCodingDefinitions> {
	private var _tlvEfs: Boolean? = null
	override var tlvEfs: Boolean
		get() = checkNotNull(_tlvEfs)
		set(value) {
			_tlvEfs = value
		}

	private var _writeOneTime: Boolean? = null
	override var writeOneTime: Boolean
		get() = checkNotNull(_writeOneTime)
		set(value) {
			_writeOneTime = value
		}

	private var _writeProprietary: Boolean? = null
	override var writeProprietary: Boolean
		get() = checkNotNull(_writeProprietary)
		set(value) {
			_writeProprietary = value
		}

	private var _writeOr: Boolean? = null
	override var writeOr: Boolean
		get() = checkNotNull(_writeOr)
		set(value) {
			_writeOr = value
		}

	private var _writeAnd: Boolean? = null
	override var writeAnd: Boolean
		get() = checkNotNull(_writeAnd)
		set(value) {
			_writeAnd = value
		}

	private var _ffValidAsTlvFirstByte: Boolean? = null
	override var ffValidAsTlvFirstByte: Boolean
		get() = checkNotNull(_ffValidAsTlvFirstByte)
		set(value) {
			_ffValidAsTlvFirstByte = value
		}

	private var _dataUnitsQuartets: Int? = null
	override var dataUnitsQuartets: Int
		get() = checkNotNull(_dataUnitsQuartets)
		set(value) {
			_dataUnitsQuartets = value
		}

	override fun build(): DataCodingDefinitions =
		DataCodingDefinitions(
			tlvEfs = tlvEfs,
			writeOneTime = writeOneTime,
			writeProprietary = writeProprietary,
			writeOr = writeOr,
			writeAnd = writeAnd,
			ffValidAsTlvFirstByte = ffValidAsTlvFirstByte,
			dataUnitsQuartets = dataUnitsQuartets,
		)
}
