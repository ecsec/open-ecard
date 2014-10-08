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

package org.openecard.crypto.common.keystore;

import java.math.BigInteger;
import java.security.Key;
import java.security.interfaces.DSAKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;


/**
 * Class with helper functions regarding cryptographic keys.+
 *
 * @author Tobias Wich
 */
public class KeyTools {

    public static int getKeySize(Key key) {
	if (key instanceof RSAKey) {
	    RSAKey rsaPk = (RSAKey) key;
	    BigInteger mod = rsaPk.getModulus();
	    return mod.bitLength();
	} else if (key instanceof DSAKey) {
	    DSAKey dsaPk = (DSAKey) key;
	    BigInteger p = dsaPk.getParams().getP();
	    return p.bitLength();
	} else if (key instanceof ECKey) {
	    ECKey ecPk = (ECKey) key;
	    BigInteger order = ecPk.getParams().getOrder();
	    return order.bitLength();
	} else if (key instanceof DHKey) {
	    DHKey dhKey = (DHKey) key;
	    BigInteger p = dhKey.getParams().getP();
	    return p.bitLength();
	} else if (key instanceof SecretKey) {
	    SecretKey sKey = (SecretKey) key;
	    if ("RAW".equals(sKey.getFormat())) {
		byte[] data = sKey.getEncoded();
		if (data != null) {
		    return data.length * 8;
		}
	    }
	}
	// unkown or inaccessible key (e.g. on secure storage device)
	return -1;
    }

}
