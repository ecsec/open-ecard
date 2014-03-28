/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.crypto.common.sal;

import java.util.HashMap;
import org.openecard.bouncycastle.crypto.tls.HashAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.openecard.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;


/**
 * Utility class which provides a mapping of an signature hash algorithm OID on a {@link SignatureAndHashAlgorithm} object.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AlgorithmResolver {

    /**
     * A HashMap which maps the algorithm string which contains a oid to the corresponding SignatureAndHashAlgorithm object.
     */
    private static final HashMap<String, SignatureAndHashAlgorithm> MAPPER;
    static {
	MAPPER = new HashMap<String, SignatureAndHashAlgorithm>();
	// The current SignerFinder resolves pure RSA with a wrong oid. To be compatible
	// until the oid is corrected we have to add this wrong oid.
	MAPPER.put("urn:oid:1.2.840.113549.1.1", new
	SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.rsa));
	// pure RSA signature default hash in this case is SHA256
	MAPPER.put("urn:oid:1.2.840.113549.1.1.1", new
	SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.rsa));
	// RSA with SHA1
	MAPPER.put("urn:oid:1.2.840.113549.1.1.5",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha1, SignatureAlgorithm.rsa));
	// RSA with SHA256
	MAPPER.put("urn:oid:1.2.840.113549.1.1.11",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.rsa));
	// RSA with SHA384
	MAPPER.put("urn:oid:1.2.840.113549.1.1.12",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha384, SignatureAlgorithm.rsa));
	// RSA with SHA512
	MAPPER.put("urn:oid:1.2.840.113549.1.1.13",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha512, SignatureAlgorithm.rsa));
	// RSA with SHA224
	MAPPER.put("urn:oid:1.2.840.113549.1.1.14",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha224, SignatureAlgorithm.rsa));
	// ECDSA with SHA1
	MAPPER.put("urn:oid:1.2.840.10045.4.1",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha1, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA224
	MAPPER.put("urn:oid:1.2.840.10045.4.3.1",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha224, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA256
	MAPPER.put("urn:oid:1.2.840.10045.4.3.2",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA384
	MAPPER.put("urn:oid:1.2.840.10045.4.3.3",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha384, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA512
	MAPPER.put("urn:oid:1.2.840.10045.4.3.4",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha512, SignatureAlgorithm.ecdsa));
	// DSA with SHA224
	MAPPER.put("urn:oid:2.16.840.1.101.3.4.3.1",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha224, SignatureAlgorithm.dsa));
	// DSA with SHA256
	MAPPER.put("urn:oid:2.16.840.1.101.3.4.3.2",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.dsa));
    }

    /**
     * Get the SignatureAndHashAlgorithm object for a specific algorithm string.
     * <br />
     * <br />
     * The currently supported OIDs are:<br /> <br />
     * <ul type="square">
     * <li>urn:oid:1.2.840.113549.1.1	 </ li>
     * <li>urn:oid:1.2.840.113549.1.1.1	 </ li>
     * <li>urn:oid:1.2.840.113549.1.1.5	 </ li>
     * <li>urn:oid:1.2.840.113549.1.1.11 </ li>
     * <li>urn:oid:1.2.840.113549.1.1.12 </ li>
     * <li>urn:oid:1.2.840.113549.1.1.13 </ li>
     * <li>urn:oid:1.2.840.113549.1.1.14 </ li>
     * <li>urn:oid:1.2.840.10045.4.1	 </ li>
     * <li>urn:oid:1.2.840.10045.4.3.1	 </ li>
     * <li>urn:oid:1.2.840.10045.4.3.2	 </ li>
     * <li>urn:oid:1.2.840.10045.4.3.3	 </ li>
     * <li>urn:oid:1.2.840.10045.4.3.4	 </ li>
     * <li>urn:oid:2.16.840.1.101.3.4.3.1</ li>
     * <li>urn:oid:2.16.840.1.101.3.4.3.2</ li>
     * </ ul>
     *
     * @param algorithm A string which contains the OID of an algorithm for signature creation for TLS 1.2.
     * @return A SignatureAndHashAlgorithm object which corresponds to the <b>algorithm</b> parameter or NULL if the
     * parameter contains a non valid string or OID.
     */
    public static SignatureAndHashAlgorithm getSignatureAndHashFromAlgorithm(String algorithm) {
	return MAPPER.get(algorithm);
    }
    
}
