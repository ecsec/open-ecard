package org.openecard.client.common.util;

import java.util.Arrays;


/**
 * To use byte arrays as keys in hashmaps
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public final class ByteArrayWrapper
{
    private final byte[] data;

    //TODO REMOVE ME Klasse wird nicht verwendet!
    
    public ByteArrayWrapper(byte[] data) {
	if (data == null) {
	    throw new NullPointerException();
	}
	this.data = data;
    }

    @Override
    public boolean equals(Object other) {
	if (!(other instanceof ByteArrayWrapper)) {
	    return false;
	}
	return Arrays.equals(data, ((ByteArrayWrapper)other).data);
    }

    @Override
    public int hashCode() {
	return Arrays.hashCode(data);
    }

}
