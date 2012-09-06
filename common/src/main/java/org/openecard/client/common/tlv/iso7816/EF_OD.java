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

package org.openecard.client.common.tlv.iso7816;

import java.util.ArrayList;
import java.util.List;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;


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
