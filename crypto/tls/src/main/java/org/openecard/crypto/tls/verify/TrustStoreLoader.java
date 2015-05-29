/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.crypto.tls.verify;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class TrustStoreLoader {

    private static final Logger logger = LoggerFactory.getLogger(TrustStoreLoader.class);
    private static final String TRUSTSTORE_FILE = "oec_cacerts.p12";
    private static final String TRUSTSTORE_PASS = "changeit"; // same as default Java Truststore

    private static Set<TrustAnchor> trustAnchors = Collections.emptySet();

    static {
	load();
    }

    public static synchronized void load() {
	try {
	    String tmAlg = TrustManagerFactory.getDefaultAlgorithm();
	    TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(tmAlg);

	    // try to load internal keystore, if none is present or deactivated, fall back to system trust store
	    // the fallback is implicit
	    KeyStore ks = loadInternalStore();
	    tmFactory.init(ks);

	    // create trustmanager and extract trust anchors
	    HashSet<TrustAnchor> anchors = new HashSet<>();
	    TrustManager[] tms = tmFactory.getTrustManagers();
	    // pick first X509 tm
	    for (TrustManager tm : tms) {
		if (tm instanceof X509TrustManager) {
		    X509TrustManager x509Tm = (X509TrustManager) tm;
		    for (X509Certificate cert : x509Tm.getAcceptedIssuers()) {
			TrustAnchor ta = new TrustAnchor(cert, null);
			anchors.add(ta);
		    }
		}
	    }

	    if (anchors.isEmpty()) {
		// no hard fail nevertheless, validation will just not work
		logger.error("No trusted CAs found.");
	    }

	    trustAnchors = Collections.unmodifiableSet(anchors);
	} catch (NoSuchAlgorithmException | KeyStoreException ex) {
	    String msg = "Failed to create or initialize TrustManagerFactory.";
	    logger.error(msg, ex);
	    throw new RuntimeException(msg, ex);
	}
    }

    private static boolean useInternalStore() {
	String useSysStr = OpenecardProperties.getProperty("tls.truststore.use-system");
	// if not set, this should evaluate to true
	boolean useSys = Boolean.valueOf(useSysStr);
	return ! useSys;
    }

    private static KeyStore loadInternalStore() {
	if (useInternalStore()) {
	    try {
		// get stream to keystore
		InputStream is = FileUtils.resolveResourceAsStream(TrustStoreLoader.class, TRUSTSTORE_FILE);
		if (is != null) {
		    KeyStore ks = KeyStore.getInstance("PKCS12");
		    ks.load(is, TRUSTSTORE_PASS.toCharArray());
		    return ks;
		} else {
		    logger.error("Internal keystore not found, falling back to next available trust store.");
		}
	    } catch (IOException | NoSuchAlgorithmException | CertificateException ex) {
		logger.error("Error reading internal keystore.", ex);
	    } catch (KeyStoreException ex) {
		logger.error("PKCS#12 keystore type not supported by system.", ex);
	    }
	}
	// error or different keystore requested
	return null;
    }

    public static Set<TrustAnchor> getTrustAnchors() {
	return trustAnchors;
    }

}
