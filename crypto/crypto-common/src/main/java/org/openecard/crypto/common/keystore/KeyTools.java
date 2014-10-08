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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.DSAKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import org.openecard.bouncycastle.crypto.tls.Certificate;


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


    /**
     * Converts the given certificate chain to a JCA CertPath.
     *
     * @param chain BouncyCastle certificates instance.
     * @return CertPath instance with the exact same certificate chain.
     * @throws CertificateException Thrown in case the JCA has problems supporting X509 or one of the certificates.
     * @throws IOException Thrown in case there is en encoding error.
     */
    public static CertPath convertCertificates(Certificate chain) throws CertificateException, IOException {
	return convertCertificates(chain.getCertificateList());
    }

    /**
     * Converts the given certificate chain to a JCA CertPath.
     *
     * @param chain BouncyCastle list of certificates.
     * @return CertPath instance with the exact same certificate chain.
     * @throws CertificateException Thrown in case the JCA has problems supporting X509 or one of the certificates.
     * @throws IOException Thrown in case there is en encoding error.
     */
    public static CertPath convertCertificates(org.openecard.bouncycastle.asn1.x509.Certificate... chain)
	    throws CertificateException, IOException {
	final int numCerts = chain.length;
	ArrayList<java.security.cert.Certificate> result = new ArrayList<>(numCerts);
	CertificateFactory cf = CertificateFactory.getInstance("X.509");

	for (org.openecard.bouncycastle.asn1.x509.Certificate next : chain) {
	    byte[] nextData = next.getEncoded();
	    ByteArrayInputStream nextDataStream = new ByteArrayInputStream(nextData);
	    java.security.cert.Certificate nextConverted = cf.generateCertificate(nextDataStream);
	    result.add(nextConverted);
	}

	return cf.generateCertPath(result);
    }

}
