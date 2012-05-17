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

    protected ECPublicKey(TLV key) throws Exception {
	this.key = key;

	List<TLV> bodyElements = key.getChild().asList();

	for (Iterator<TLV> it = bodyElements.iterator(); it.hasNext();) {
	    TLV item = it.next();
	    int itemTag = (int) item.getTagNumWithClass();

	    switch (itemTag) {
		case OID_TAG:
		    oid = ObjectIdentifierUtils.toString(key.findChildTags(OID_TAG).get(0).getValue());
		    break;
		case PRIME_TAG:
		    prime = new BigInteger(key.findChildTags(PRIME_TAG).get(0).getValue());
		    break;
		case COEFFICIENT_A_TAG:
		    a = new BigInteger(key.findChildTags(COEFFICIENT_A_TAG).get(0).getValue());
		    break;
		case COEFFICIENT_B_TAG:
		    b = new BigInteger(key.findChildTags(COEFFICIENT_B_TAG).get(0).getValue());
		    break;
		case BASE_POINT_TAG:
		    g = key.findChildTags(BASE_POINT_TAG).get(0).getValue();
		    break;
		case ORDER_TAG:
		    order = new BigInteger(key.findChildTags(ORDER_TAG).get(0).getValue());
		    break;
		case PUBLIC_POINT_TAG:
		    y = key.findChildTags(PUBLIC_POINT_TAG).get(0).getValue();
		    break;
		case COFACTOR_TAG:
		    h = new BigInteger(key.findChildTags(COFACTOR_TAG).get(0).getValue());
		    break;
		default:
		    break;
	    }
	}
	//TODO Verify me.
    }

    @Override
    public boolean equals(PublicKey pk) {
	try {
	    return ByteUtils.compare(getTLVEncoded().toBER(), pk.getTLVEncoded().toBER());
	} catch (TLVException ignore) {
	    return false;
	}
    }

    @Override
    public TLV getTLVEncoded() {
	return key;
    }

    public BigInteger getA() {
	return a;
    }

    public BigInteger getB() {
	return b;
    }

    public BigInteger getCofactor() {
	return h;
    }

    @Override
    public String getObjectIdentifier() {
	return oid;
    }

    public BigInteger getOrder() {
	return order;
    }

    public BigInteger getPrime() {
	return prime;
    }

    public byte[] getY() {
	return y;
    }

    public byte[] getBasePoint() {
	return g;
    }
}
