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

import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author René Lottes
 */
public enum SignatureUsage {
    DIGITAL_SIGNATURE(0),
    NON_REPUDIATION(1),
    KEY_CERT_SIGN(5),
    CRL_SIGN(6);

    private static final Logger logger = LoggerFactory.getLogger(SignatureUsage.class);
    
    private final int value;

    private SignatureUsage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean hasUsage(X509Certificate x509cert) {
        boolean[] keyUsage = x509cert.getKeyUsage();
        return keyUsage[this.getValue()];
    }
}
