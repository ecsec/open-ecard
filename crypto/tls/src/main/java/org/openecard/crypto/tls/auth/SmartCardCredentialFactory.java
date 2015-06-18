/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;
import org.openecard.crypto.common.sal.CredentialNotFound;
import org.openecard.crypto.common.sal.GenericCryptoSigner;
import org.openecard.crypto.common.sal.GenericCryptoSignerFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of CredentialFactory operating on generic crypto SAL DIDs.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class SmartCardCredentialFactory implements CredentialFactory {

    private static final Logger logger = LoggerFactory.getLogger(SmartCardCredentialFactory.class);

    private final GenericCryptoSignerFinder finder;
    private final List<TlsCredentials> credentials = new ArrayList<TlsCredentials>();

    public SmartCardCredentialFactory(@Nonnull GenericCryptoSignerFinder finder) {
	this.finder = finder;
    }

    @Override
    public List<TlsCredentials> getClientCredentials(CertificateRequest cr) {
	SmartCardSignerCredential cred;
	// TODO: clarify if the result needs to be cached or not (remove member in case)
	credentials.clear();
	try {
	    // TODO: just one? perhaps return a lazy list with all matches
	    GenericCryptoSigner result = finder.findFirstMatching(cr);
	    cred = new SmartCardSignerCredential(result);
	    credentials.add(cred);
	} catch (CredentialNotFound e) {
	    logger.error("No suitable credential found. Returning empty list.");
	}
	return credentials;
    }

}
