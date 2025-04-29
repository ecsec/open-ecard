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
 *
 * @author Tobias Wich
 */
class DataElements(dataElements: List<ByteArray>) {
    private val dataElements: MutableList<DataElement> = ArrayList(dataElements.size)

    init {
        for (next in dataElements) {
            this.dataElements.add(DataElement(next))
        }
    }


    /**
     * FileDescriptorBytes combined - some of them must be seen as a whole (linear and record)
     */
    fun shareable(): Boolean {
        // only one needed to state this claim
        if (!dataElements.isEmpty()) {
            return dataElements[0].fileDescriptorByte.shareable()
        }
        return false
    }


    val isDF: Boolean
        get() {
            // only one needed to state this claim
            if (!dataElements.isEmpty()) {
                return dataElements[0].fileDescriptorByte.isDF
            }
            return false
        }

    val isEF: Boolean
        get() {
            // only one needed to state this claim
            if (!dataElements.isEmpty()) {
                return dataElements[0].fileDescriptorByte.isEF
            }
            return false
        }

    val isWorkingEF: Boolean
        get() {
            // only one needed to state this claim
            if (!dataElements.isEmpty()) {
                return dataElements[0].fileDescriptorByte.isWorkingEF
            }
            return false
        }

    val isInternalEF: Boolean
        get() {
            // only one needed to state this claim
            if (!dataElements.isEmpty()) {
                return dataElements[0].fileDescriptorByte.isInternalEF
            }
            return false
        }

    val isUnknownFormat: Boolean
        get() {
            // only one needed to state this claim
            if (!dataElements.isEmpty()) {
                return dataElements[0].fileDescriptorByte.isUnknownFormat
            }
            return false
        }

    val isTransparent: Boolean
        get() {
            for (next in dataElements) {
                if (next.fileDescriptorByte.isTransparent) {
                    return true
                }
            }
            return false
        }
    val isLinear: Boolean
        get() {
            for (next in dataElements) {
                if (next.fileDescriptorByte.isLinear) {
                    return true
                }
            }
            return false
        }
    val isCyclic: Boolean
        get() {
            for (next in dataElements) {
                if (next.fileDescriptorByte.isCyclic) {
                    return true
                }
            }
            return false
        }
    val isDataObject: Boolean
        get() {
            for (next in dataElements) {
                if (next.fileDescriptorByte.isDataObject) {
                    return true
                }
            }
            return false
        }


    // TODO: implement other aggregating functions when needed (e.g. for DataCodingByte or record size)
    fun toString(prefix: String): String {
        val b = StringBuilder(4096)
        b.append("DataElements:")
        for (next in dataElements) {
            b.append("\n")
            b.append(next.toString("$prefix "))
        }
        return b.toString()
    }

    override fun toString(): String {
        return toString("")
    }
}
