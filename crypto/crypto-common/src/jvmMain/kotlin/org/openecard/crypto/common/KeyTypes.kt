/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.crypto.common

/**
 * List of key types.
 *
 * @author Tobias Wich
 */
enum class KeyTypes(
	val pkcs11Mechanism: Long,
	val jcaAlg: String,
) {
	CKK_RSA(0x00000000L, "RSA"),
	CKK_DSA(0x00000001L, "DSA"),
	CKK_DH(0x00000002L, "DiffieHellman"),
	CKK_EC(0x00000003L, "EC"),
	//    CKK_X9_42_DH       (0x00000004UL, ),
	//    CKK_KEA            (0x00000005UL, ),
	//    CKK_GENERIC_SECRET (0x00000010UL, ),
	//    CKK_RC2            (0x00000011UL, ),
	//    CKK_RC4            (0x00000012UL, ),
	//    CKK_DES            (0x00000013UL, "DES"),
	//    CKK_DES2           (0x00000014UL, ),
	//    CKK_DES3           (0x00000015UL, "DESede),
	//    CKK_CAST           (0x00000016UL, ),
	//    CKK_CAST3          (0x00000017UL, ),
	//    CKK_CAST128        (0x00000018UL, ),
	//    CKK_RC5            (0x00000019UL, ),
	//    CKK_IDEA           (0x0000001AUL, ),
	//    CKK_SKIPJACK       (0x0000001BUL, ),
	//    CKK_BATON          (0x0000001CUL, ),
	//    CKK_JUNIPER        (0x0000001DUL, ),
	//    CKK_CDMF           (0x0000001EUL, ),
	//    CKK_AES            (0x0000001FUL, ),
	//    CKK_BLOWFISH       (0x00000020UL, ),
	//    CKK_TWOFISH        (0x00000021UL, ),
	//    CKK_SECURID        (0x00000022UL, ),
	//    CKK_HOTP           (0x00000023UL, ),
	//    CKK_ACTI           (0x00000024UL, ),
	//    CKK_CAMELLIA       (0x00000025UL, ),
	//    CKK_ARIA           (0x00000026UL, );
}
