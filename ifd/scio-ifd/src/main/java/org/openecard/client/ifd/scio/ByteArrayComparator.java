package org.openecard.client.ifd.scio;

import java.util.Comparator;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
class ByteArrayComparator implements Comparator<byte[]> {

    @Override
    public int compare(byte[] o1, byte[] o2) {
	int minLen = Math.min(o1.length, o2.length);
	// compare elements
	for (int i=0; i < minLen; i++) {
	    if (o1[i] != o2[i]) {
		return o1[i] - o2[i];
	    }
	}
	// compare length of arrays
	if (o1.length != o2.length) {
	    return o1.length - o2.length;
	}
	// equal
	return 0;
    }

}
