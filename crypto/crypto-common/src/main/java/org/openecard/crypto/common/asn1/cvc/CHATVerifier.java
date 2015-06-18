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

package org.openecard.crypto.common.asn1.cvc;

import java.security.GeneralSecurityException;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Moritz Horsch
 */
public class CHATVerifier {

    /**
     * Verifies that the second CHAT is a subset of the first one.
     * Ensures that the second CHAT does not have move rights then the first one.
     *
     * @param firstCHAT First CHAT
     * @param secondCHAT Second CHAT
     * @throws GeneralSecurityException
     */
    public static void verfiy(CHAT firstCHAT, CHAT secondCHAT) throws GeneralSecurityException {
	try {
	    byte[] firstCHATBytes = firstCHAT.toByteArray();
	    byte[] secondCHATBytes = secondCHAT.toByteArray();

	    for (int i = 0; i < firstCHATBytes.length * 8; i++) {
		if (ByteUtils.isBitSet(i, secondCHATBytes) && !ByteUtils.isBitSet(i, firstCHATBytes)) {
		    throw new GeneralSecurityException("The second CHAT is not a subset of the first one");
		}
	    }
	} catch (Exception e) {
	    throw new GeneralSecurityException(e);
	}
    }

}
