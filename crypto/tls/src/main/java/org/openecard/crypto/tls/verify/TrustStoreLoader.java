/****************************************************************************
 * Copyright (C) 2015-2016 ecsec GmbH.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

    private static final Logger LOG = LoggerFactory.getLogger(TrustStoreLoader.class);
    private static final String DEFAULT_TRUSTSTORE_FILE = "oec_cacerts.zip";

    private static final Map<String, KeyStore> TRUST_STORES = new HashMap<>();
    private static final Map<String, Set<TrustAnchor>> TRUST_ANCHORS = new HashMap<>();

    public static final void reset() {
	synchronized (TrustStoreLoader.class) {
	    TRUST_STORES.clear();
	    TRUST_ANCHORS.clear();
	}
    }

    protected String getStoreFileName() {
	return DEFAULT_TRUSTSTORE_FILE;
    }

    protected void load() {
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
		LOG.error("No trusted CAs found.");
	    }

	    synchronized (TrustStoreLoader.class) {
		TRUST_STORES.put(getStoreFileName(), ks);
		TRUST_ANCHORS.put(getStoreFileName(), Collections.unmodifiableSet(anchors));
	    }
	} catch (NoSuchAlgorithmException | KeyStoreException ex) {
	    String msg = "Failed to create or initialize TrustManagerFactory.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex);
	}
    }

    protected boolean useInternalStore() {
	String useSysStr = OpenecardProperties.getProperty("tls.truststore.use-system");
	// if not set, this should evaluate to true
	boolean useSys = Boolean.valueOf(useSysStr);
	return ! useSys;
    }

    protected KeyStore loadInternalStore() {
	if (useInternalStore()) {
	    // The internal keystore is a zip file containing DER encoded certificates.
	    // This is due to the following problems:
	    // - Java keystore not supported on Android
	    // - PKCS12 can not be used as truststore in Desktop Java
	    // - Bundled BC JCE has no Oracle signature and can thus not be used that way
	    try {
		// create keystore object to be able to save the certificates later
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		// get stream to zip file containing the certificates
		InputStream is = FileUtils.resolveResourceAsStream(TrustStoreLoader.class, getStoreFileName());
		ZipInputStream zis = is != null ? new ZipInputStream(is) : null;
		if (zis != null) {
		    ZipEntry entry;
		    while ((entry = zis.getNextEntry()) != null) {
			// only read entry if it is not a directory
			if (! entry.isDirectory()) {
			    ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    // read entry (certificates can not be longer than int)
			    byte[] data = new byte[4096];
			    int numRead;
			    do {
				numRead = zis.read(data, 0, 4096);
				if (numRead != -1) {
				    baos.write(data, 0, numRead);
				}
			    } while (numRead != -1);
			    // convert bytes to x509 cert
			    ByteArrayInputStream dataStream = new ByteArrayInputStream(baos.toByteArray());
			    Certificate cert = cf.generateCertificate(dataStream);
			    ks.setCertificateEntry(entry.getName(), cert);
			}
		    }

		    return ks;
		} else {
		    LOG.error("Internal keystore not found, falling back to next available trust store.");
		}
	    } catch (IOException ex) {
		LOG.error("Error reading embedded keystore.", ex);
	    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
		LOG.error("Failed to obtain keystore or save entry in it..", ex);
	    }
	}
	// error or different keystore requested
	return null;
    }

    public Set<TrustAnchor> getTrustAnchors() {
	Set<TrustAnchor> result = TRUST_ANCHORS.get(getStoreFileName());
	if (result != null) {
	    return result;
	} else {
	    // load truststore and try again
	    load();
	    return getTrustAnchors();
	}
    }

    public KeyStore getTrustStore() {
	KeyStore result = TRUST_STORES.get(getStoreFileName());
	if (result != null) {
	    return result;
	} else {
	    // load truststore and try again
	    load();
	    return getTrustStore();
	}
    }

}
