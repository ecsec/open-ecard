package org.openecard.client.crypto.common.asn1.cvc;

import org.openecard.client.common.tlv.TLV;


/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class DHPublicKey extends PublicKey {

    /**
     * Creates a new DHPublicKey.
     *
     * @param tlv TLV encoded key
     */
    protected DHPublicKey(TLV tlv) {
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
