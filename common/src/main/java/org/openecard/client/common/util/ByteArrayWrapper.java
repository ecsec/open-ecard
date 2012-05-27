/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.common.util;

import java.util.Arrays;


/**
 * To use byte arrays as keys in hashmaps
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class ByteArrayWrapper
{
    private final byte[] data;

    // TODO: REMOVE ME Klasse wird nicht verwendet!

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
