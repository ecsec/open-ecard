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
 * @author Hans-Martin Haase
 */
public class AlgorithmResolver {

    /**
     * A HashMap which maps the algorithm string which contains an URI to the corresponding SignatureAndHashAlgorithm object.
     */
    private static final HashMap<String, SignatureAndHashAlgorithm> MAPPER;
    static {
	MAPPER = new HashMap<>();
	// pure RSA signature default hash in this case is SHA256
	MAPPER.put("http://www.w3.org/2001/04/xmlenc#rsa-1_5",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.rsa));
	// RSA with SHA1
	MAPPER.put("http://www.w3.org/2000/09/xmldsig#rsa-sha1",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha1, SignatureAlgorithm.rsa));
	// RSA with SHA256
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.rsa));
	// RSA with SHA384
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha384, SignatureAlgorithm.rsa));
	// RSA with SHA512
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha512, SignatureAlgorithm.rsa));
	// RSA with SHA224
	MAPPER.put("http://www.w3.org/2007/05/xmldsig-more#rsa-sha224",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha224, SignatureAlgorithm.rsa));
	// ECDSA with SHA1
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha1, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA224
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha224, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA256
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA384
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha384, SignatureAlgorithm.ecdsa));
	// ECDSA with SHA512
	MAPPER.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512",
		new SignatureAndHashAlgorithm(HashAlgorithm.sha512, SignatureAlgorithm.ecdsa));
    }

    /**
     * Get the SignatureAndHashAlgorithm object for a specific algorithm string.
     * <br>
     * <br>
     * The currently supported URIs are:<br> <br>
     * <ul type="square">
     * <li>http://www.w3.org/2001/04/xmlenc#rsa-1_5</li>
     * <li>http://www.w3.org/2000/09/xmldsig#rsa-sha1</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#rsa-sha256</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#rsa-sha384</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#rsa-sha512</li>
     * <li>http://www.w3.org/2007/05/xmldsig-more#rsa-sha224</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384</li>
     * <li>http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512</li>
     * </ul>
     *
     * @param algorithm A string which contains the URI of an algorithm for signature creation for TLS 1.2.
     * @return A SignatureAndHashAlgorithm object which corresponds to the <b>algorithm</b> parameter or {@code null}
     * if the parameter contains a non valid string or URI.
     */
    public static SignatureAndHashAlgorithm getSignatureAndHashFromAlgorithm(String algorithm) {
	return MAPPER.get(algorithm);
    }
    
}
