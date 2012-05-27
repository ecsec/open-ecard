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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.crypto.common.asn1.cvc;

import org.openecard.client.common.tlv.TLV;


/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class RSAPublicKey extends PublicKey {

    /**
     * Creates a new RSAPublicKey.
     *
     * @param tlv TLV encoded key
     */
    protected RSAPublicKey(TLV tlv) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getObjectIdentifier() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(PublicKey pk) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TLV getTLVEncoded() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
