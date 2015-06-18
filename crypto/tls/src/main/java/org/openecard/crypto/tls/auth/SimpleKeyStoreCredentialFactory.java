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

package org.openecard.crypto.tls.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;
import org.openecard.crypto.common.keystore.KeyStoreSigner;


/**
 * This factory simply returns the one credential it was given in the constructor.
 * 
 * @author Dirk Petrautzki
 */
public class SimpleKeyStoreCredentialFactory implements CredentialFactory {

    private final List<TlsCredentials> credentials;

    /**
     * Create a new factory for the given signer.
     * 
     * @param signer Keystore base signer that will be wrapped in the credential.
     */
    public SimpleKeyStoreCredentialFactory(KeyStoreSigner signer) {
	ArrayList<TlsCredentials> c = new ArrayList<TlsCredentials>(1);
    	c.add(new KeyStoreCredential(signer));
	credentials = Collections.unmodifiableList(c);
    }

    @Override
    @Nonnull
    public List<TlsCredentials> getClientCredentials(CertificateRequest cr) {
	return credentials;
    }

}
