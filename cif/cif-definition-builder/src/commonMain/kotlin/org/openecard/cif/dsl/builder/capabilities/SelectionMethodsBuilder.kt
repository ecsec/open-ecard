package org.openecard.cif.dsl.builder.capabilities

import org.openecard.cif.definition.capabilities.SelectionMethodsDefinition
import org.openecard.cif.dsl.api.capabilities.SelectionMethodsScope
import org.openecard.cif.dsl.builder.Builder

class SelectionMethodsBuilder :
	SelectionMethodsScope,
	Builder<SelectionMethodsDefinition> {
	private var _selectDfByFullName: Boolean? = null
	override var selectDfByFullName: Boolean
		get() = checkNotNull(_selectDfByFullName)
		set(value) {
			_selectDfByFullName = value
		}

	private var _selectDfByPartialName: Boolean? = null
	override var selectDfByPartialName: Boolean
		get() = checkNotNull(_selectDfByPartialName)
		set(value) {
			_selectDfByPartialName = value
		}

	private var _selectDfByPath: Boolean? = null
	override var selectDfByPath: Boolean
		get() = checkNotNull(_selectDfByPath)
		set(value) {
			_selectDfByPath = value
		}

	private var _selectDfByFileId: Boolean? = null
	override var selectDfByFileId: Boolean
		get() = checkNotNull(_selectDfByFileId)
		set(value) {
			_selectDfByFileId = value
		}

	private var _selectDfImplicit: Boolean? = null
	override var selectDfImplicit: Boolean
		get() = checkNotNull(_selectDfImplicit)
		set(value) {
			_selectDfImplicit = value
		}

	private var _supportsShortEf: Boolean? = null
	override var supportsShortEf: Boolean
		get() = checkNotNull(_supportsShortEf)
		set(value) {
			_supportsShortEf = value
		}

	private var _supportsRecordNumber: Boolean? = null
	override var supportsRecordNumber: Boolean
		get() = checkNotNull(_supportsRecordNumber)
		set(value) {
			_supportsRecordNumber = value
		}

	private var _supportsRecordIdentifier: Boolean? = null
	override var supportsRecordIdentifier: Boolean
		get() = checkNotNull(_supportsRecordIdentifier)
		set(value) {
			_supportsRecordIdentifier = value
		}

	override fun build(): SelectionMethodsDefinition =
		SelectionMethodsDefinition(
			selectDfByFullName = selectDfByFullName,
			selectDfByPartialName = selectDfByPartialName,
			selectDfByPath = selectDfByPath,
			selectDfByFileId = selectDfByFileId,
			selectDfImplicit = selectDfImplicit,
			supportsShortEf = supportsShortEf,
			supportsRecordNumber = supportsRecordNumber,
			supportsRecordIdentifier = supportsRecordIdentifier,
		)
}
