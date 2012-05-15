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

import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Dirk Petrautzki <petrauttzki@hs-coburg.de>
 * @author Johannes.Schmoelz
 */
public class IntegerUtilsTest {

    @Test
    public void testToByteArray() {
	byte[] expected = new byte[] { (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	assertArrayEquals(expected, IntegerUtils.toByteArray(Integer.MAX_VALUE));

	expected = new byte[] { 0x00 };
	assertArrayEquals(expected, IntegerUtils.toByteArray(0));

	expected = new byte[] { 0x00, 0x00, 0x00, 0x00 };
	assertArrayEquals(expected, IntegerUtils.toByteArray(0, true));

	expected = new byte[] { 0x00 };
	assertArrayEquals(expected, IntegerUtils.toByteArray(0, false));

	expected = new byte[] { (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	assertArrayEquals(expected, IntegerUtils.toByteArray(Integer.MAX_VALUE, true));

	expected = new byte[] { (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	assertArrayEquals(expected, IntegerUtils.toByteArray(Integer.MAX_VALUE, true));

	expected = new byte[] { 0x01, 0x00, 0x00, 0x00 };
	assertArrayEquals(expected, IntegerUtils.toByteArray(8, 1));

	expected = new byte[] { 0x03, 0x01 };
	assertArrayEquals(expected, IntegerUtils.toByteArray(0x61, 5));
    }

}
