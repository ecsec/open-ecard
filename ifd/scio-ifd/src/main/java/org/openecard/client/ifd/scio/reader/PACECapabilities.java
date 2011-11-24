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
