package org.openecard.cif.dsl.builder.dataset

import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.dataset.DataSetDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.cif.dsl.api.dataset.DataSetScope
import org.openecard.cif.dsl.builder.Builder
import org.openecard.cif.dsl.builder.acl.AclBuilder
import org.openecard.utils.serialization.PrintableUByteArray
import kotlin.coroutines.EmptyCoroutineContext.get

class DataSetBuilder :
	DataSetScope,
	Builder<DataSetDefinition> {
	private var _path: PrintableUByteArray? = null
	override var path: PrintableUByteArray
		get() = _path!!
		set(value) {
			_path = value
		}
	override var shortEf: UByte? = null
		set(value) {
			field =
				if (value != null) {
					if (value > 0x1Fu) {
						value.toInt().ushr(3).toUByte()
					} else {
						value
					}
				} else {
					value
				}
		}
	private var _name: String? = null
	override var name: String
		get() = _name!!
		set(value) {
			_name = value
		}
	private var _description: String? = null
	override var description: String
		get() = _description!!
		set(value) {
			_description = value
		}
	private var readAcl: AclDefinition? = null
	private var writeAcl: AclDefinition? = null

	override fun readAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		readAcl = builder.build()
	}

	override fun writeAcl(content: @CifMarker (AclScope.() -> Unit)) {
		val builder = AclBuilder()
		content(builder)
		writeAcl = builder.build()
	}

	override fun build(): DataSetDefinition =
		DataSetDefinition(
			name = name,
			path = path,
			shortEf = shortEf,
			description = description,
			readAcl = readAcl ?: AclDefinition(mapOf()),
			writeAcl = writeAcl ?: AclDefinition(mapOf()),
		)
}
