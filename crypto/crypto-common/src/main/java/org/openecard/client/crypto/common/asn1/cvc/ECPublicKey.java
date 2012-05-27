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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;


/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ECPublicKey extends PublicKey {

    private static final int PRIME_TAG = 0x81;
    private static final int COEFFICIENT_A_TAG = 0x82;
    private static final int COEFFICIENT_B_TAG = 0x83;
    private static final int BASE_POINT_TAG = 0x84;
    private static final int ORDER_TAG = 0x85;
    private static final int PUBLIC_POINT_TAG = 0x86;
    private static final int COFACTOR_TAG = 0x87;
    //
    private TLV key;
    private String oid;
    private BigInteger prime;
    private BigInteger a;
    private BigInteger b;
    private byte[] g;
    private BigInteger order;
    private byte[] y;
    private BigInteger h;

    /**
     * Creates a new ECPublicKey.
     *
     * @param key Key
     * @throws Exception
     */
    protected ECPublicKey(TLV key) throws Exception {
	this.key = key;

	List<TLV> bodyElements = key.getChild().asList();

	for (Iterator<TLV> it = bodyElements.iterator(); it.hasNext();) {
	    TLV item = it.next();
	    int itemTag = (int) item.getTagNumWithClass();

	    switch (itemTag) {
		case OID_TAG:
		    // MANDATORY
		    oid = ObjectIdentifierUtils.toString(key.findChildTags(OID_TAG).get(0).getValue());
		    break;
		case PRIME_TAG:
		    // CONDITIONAL
		    prime = new BigInteger(key.findChildTags(PRIME_TAG).get(0).getValue());
		    break;
		case COEFFICIENT_A_TAG:
		    // CONDITIONAL
		    a = new BigInteger(key.findChildTags(COEFFICIENT_A_TAG).get(0).getValue());
		    break;
		case COEFFICIENT_B_TAG:
		    // CONDITIONAL
		    b = new BigInteger(key.findChildTags(COEFFICIENT_B_TAG).get(0).getValue());
		    break;
		case BASE_POINT_TAG:
		    // CONDITIONAL
		    g = key.findChildTags(BASE_POINT_TAG).get(0).getValue();
		    break;
		case ORDER_TAG:
		    // CONDITIONAL
		    order = new BigInteger(key.findChildTags(ORDER_TAG).get(0).getValue());
		    break;
		case PUBLIC_POINT_TAG:
		    // MANDATORY
		    y = key.findChildTags(PUBLIC_POINT_TAG).get(0).getValue();
		    break;
		case COFACTOR_TAG:
		    // CONDITIONAL
		    h = new BigInteger(key.findChildTags(COFACTOR_TAG).get(0).getValue());
		    break;
		default:
		    break;
	    }
	}
	verify();
    }

    private void verify() {
	// Object identifier and public point are MANDATORY
	if (oid == null || y == null) {
	    throw new IllegalArgumentException("Malformed ECPublicKey");
	}
	// CONDITIONAL domain parameters MUST be either all present, except the cofactor, or all absent
	if (prime == null || a == null || b == null || g == null || order == null) {
	    if (prime != null || a != null || b != null || g != null || order != null) {
		throw new IllegalArgumentException("Malformed ECPublicKey");
	    }
	}
    }

    @Override
    public TLV getTLVEncoded() {
	return key;
    }

    /**
     * Returns the coefficient A.
     *
     * @return Coefficient A
     */
    public BigInteger getA() {
	return a;
    }

    /**
     * Returns the coefficient B.
     *
     * @return Coefficient B
     */
    public BigInteger getB() {
	return b;
    }

    /**
     * Returns the cofactor.
     *
     * @return Cofactor
     */
    public BigInteger getCofactor() {
	return h;
    }

    @Override
    public String getObjectIdentifier() {
	return oid;
    }

    /**
     * Returns the order.
     *
     * @return Order
     */
    public BigInteger getOrder() {
	return order;
    }

    /**
     * Returns the prime number.
     *
     * @return Prime number.
     */
    public BigInteger getPrime() {
	return prime;
    }

    /**
     * Returns the public point.
     *
     * @return Public point
     */
    public byte[] getY() {
	return y;
    }

    /**
     * Returns the base point.
     *
     * @return Base point
     */
    public byte[] getBasePoint() {
	return g;
    }

}
