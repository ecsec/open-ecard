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
package org.openecard.crypto.tls.verify

import org.openecard.bouncycastle.tls.CertificateRequest
import org.openecard.bouncycastle.tls.TlsAuthentication
import org.openecard.bouncycastle.tls.TlsCredentials
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.bouncycastle.util.Strings
import org.openecard.crypto.common.ReusableSecureRandom
import org.openecard.crypto.tls.ClientCertDefaultTlsClient
import java.io.IOException

/**
 * Implementation of BouncyCastle's abstract DefaultTlsClient.
 *
 * @author Tobias Wich
 */
class DefaultTlsClientImpl(hostName: String) :
    ClientCertDefaultTlsClient(BcTlsCrypto(ReusableSecureRandom.instance), hostName, true) {
    @Throws(IOException::class)
	override fun getAuthentication(): TlsAuthentication {
        return object : TlsAuthentication {
            @Throws(IOException::class)
            override fun notifyServerCertificate(crtfct: TlsServerCertificate) {
                val v = JavaSecVerifier()

                val cv = CertificateVerifierBuilder()
                    .and(HostnameVerifier())
                    .and(v)
                    .and(KeyLengthVerifier())
                    .build()
                val hostname = Strings.fromUTF8ByteArray(serverNames[0].nameData)
                cv.isValid(crtfct, hostname)
            }

            @Throws(IOException::class)
            override fun getClientCredentials(cr: CertificateRequest): TlsCredentials {
                throw UnsupportedOperationException("Not supported yet.")
            }
        }
    }
}
