/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Java Security based certificate verifier. <br/>
 * This implementation converts the BouncyCastle certificates to java.security certificates and uses the Java-bundled
 * mechanisms to verify the certificate chain.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class JavaSecVerifier implements CertificateVerifier {

    private final KeyStore keyStore;
    private final CertPathValidator certPathValidator;

    /**
     * Create a JavaSecVerifier and load the system keystore.
     *
     * @throws KeyStoreException Keystore type could not be instantiated.
     * @throws FileNotFoundException Keystore was not found in standard locations.
     * @throws IOException Error loading keystore from disc.
     * @throws GeneralSecurityException Error processing loaded keystore.
     */
    public JavaSecVerifier() throws IOException, GeneralSecurityException {
	keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	keyStore.load(null); // initialize keystore
	KeyStore tmpKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	certPathValidator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());

	// determine system keystore
	final String fSep = File.separator;
	File keyStoreFile;
	// try system property
	keyStoreFile = getKeystore(System.getProperty("java.home"), "lib" + fSep + "security" + fSep + "cacerts");

	// load file
	if (keyStoreFile != null) {
	    tmpKeyStore.load(new FileInputStream(keyStoreFile), null); // system keystore has no password protection
	} else {
	    // TODO: this is either on android or it doesn' work at all
	    throw new FileNotFoundException("Unable to find system keystore in standard locations.");
	}

	addKeyStore(tmpKeyStore);
    }


    private File getKeystore(String prefix, String fileName) {
	if (fileName == null) {
	    return null;
	}
	if (prefix != null) {
	    fileName = prefix + File.separator + fileName;
	}
	File f = new File(fileName);
	if (! f.canRead()) {
	    return null;
	}
	return f;
    }


    /**
     * Merge the given keystore into the one enclosed in this verifier instance.
     *
     * @param keyStore Keystore to merge.
     * @throws KeyStoreException In case access to the given keystore is not possible.
     */
    public final void addKeyStore(KeyStore keyStore) throws KeyStoreException {
	Enumeration aliases = keyStore.aliases();
	while(aliases.hasMoreElements()) {
	    String alias = (String) aliases.nextElement();
	    if (keyStore.isCertificateEntry(alias)) {
		java.security.cert.Certificate cert = keyStore.getCertificate(alias);
		this.keyStore.setCertificateEntry(alias, cert);
	    }
	}
    }

    /**
     * Merge all given keystores into the one enclosed in this verifier instance.
     * @param keyStores Keystores to merge.
     * @throws KeyStoreException In case access to the given keystore is not possible.
     */
    public final void addKeyStore(List<KeyStore> keyStores) throws KeyStoreException {
	for (KeyStore next : keyStores) {
	    addKeyStore(next);
	}
    }


    @Override
    public void isValid(Certificate chain, String hostname) throws CertificateVerificationException {
	try {
	    CertPath certPath = convertChain(chain);

	    // create the parameters for the validator
	    PKIXParameters params = new PKIXParameters(keyStore);

	    // disable CRL checking since we are not supplying any CRLs yet
	    params.setRevocationEnabled(false);

	    // validate - exception marks failure
	    certPathValidator.validate(certPath, params);
	} catch (CertPathValidatorException ex) {
	    throw new CertificateVerificationException(ex.getMessage());
	} catch (GeneralSecurityException ex) {
	    throw new CertificateVerificationException(ex.getMessage());
	} catch (IOException ex) {
	    throw new CertificateVerificationException("Error converting certificate chain to java.security format.");
	}
    }


    private CertPath convertChain(Certificate chain) throws CertificateException, IOException {
	final int numCerts = chain.getCertificateList().length;
	ArrayList<java.security.cert.Certificate> result = new ArrayList<>(numCerts);
	CertificateFactory cf = CertificateFactory.getInstance("X.509");

	for (org.openecard.bouncycastle.asn1.x509.Certificate next : chain.getCertificateList()) {
	    byte[] nextData = next.getEncoded();
	    ByteArrayInputStream nextDataStream = new ByteArrayInputStream(nextData);
	    java.security.cert.Certificate nextConverted = cf.generateCertificate(nextDataStream);
	    result.add(nextConverted);
	}

	return cf.generateCertPath(result);
    }

}
