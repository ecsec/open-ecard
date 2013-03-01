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

package org.openecard.ifd.scio.wrapper;

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
