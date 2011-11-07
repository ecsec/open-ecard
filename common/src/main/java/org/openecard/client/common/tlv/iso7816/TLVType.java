package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.TLV;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class TLVType {

    protected TLV tlv;

    private TLVType() { }
    public TLVType(TLV tlv) {
	this.tlv = tlv;
    }

}
