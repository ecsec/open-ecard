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

package org.openecard.addons.cg.impl;

import java.util.EnumSet;
import org.openecard.crypto.common.SignatureAlgorithms;
import static org.openecard.crypto.common.SignatureAlgorithms.*;
import org.openecard.crypto.common.UnsupportedAlgorithmException;


/**
 *
 * @author Tobias Wich
 */
public class AllowedSignatureAlgorithms {

    private static final EnumSet<SignatureAlgorithms> ALLOWED_ALGS;

    static {
	ALLOWED_ALGS = EnumSet.of(
	    CKM_RSA_PKCS,
	    CKM_SHA1_RSA_PKCS,
	    CKM_SHA256_RSA_PKCS,
	    CKM_SHA384_RSA_PKCS,
	    CKM_RSA_PKCS_PSS,
	    CKM_SHA1_RSA_PKCS_PSS,
	    CKM_SHA256_RSA_PKCS_PSS,
	    CKM_SHA384_RSA_PKCS_PSS,
	    CKM_ECDSA,
	    CKM_ECDSA_SHA1,
	    CKM_ECDSA_SHA256,
	    CKM_ECDSA_SHA384
	);
    }

    public static String algIdtoJcaName(String algId) throws UnsupportedAlgorithmException {
	SignatureAlgorithms alg = SignatureAlgorithms.Companion.fromAlgId(algId);
	if (! ALLOWED_ALGS.contains(alg)) {
	    String msg = "The requested algorithm is not allowed in the ChipGateway protocol.";
	    throw new UnsupportedAlgorithmException(msg);
	}
	return alg.getJcaAlg();
    }

    public static boolean isKnownJcaAlgorithm(String jcaAlg) {
	try {
	    SignatureAlgorithms alg = SignatureAlgorithms.Companion.fromJcaName(jcaAlg);
	    return ALLOWED_ALGS.contains(alg);
	} catch (UnsupportedAlgorithmException ex) {
	    return false;
	}
    }

}
