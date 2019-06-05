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

package org.openecard.crypto.tls;

import javax.annotation.Nonnull;
import org.bouncycastle.tls.TlsServerCertificate;


/**
 * Interface for certificate verification.
 *
 * @author Tobias Wich
 */
public interface CertificateVerifier {

    /**
     * Verify the given certificate chain.
     * An invalid certificate is indicated by a CertificateVerificationException.<br>
     * The verification must at least check the certificate chain and the hosts name.
     *
     * @param chain Certificate chain to be verified.
     * @param hostOrIP Name of the host or its IP address used in the validation.
     * @throws CertificateVerificationException Thrown in case the verification failed.
     */
    void isValid(@Nonnull TlsServerCertificate chain, @Nonnull String hostOrIP) throws CertificateVerificationException;

}
