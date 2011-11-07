package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_OD {

    private List<CIOChoice> content;

    public EF_OD(TLV tlv) throws TLVException {
	List<TLV> tlvList = tlv.asList();
	content = new ArrayList<CIOChoice>(tlvList.size());
	for (TLV next : tlvList) {
	    content.add(new CIOChoice(next));
	}
    }

    public List<CIOChoice> getContent() {
        return content;
    }

}
