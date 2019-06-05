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

package org.openecard.ifd.protocol.pace.crypto;

import java.math.BigInteger;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.openecard.crypto.common.asn1.eac.PACEDomainParameter;


/**
 * Implements the Generic Mapping for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section A.3.5.1.
 *
 * @author Moritz Horsch
 */
public final class PACEGenericMapping extends PACEMapping {

    // Mapping key for generic mapping.
    private PACEKey mapKey;

    /**
     * Creates an new generic mapping for PACE.
     *
     * @param pdp PACEDomainParameter
     */
    public PACEGenericMapping(PACEDomainParameter pdp) {
	super(pdp);

	mapKey = new PACEKey(pdp);
	mapKey.generateKeyPair();
    }

    @Override
    public PACEDomainParameter map(byte[] keyPICC, byte[] keyPCD) {
	if (pdp.isDH()) {
	    throw new UnsupportedOperationException("Not supported yet.");
	} else if (pdp.isECDH()) {
	    ECParameterSpec p = (ECParameterSpec) pdp.getParameter();
	    ECPoint pkMapPICC = p.getCurve().decodePoint(keyPICC);
	    BigInteger d = ((ECPrivateKeyParameters) mapKey.getPrivateKey()).getD();
	    BigInteger s = new BigInteger(1, keyPCD);

	    ECPoint h = pkMapPICC.multiply(p.getH().multiply(d));
	    ECPoint newG = p.getG().multiply(s).add(h);

	    ECParameterSpec parameter = new ECParameterSpec(p.getCurve(), newG, p.getN(), p.getH());
	    pdp.setParameter(parameter);

	    return pdp;
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Return the mapping key.
     *
     * @return Key
     */
    public PACEKey getMappingKey() {
	return mapKey;
    }

}
