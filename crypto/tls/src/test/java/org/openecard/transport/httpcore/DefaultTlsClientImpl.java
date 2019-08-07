/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.transport.httpcore;

import java.io.IOException;
import org.bouncycastle.tls.CertificateRequest;
import org.bouncycastle.tls.DefaultTlsClient;
import org.bouncycastle.tls.TlsAuthentication;
import org.bouncycastle.tls.TlsCredentials;
import org.bouncycastle.tls.TlsServerCertificate;
import org.bouncycastle.tls.crypto.TlsCrypto;


/**
 * Implementation of BouncyCastle's abstract DefaultTlsClient.
 *
 * @author Tobias Wich
 */
public class DefaultTlsClientImpl extends DefaultTlsClient {

    public DefaultTlsClientImpl(TlsCrypto crypto) {
	super(crypto);
    }

    @Override
    public TlsAuthentication getAuthentication() throws IOException {
	return new TlsAuthentication() {
	    @Override
	    public void notifyServerCertificate(TlsServerCertificate serverCertificate) throws IOException {
		// ignore
	    }
	    @Override
	    public TlsCredentials getClientCredentials(CertificateRequest cr) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	};
    }

}
