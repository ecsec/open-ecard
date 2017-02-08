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

package org.openecard.mdlw.sal;

import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.mdlw.sal.cryptoki.CK_MECHANISM_INFO;


/**
 *
 * @author Jan Mannsbart
 */
public class MwMechanism {

    private final CK_MECHANISM_INFO orig;
    private final long type;

    /**
     * Creates new Mechanism Object from given {@link CK_MECHANISM_INFO}
     * 
     * @param info
     * @param type
     * @throws UnsupportedAlgorithmException
     */
    public MwMechanism(CK_MECHANISM_INFO info, long type) throws UnsupportedAlgorithmException {
        this.orig = info;
        this.type = type;
    }

    public MwMechanism(long type) throws UnsupportedAlgorithmException {
	this.orig = null;
	this.type = type;
    }

    /**
     * Returns the Mechanism Type, for example CKM_SHA1_RSA_PKCS
     * 
     * @return String
     * @throws UnsupportedAlgorithmException Thrown in case the mechanism is not a known signature algorithm.
     */
    public SignatureAlgorithms getSignatureAlgorithm() throws UnsupportedAlgorithmException {
	return SignatureAlgorithms.fromMechanismId(type);
    }

    public boolean isSignatureAlgorithm() {
	try {
	    getSignatureAlgorithm();
	    return true;
	} catch (UnsupportedAlgorithmException ex) {
	    return false;
	}
    }

//    /**
//     * Returns the Mechanism Flags
//     * @return long
//     */
//    public long getFlags() {
//        return orig.getFlags().longValue();
//    }

    @Override
    public String toString() {
	try {
	    return getSignatureAlgorithm().toString();
	} catch (UnsupportedAlgorithmException ex) {
	    // bad luck
	}

	return "Mechanism: " + type;
    }

}
