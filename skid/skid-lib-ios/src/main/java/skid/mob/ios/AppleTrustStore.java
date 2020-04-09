/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.ios;

import com.android.org.conscrypt.TrustedCertificateStore;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tobias Wich
 */
public class AppleTrustStore {

    private static final TrustedCertificateStore ts;
    private static final Map<String, Certificate> certs;

    static {
	ts = new TrustedCertificateStore();
	certs = new HashMap<>();

	for (String alias : ts.aliases()) {
	    Certificate cert = ts.getCertificate(alias);
	    certs.put(alias, cert);
	}
    }

    public static KeyStore getTrustStore() throws KeyStoreException {
	KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	for (Map.Entry<String, Certificate> e : certs.entrySet()) {
	    ks.setCertificateEntry(e.getKey(), e.getValue());
	}
	return ks;
    }

}
