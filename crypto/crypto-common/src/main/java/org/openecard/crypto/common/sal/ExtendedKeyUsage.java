/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of SkIDentity.
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.crypto.common.sal;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.openecard.bouncycastle.asn1.x509.KeyPurposeId;
import org.slf4j.LoggerFactory;


/**
 *
 * @author René Lottes
 */
public enum ExtendedKeyUsage {
    SERVER_AUTH(KeyPurposeId.id_kp_serverAuth.getId()),
    CLIENT_AUTH(KeyPurposeId.id_kp_clientAuth.getId()),
    CODE_SIGNING(KeyPurposeId.id_kp_codeSigning.getId()),
    EMAIL_PROTECTION(KeyPurposeId.id_kp_emailProtection.getId()),
    OCSP_SIGNING(KeyPurposeId.id_kp_OCSPSigning.getId());

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExtendedKeyUsage.class);

    private final String str;

    private ExtendedKeyUsage(String str) {
	this.str = str;
    }

    public String getValue() {
	return str;
    }

    public boolean hasUsage(X509Certificate x509cert) {
	try {
	    List<String> extendedKeyUsage = x509cert.getExtendedKeyUsage();

	    if (extendedKeyUsage != null) {
		return extendedKeyUsage.contains(this.getValue());
	    }

	} catch (CertificateParsingException ex) {
	    logger.error("Error parsing certificate", ex);
	}

	return false;
    }
}
