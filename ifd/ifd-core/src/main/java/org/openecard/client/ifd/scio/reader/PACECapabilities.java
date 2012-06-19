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

package org.openecard.client.ifd.scio.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACECapabilities {

    private final BitSet capabilities;

    public PACECapabilities(byte[] capabilitiesStructure) {
	if (capabilitiesStructure.length == 1) {
	    // special case for reiner sct readers
	    capabilities = makeBitSet(capabilitiesStructure);
	} else {
	    // standard way
	    byte length = capabilitiesStructure[0];
	    byte[] data = Arrays.copyOfRange(capabilitiesStructure, 1, length+1);
	    capabilities = makeBitSet(data);
	}
    }

    public List<Long> getFeatures() {
	List<Long> result = new ArrayList<Long>(capabilities.cardinality());
	for (int i = capabilities.nextSetBit(0); i >= 0; i = capabilities.nextSetBit(i+1)) {
	    // operate on index i here
	    result.add(Long.valueOf(1<<i));
	}
	return result;
    }

    private BitSet makeBitSet(byte[] d) {
        BitSet b = new BitSet(d.length*8);
        for (int i=0; i<d.length; i++) {
            byte next = d[i];
            for (int j=0; j<8; j++) {
                boolean isSet = ((next >> j) & 0x01) == 1;
                if (isSet) {
                    b.set((i*8)+j);
                }
            }
        }
        return b;
    }

}
