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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author René Lottes
 */
public enum ExtendedKeyUsage {
    SERVER_AUTH,
    CLIENT_AUTH,
    CODE_SIGNING,
    EMAIL_PROTECTION,
    OCSP_SIGNING;
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExtendedKeyUsage.class);

    public boolean hasUsage(X509Certificate x509cert) {
        try {
            List<String> extendedKeyUsage = x509cert.getExtendedKeyUsage();
        } catch (CertificateParsingException ex) {
            logger.error("Error parsing certificate", ex);
        }
        
        return false;
    }
}
