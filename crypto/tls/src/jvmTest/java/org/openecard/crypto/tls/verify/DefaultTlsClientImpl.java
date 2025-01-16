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

package org.openecard.crypto.tls.verify;

import java.io.IOException;
import org.openecard.bouncycastle.tls.CertificateRequest;
import org.openecard.bouncycastle.tls.TlsAuthentication;
import org.openecard.bouncycastle.tls.TlsCredentials;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto;
import org.openecard.bouncycastle.util.Strings;
import org.openecard.crypto.common.ReusableSecureRandom;
import org.openecard.crypto.tls.CertificateVerifier;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;


/**
 * Implementation of BouncyCastle's abstract DefaultTlsClient.
 *
 * @author Tobias Wich
 */
public class DefaultTlsClientImpl extends ClientCertDefaultTlsClient {

	public DefaultTlsClientImpl(String hostName) {
		super(new BcTlsCrypto(ReusableSecureRandom.instance), hostName, true);
	}

	@Override
	public TlsAuthentication getAuthentication() throws IOException {
		return new TlsAuthentication() {
			@Override
			public void notifyServerCertificate(TlsServerCertificate crtfct) throws IOException {
				JavaSecVerifier v = new JavaSecVerifier();

				CertificateVerifier cv = new CertificateVerifierBuilder()
					.and(new HostnameVerifier())
					.and(v)
					.and(new KeyLengthVerifier())
					.build();
				var hostname = Strings.fromUTF8ByteArray(serverNames.get(0).getNameData());
				cv.isValid(crtfct, hostname);
			}

			@Override
			public TlsCredentials getClientCredentials(CertificateRequest cr) throws IOException {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}

}
