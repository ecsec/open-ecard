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
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.util;

import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Dirk Petrautzki
 */
public class ByteArrayWrapperTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testConstructor() {
	new ByteArrayWrapper(null);
    }

    @Test
    public void testCompareEquals() {
	byte[] a = new byte[] { 0x00, 0x01, 0x02 }, b = new byte[] { 0x03, 0x04, 0x05 };
	ByteArrayWrapper wrapA = new ByteArrayWrapper(a);
	ByteArrayWrapper wrapB = new ByteArrayWrapper(b);

	// test unequal
	assertNotEquals(wrapA, wrapB);
	// test equal
	assertEquals(wrapA, wrapA);
	// test not instance of ByteArrayWrapper
	assertNotEquals(wrapA, b);
    }

    @Test
    public void testHashCode() {
	byte[] a = new byte[] { 0x00, 0x01, 0x02 };
	byte[] b = new byte[] { 0x00, 0x01, 0x02 };
	ByteArrayWrapper wrapA = new ByteArrayWrapper(a);
	ByteArrayWrapper wrapB = new ByteArrayWrapper(b);
	assertEquals(wrapA.hashCode(), wrapB.hashCode());
    }

}
