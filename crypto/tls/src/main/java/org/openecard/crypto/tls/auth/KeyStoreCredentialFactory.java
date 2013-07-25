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
import org.openecard.crypto.common.keystore.KeyStoreSignerFinder;
import org.openecard.crypto.common.sal.CredentialNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of CredentialFactory operating on key stores.
 *
 * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
 */
public class KeyStoreCredentialFactory implements CredentialFactory {

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreCredentialFactory.class);

    private final KeyStoreSignerFinder finder;

    public KeyStoreCredentialFactory(@Nonnull KeyStoreSignerFinder finder) {
	this.finder = finder;
    }

    @Override
    public List<TlsCredentials> getClientCredentials(CertificateRequest cr) {
	ArrayList<TlsCredentials> credentials = new ArrayList<TlsCredentials>(1);
	try {
	    KeyStoreCredential cred = new KeyStoreCredential(finder.findFirstMatching(cr));
	    credentials.add(cred);
	} catch (CredentialNotFound e) {
	    logger.error("No suitable credential found. Returning empty list.");
	}
	return Collections.unmodifiableList(credentials);
    }

}
