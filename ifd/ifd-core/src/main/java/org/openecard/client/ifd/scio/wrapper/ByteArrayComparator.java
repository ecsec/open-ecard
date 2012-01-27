/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ifd.scio.wrapper;

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
