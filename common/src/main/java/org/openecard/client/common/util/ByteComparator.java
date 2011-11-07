package org.openecard.client.common.util;

import java.util.Comparator;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ByteComparator implements Comparator<byte[]> {

    @Override
    public int compare(byte[] o1, byte[] o2) {
	if (o1 == o2) {
	    return 0;
	}
	if (o1 == null) {
	    return -1;
	}
	if (o2 == null) {
	    return 1;
	}
	if (o1.length != o2.length) {
	    return o1.length - o2.length;
	}

	for (int i = 0; i < o1.length; i++) {
	    // use int so no overflow is possible
	    int b1 = o1[i];
	    int b2 = o2[i];
	    if (b1 != b2) {
		return b1 - b2;
	    }
	}

	// equal arrays
	return 0;
    }

}
