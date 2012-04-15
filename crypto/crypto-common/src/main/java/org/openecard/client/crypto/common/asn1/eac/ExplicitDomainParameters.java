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

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.bouncycastle.asn1.ASN1Integer;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.x9.X9ECParameters;
import org.openecard.bouncycastle.jce.spec.ECParameterSpec;
import org.openecard.bouncycastle.math.ec.ECCurve;


/**
 * See BSI-TR-03110, version 2.10, part 3, section A.2.1.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ExplicitDomainParameters extends DomainParameters {

    /**
     * dhpublicnumber OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) ansi-x942(10046) number-type(2) 1}
     */
    public static final String dhpublicnumber = "1.2.840.10046.2.1";
    /**
     * ecPublicKey OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) ansi-x962(10045) keyType(2) 1}
     */
    public static final String ecPublicKey = "1.2.840.10045.2.1";

    /**
     * Creates new ExplicitDomainParameters.
     *
     * @param ai AlgorithmIdentifier
     */
    public ExplicitDomainParameters(AlgorithmIdentifier ai) {
	String oid = ai.getObjectIdentifier();
	if (oid.equals(dhpublicnumber)) {
	    loadDHParameter((ASN1Sequence) ai.getParameters());
	} else if (oid.equals(ecPublicKey)) {
	    loadECDHParameter((ASN1Sequence) ai.getParameters());
	} else {
	    throw new IllegalArgumentException("Cannot parse explicit domain parameters");
	}
    }

    private void loadECDHParameter(ASN1Sequence seq) {
//        ASN1Integer version = (ASN1Integer) seq.getObjectAt(0);
//        ASN1Sequence modulus = (ASN1Sequence) seq.getObjectAt(1);
//        ASN1Sequence coefficient = (ASN1Sequence) seq.getObjectAt(2);
//        ASN1OctetString basepoint = (ASN1OctetString) seq.getObjectAt(3);
	ASN1Integer order = (ASN1Integer) seq.getObjectAt(4);
	ASN1Integer cofactor = (ASN1Integer) seq.getObjectAt(5);

	try {
//            BigInteger p = new BigInteger(modulus.getObjectAt(1).toASN1Primitive().getEncoded());
//            BigInteger a = new BigInteger(coefficient.getObjectAt(0).toASN1Primitive().getEncoded());
//            BigInteger b = new BigInteger(coefficient.getObjectAt(1).toASN1Primitive().getEncoded());
	    BigInteger r = order.getValue();
	    BigInteger f = cofactor.getValue();

	    X9ECParameters ECParameters = X9ECParameters.getInstance(seq);
	    ECCurve curve = ECParameters.getCurve();

	    domainParameter = new ECParameterSpec(curve, ECParameters.getG(), r, f);

	} catch (Exception e) {
	    Logger.getLogger(ExplicitDomainParameters.class.getName()).log(Level.SEVERE, "Failed to load proprietary domain parameters", e);
	}
    }

    private void loadDHParameter(ASN1Sequence seq) {
	throw new UnsupportedOperationException("Not implemented yet");
    }

}
