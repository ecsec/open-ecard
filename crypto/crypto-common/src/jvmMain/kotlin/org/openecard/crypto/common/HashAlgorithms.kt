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

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.openecard.bouncycastle.asn1.x509.X509ObjectIdentifiers

/**
 *
 * @author Tobias Wich
 */
enum class HashAlgorithms(
	val pkcs11Mechanism: Long,
	val jcaAlg: String,
	val algId: String,
	val oid: ASN1ObjectIdentifier,
) {
	//    CKM_RIPEMD128 (0x00000230L),
	//    CKM_RIPEMD160 (0x00000240L),
	//    CKM_MD2       (0x00000200L, "MD2"),
	//    CKM_MD5       (0x00000210L, "MD5", "http://www.w3.org/2001/04/xmldsig-more#md5"),
	CKM_SHA_1(0x00000220L, "SHA-1", "http://www.w3.org/2000/09/xmldsig#sha1", X509ObjectIdentifiers.id_SHA1),
	CKM_SHA256(0x00000250L, "SHA-256", "http://www.w3.org/2001/04/xmlenc#sha256", NISTObjectIdentifiers.id_sha256),
	CKM_SHA224(
		0x00000255L,
		"SHA-224",
		"http://www.w3.org/2001/04/xmldsig-more#sha224",
		NISTObjectIdentifiers.id_sha224,
	),
	CKM_SHA384(
		0x00000260L,
		"SHA-384",
		"http://www.w3.org/2001/04/xmldsig-more#sha384",
		NISTObjectIdentifiers.id_sha384,
	),
	CKM_SHA512(
		0x00000270L,
		"SHA-512",
		"http://www.w3.org/2001/04/xmlenc#sha512",
		NISTObjectIdentifiers.id_sha512,
	), //    CKM_SHA512_T  (0x00000050L);
}
