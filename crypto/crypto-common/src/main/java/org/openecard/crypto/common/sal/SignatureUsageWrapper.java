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
public class SignatureUsageWrapper {
    
    private static final Logger logger = LoggerFactory.getLogger(SignatureUsageWrapper.class);
    
    private SignatureUsage keyUsage;
    private ExtendedKeyUsage extendedKeyUsage;
    
    public SignatureUsageWrapper(SignatureUsage usage) {
        this.keyUsage = usage;
    }
    
    public SignatureUsageWrapper(SignatureUsage usage, ExtendedKeyUsage extUsage) {
        this.keyUsage = usage;
        this.extendedKeyUsage = extUsage;
    }
    
    public SignatureUsageWrapper(ExtendedKeyUsage extUsage) {
        this.extendedKeyUsage = extUsage;
    }
    
    public boolean hasUsage(X509Certificate x509cert) {
        if (keyUsage != null && extendedKeyUsage != null) {
            return (keyUsage.hasUsage(x509cert) & extendedKeyUsage.hasUsage(x509cert));
        } else if (keyUsage != null) {
            return keyUsage.hasUsage(x509cert);
        } else if (extendedKeyUsage != null) {
            return extendedKeyUsage.hasUsage(x509cert);
        }
        return false;
    }
    
}
