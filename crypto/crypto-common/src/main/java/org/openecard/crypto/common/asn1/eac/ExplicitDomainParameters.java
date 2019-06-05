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

package org.openecard.crypto.common.asn1.eac;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * See BSI-TR-03110, version 2.10, part 3, section A.2.1.2.
 *
 * @author Moritz Horsch
 */
public class ExplicitDomainParameters extends DomainParameters {

    private static final Logger _logger = LoggerFactory.getLogger(ExplicitDomainParameters.class);

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
	    _logger.error("Failed to load proprietary domain parameters", e);
	}
    }

    private void loadDHParameter(ASN1Sequence seq) {
	throw new UnsupportedOperationException("Not implemented yet");
    }

}
