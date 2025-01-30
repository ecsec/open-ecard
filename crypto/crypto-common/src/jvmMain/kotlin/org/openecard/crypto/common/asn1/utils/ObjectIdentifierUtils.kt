/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.crypto.common.asn1.utils

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import java.io.IOException

/**
 * Helper class to convert ASN1 object identifier.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
object ObjectIdentifierUtils {
    /**
     * Converts a ASN1 object identifier to a byte array
     *
     * @param oid String
     * @return TLV encoded object identifier
     * @throws TLVException
     */
    @JvmStatic
	@Throws(TLVException::class)
    fun toByteArray(oid: String): ByteArray {
        try {
            return ASN1ObjectIdentifier(oid).encoded
        } catch (ex: IllegalArgumentException) {
            throw TLVException("Failed to parse OID.", ex)
        } catch (ex: IOException) {
            throw TLVException("Failed to parse OID.", ex)
        }
    }

    /**
     * Converts a ASN1 object identifier in byte array form to a String.
     *
     * @param oid Object Identifier either as TLV structure or plain.
     * @return Object Identifier string.
     * @throws TLVException
     */
    @JvmStatic
	@Throws(TLVException::class)
    fun toString(oid: ByteArray): String {
        try {
            var obj: TLV
            try {
                obj = TLV.fromBER(oid)
            } catch (ex: TLVException) {
                // read as plain value
                obj = TLV()
                obj.tagNum = 0x06
				obj.value = oid
            }
            return ASN1ObjectIdentifier.getInstance(obj.toBER()).id
        } catch (ex: IllegalArgumentException) {
            throw TLVException("Failed to parse OID.", ex)
        }
    }

    /**
     * Converts a ASN1 object identifier to a byte array.
     * Returns only the value without the length and 0x06 tag.
     *
     * @param oid String
     * @return Value of the object identifier
     * @throws TLVException
     */
    @JvmStatic
	@Throws(TLVException::class)
    fun getValue(oid: String): ByteArray {
        try {
            val oidObj = ASN1ObjectIdentifier(oid)
            val oidTlv = TLV.fromBER(oidObj.encoded)
            return oidTlv.value
        } catch (ex: IllegalArgumentException) {
            throw TLVException("Failed to parse OID.", ex)
        } catch (ex: IOException) {
            throw TLVException("Failed to parse OID.", ex)
        }
    }
}
