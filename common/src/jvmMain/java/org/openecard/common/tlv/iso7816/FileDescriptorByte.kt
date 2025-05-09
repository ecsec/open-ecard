/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.common.tlv.iso7816

/**
 * See ISO/IEC 7816-4 p.21 tab. 14
 *
 * @author Tobias Wich
 */
class FileDescriptorByte(
	private val data: Byte,
) {
	private val isFD: Boolean
		// test msb
		get() = ((data.toInt() shr 7) and 0x01) != 1

	fun shareable(): Boolean {
		if (isFD) {
			// bit 7
			return ((data.toInt() shr 6) and 0x01) == 1
		}
		return false
	}

	val isDF: Boolean
		get() {
			if (isFD) {
				// 111000
				return (data.toInt() and 0x38) == 0x38
			}
			return false
		}

	val isWorkingEF: Boolean
		get() {
			if (isFD) {
				return ((data.toInt() shr 3) and 0x07) == 0
			}
			return false
		}
	val isInternalEF: Boolean
		get() {
			if (isFD) {
				return ((data.toInt() shr 3) and 0x07) == 1
			}
			return false
		}
	val isProprietaryEF: Boolean
		get() {
			if (isFD) {
				val `val` = ((data.toInt() shr 3) and 0x07).toByte()
				return `val` < 0x07 && `val`.toInt() != 0 && `val`.toInt() != 1
			}
			return false
		}

	val isEF: Boolean
		get() = isWorkingEF || isInternalEF || isProprietaryEF

	val isUnknownFormat: Boolean
		get() {
			if (isEF && ((data.toInt() and 0x07) == 0x00)) {
				return true
			}
			return false
		}
	val isTransparent: Boolean
		get() {
			if (isEF && ((data.toInt() and 0x07) == 0x01)) {
				return true
			}
			return false
		}
	val isLinear: Boolean
		get() {
			if (isEF) {
				val lower = (data.toInt() and 0x07).toByte()
				if (lower.toInt() == 2 || lower.toInt() == 3 || lower.toInt() == 4 || lower.toInt() == 5) {
					return true
				}
			}
			return false
		}
	val isCyclic: Boolean
		get() {
			if (isEF) {
				val lower = (data.toInt() and 0x07).toByte()
				if (lower.toInt() == 6 || lower.toInt() == 7) {
					return true
				}
			}
			return false
		}

	val isDataObject: Boolean
		get() {
			// 0X111010 || 0X111001
			if (isFD && ((data.toInt() shr 3) and 0x07) == 0x07) {
				val lower = (data.toInt() and 0x07).toByte()
				if (lower.toInt() == 1 || lower.toInt() == 2) {
					return true
				}
			}
			return false
		}

	fun toString(prefix: String?): String {
		val b = StringBuilder(4096)
		b.append(prefix)
		b.append("FileDescriptor-Byte: ")
		if (shareable()) {
			b.append("shareable ")
		}
		if (isDF) {
			b.append("DF ")
		}
		if (isEF) {
			b.append("EF ")
		}
		if (isInternalEF) {
			b.append("internal ")
		}
		if (isWorkingEF) {
			b.append("working ")
		}
		if (isProprietaryEF) {
			b.append("proprietary ")
		}
		if (isUnknownFormat) {
			b.append("unknown-format ")
		}
		if (isTransparent) {
			b.append("transparent ")
		}
		if (isLinear) {
			b.append("linear ")
		}
		if (isCyclic) {
			b.append("cyclic ")
		}
		if (isDataObject) {
			b.append("data-object ")
		}

		return b.toString()
	}

	override fun toString(): String = toString("")
}
