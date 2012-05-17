/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.crypto.common.asn1.cvc;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;


/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class PublicKey {

    protected static final int OID_TAG = 0x06;

    public static PublicKey getInstance(byte[] key) throws Exception {
	return getInstance(TLV.fromBER(key));
    }

    public static PublicKey getInstance(TLV key) throws Exception {
	try {
	    String oid = ObjectIdentifierUtils.toString(key.findChildTags(OID_TAG).get(0).getValue());

	    //TODO Bedingungen überprüfen!!!
	    if (oid.startsWith(TAObjectIdentifier.id_TA_ECDSA)) {
		return new ECPublicKey(key);
	    } else if (oid.startsWith(TAObjectIdentifier.id_TA_RSA)) {
		return new RSAPublicKey(key);
	    }
	    throw new IllegalArgumentException("Malformed public key");
	} catch (Exception e) {
	    throw new IllegalArgumentException("Malformed public key: " + e.getMessage());
	}
    }

    public abstract String getObjectIdentifier();

    public abstract boolean equals(PublicKey pk);

    public abstract TLV getTLVEncoded();
}
