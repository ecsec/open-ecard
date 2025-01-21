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
 ***************************************************************************/
package org.openecard.ifd.scio.reader

/**
 *
 * @author Tobias Wich
 */
class EstablishPACEResponse(response: ByteArray) {

    val status: ByteArray
    private var efCardAccessLength: Short
    val eFCardAccess: ByteArray

    // eID attributes
    private var currentCARLength: Byte = 0
    val currentCAR: ByteArray?
    private var previousCARLength: Byte = 0
    val previousCAR: ByteArray?
    private var idiccLength: Short = 0
    val iDICC: ByteArray?

    init {
        val dataLen = response.size
        var idx = 4
        // read status
        this.status = response.copyOfRange(0, 2)
        // read card access (& 0xFF produces unsigned numbers)
        efCardAccessLength = ((response[2].toInt() and 0xFF) + ((response[3].toInt() and 0xFF) shl 8)).toShort()
        eFCardAccess = if (efCardAccessLength > 0) {
            idx += efCardAccessLength.toInt()
			response.copyOfRange(idx, idx + efCardAccessLength)
        } else {
			// TODO: check if this is correct or an error would be better
            ByteArray(0)
        }
        // read car
		currentCAR = if (dataLen > idx + 1) {
            currentCARLength = (response[idx].toInt() and 0xFF).toByte()
            idx++
            if (currentCARLength > 0) {
                idx += currentCARLength.toInt()
				response.copyOfRange(idx, idx + currentCARLength)
            } else {
				null
			}
		} else {
			null
		}
		// read car prev
		previousCAR = if (dataLen > idx + 1) {
            previousCARLength = (response[idx].toInt() and 0xFF).toByte()
            idx++
            if (previousCARLength > 0) {
                idx += previousCARLength.toInt()
				response.copyOfRange(idx, idx + previousCARLength)
            } else {
				null
			}
		} else {
			null
		}
		// read id icc
		this.iDICC = if (dataLen > idx + 2) {
            idiccLength = ((response[idx].toInt() and 0xFF) + ((response[idx + 1].toInt() and 0xFF) shl 8)).toShort()
            idx += 2
            if (idiccLength > 0) {
                idx += idiccLength.toInt()
				response.copyOfRange(idx, idx + idiccLength)
            } else {
				null
			}
        } else {
			null
		}
	}

    val retryCounter: Byte
        get() {
            // TODO: verify that retry counter is extracted from 63CX statusword
			return if (this.status[0].toInt() == 0x63 && (this.status[1].toInt() and 0xF0) == 0xC0) {
				(this.status[1].toInt() and 0x0F).toByte()
			} else {
				// TODO: check if 3 is ok as default and if any other statuswords must be considered here
				// default 3 seems to make sense
				3
			}
        }

    fun hasEFCardAccess(): Boolean {
        return efCardAccessLength > 0
    }

    fun hasCurrentCAR(): Boolean {
        return currentCARLength > 0
    }

    fun hasPreviousCAR(): Boolean {
        return previousCARLength > 0
    }

    fun hasIDICC(): Boolean {
        return idiccLength > 0
    }
}
