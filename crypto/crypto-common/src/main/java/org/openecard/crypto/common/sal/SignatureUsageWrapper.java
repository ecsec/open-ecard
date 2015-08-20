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



public class SignatureUsageWrapper {
    
    public enum SignatureUsage {
        DIGITAL_SIGNATURE,
        NON_REPUDIATION,
        KEY_CERT_SIGN,
        CRL_SIGN;
        
        public boolean hasUsage(X509Certificate cert) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
    
    public enum ExtendedKeyUsage {
        SERVER_AUTH,
        CLIENT_AUTH,
        CODE_SIGNING,
        EMAIL_PROTECTION,
        OCSP_SIGNING;
        
        public boolean hasUsage(X509Certificate cert) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
    
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
    
    public boolean hasUsage(X509Certificate cert) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}
