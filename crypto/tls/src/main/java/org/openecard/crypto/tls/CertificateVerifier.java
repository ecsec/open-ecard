/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

import org.openecard.bouncycastle.crypto.tls.Certificate;


/**
 * Interface for certificate verification.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface CertificateVerifier {

    /**
     * Verify the given certificate chain.
     * An invalid certificate is indicated by a CertificateVerificationException.<br/>
     * The verification must at least check the certificate chain.
     *
     * @param chain Certificate chain to be verified.
     * @throws CertificateVerificationException Thrown in case the verification failed.
     */
    void isValid(Certificate chain) throws CertificateVerificationException;
    /**
     * Verify the given certificate chain.
     * An invalid certificate is indicated by a CertificateVerificationException.<br/>
     * The verification must at least check the certificate chain and the hosts name.
     *
     * @param chain Certificate chain to be verified.
     * @param hostname Name of the host used in the validation. Null if hostname is not available.
     * @throws CertificateVerificationException Thrown in case the verification failed.
     */
    void isValid(Certificate chain, String hostname) throws CertificateVerificationException;

}
