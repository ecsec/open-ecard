/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.crypto.common.asn1.eac;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.bouncycastle.asn1.ASN1Integer;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.x9.X9ECParameters;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.math.ec.ECCurve;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class ProprietaryDomainParameters extends DomainParameters {

    /**
     * Instantiates a new proprietary domain parameters.
     *
     * @param seq the ASN1 encoded sequence
     */
    public ProprietaryDomainParameters(ASN1Sequence seq) {
//        ASN1Integer version = (ASN1Integer) seq.getObjectAt(0);
        ASN1Sequence modulus = (ASN1Sequence) seq.getObjectAt(1);
        ASN1Sequence coefficient = (ASN1Sequence) seq.getObjectAt(2);
//        ASN1OctetString basepoint = (ASN1OctetString) seq.getObjectAt(3);
        ASN1Integer order = (ASN1Integer) seq.getObjectAt(4);
        ASN1Integer cofactor = (ASN1Integer) seq.getObjectAt(5);

        try {
            BigInteger p = new BigInteger(modulus.getObjectAt(1).toASN1Primitive().getEncoded());
            BigInteger a = new BigInteger(coefficient.getObjectAt(0).toASN1Primitive().getEncoded());
            BigInteger b = new BigInteger(coefficient.getObjectAt(1).toASN1Primitive().getEncoded());
            BigInteger r = order.getValue();
            BigInteger f = cofactor.getValue();

            ECCurve curve = new ECCurve.Fp(p, a, b);

            X9ECParameters ECParameters = X9ECParameters.getInstance(seq);
            curve = ECParameters.getCurve();

            domainParameter = new ECParameterSpec(curve, ECParameters.getG(), r, f);

        } catch (IOException e) {
            Logger.getLogger("ASN1").log(Level.SEVERE, "Failed to load proprietary domain parameters", e);
        }
    }
}
