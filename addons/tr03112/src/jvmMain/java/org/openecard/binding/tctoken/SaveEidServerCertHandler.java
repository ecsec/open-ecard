/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.DynamicContext;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Verifier saving the certificate in the dynamic context.
 *
 * @author Tobias Wich
 */
public class SaveEidServerCertHandler implements CertificateVerifier {

    boolean firstCert = true;

    @Override
    public void isValid(TlsServerCertificate chain, String hostOrIp) throws CertificateVerificationException {
	if (firstCert) {
	    firstCert = false;
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    dynCtx.put(TR03112Keys.EIDSERVER_CERTIFICATE, chain);
	}
    }

}
