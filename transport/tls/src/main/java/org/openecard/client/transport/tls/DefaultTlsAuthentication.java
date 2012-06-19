/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.transport.tls;

import java.io.IOException;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.TlsAuthentication;
import org.openecard.bouncycastle.crypto.tls.TlsCredentials;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DefaultTlsAuthentication implements TlsAuthentication {

    private TlsCredentials tlsCredentials;

    public DefaultTlsAuthentication(TlsCredentials tlsCredentials) {
	this.tlsCredentials = tlsCredentials;
    }

    @Override
    public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
	return this.tlsCredentials;
    }

    @Override
    public void notifyServerCertificate(Certificate certificate) throws IOException {
	/* ignore */
    }

}
