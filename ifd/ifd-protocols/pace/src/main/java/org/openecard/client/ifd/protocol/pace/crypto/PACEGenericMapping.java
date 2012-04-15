/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace.crypto;

import java.math.BigInteger;
import org.openecard.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.math.ec.ECPoint;


/**
 * Implements the Generic Mapping for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section A.3.5.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
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
