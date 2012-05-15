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

package org.openecard.client.common.util;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ShortUtilsTest {

    @Test
    public void testToByteArray() {
	byte[] result = ShortUtils.toByteArray((short) Short.MAX_VALUE);
	byte[] expected = new byte[] { 0x7F, (byte) 0xFF };
	Assert.assertArrayEquals(expected, result);

	result = ShortUtils.toByteArray((short) Short.MAX_VALUE, true);
	expected = new byte[] { 0x7F, (byte) 0xFF };
	Assert.assertArrayEquals(expected, result);

	result = ShortUtils.toByteArray((short) 0, true);
	expected = new byte[] { 0x00, 0x00 };
	Assert.assertArrayEquals(expected, result);

	result = ShortUtils.toByteArray((short) 0, false);
	expected = new byte[] { 0x00 };
	Assert.assertArrayEquals(expected, result);

	try {
	    expected = new byte[] { 0x01, 0x00, 0x00, 0x00 };
	    assertArrayEquals(expected, ShortUtils.toByteArray((short) -8, 5));
	    fail("A negative value for value should give an IllegalArgumentException");
	} catch (IllegalArgumentException e) {
	    /* expected */
	}
    }

}
