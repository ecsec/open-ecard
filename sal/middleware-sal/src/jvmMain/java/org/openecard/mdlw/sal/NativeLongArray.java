/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;


/**
 *
 * @author Tobias Wich
 */
public class NativeLongArray {
    
    private final int numElements;
    private final Memory mem;

    public NativeLongArray(int size) {
	if (size < 0) {
	    throw new IllegalArgumentException("Negative value supplied to array allocation.");
	}
	numElements = size;
	mem = new Memory(NativeLong.SIZE * size);
    }

    public Memory getReference() {
	return mem;
    }

    public long getValue(int idx) {
	if (idx < 0 || idx >= numElements) {
	    throw new ArrayIndexOutOfBoundsException(idx);
	}

	return mem.getNativeLong(idx * NativeLong.SIZE).longValue();
    }

    long[] getValues(int numValues) {
	if (numValues < 0 || numValues > numElements) {
	    throw new ArrayIndexOutOfBoundsException(numValues);
	}

	long[] result = new long[numValues];
	for (int i = 0; i < numValues; i++) {
	    result[i] = getValue(i);
	}

	return result;
    }

}
