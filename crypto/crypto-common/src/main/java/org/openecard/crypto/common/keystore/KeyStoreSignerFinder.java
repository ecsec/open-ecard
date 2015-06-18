/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import java.security.KeyStore;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.asn1.x509.Certificate;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.crypto.common.sal.CredentialNotFound;


/**
 * Utility class that helps determine a KeyStore entry for signature creation.
 * The class is instantiated with a specific KeyStore. Afterwards it can look for entries with
 * different search strategies.
 *
 * @author Dirk Petrautzki
 */
public class KeyStoreSignerFinder {

    private final KeyStore keystore;

    public KeyStoreSignerFinder(KeyStore keyStore) {
	this.keystore = keyStore;
    }

    @Nonnull
    public KeyStoreSigner findFirstMatching(@Nonnull Certificate[] caChain) {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public KeyStoreSigner findFirstMatching(@Nonnull org.openecard.bouncycastle.crypto.tls.Certificate caChain)
	    throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public KeyStoreSigner findFirstMatching(@Nonnull java.security.cert.Certificate[] caChain)
	    throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public KeyStoreSigner findFirstMatching(@Nonnull CertificateRequest cr)
	    throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }
    // TODO: add more useful search functions

}
