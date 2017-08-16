/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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

import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;


/**
 * Interface of a factory for TlsCredentials
 *
 * @author Tobias Wich
 */
public interface CredentialFactory {

    /**
     * Gets all credentials matching the given certificate request.
     * Given a set of credentials the factory manages, filter out all that do NOT match the given certificate request.
     *
     * @param cr Certificate request for which a credential is searched.
     * @return Possibly empty list of all credentials which could answer the given request.
     */
    @Nonnull
    List<TlsSignerCredentials> getClientCredentials(CertificateRequest cr);

}
