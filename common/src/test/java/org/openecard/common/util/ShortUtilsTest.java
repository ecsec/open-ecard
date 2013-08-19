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
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ShortUtilsTest {

    @Test
    public void testToByteArray() {
	byte[] result = ShortUtils.toByteArray(Short.MAX_VALUE);
	byte[] expected = new byte[] { 0x7F, (byte) 0xFF };
	assertEquals(expected, result);

	result = ShortUtils.toByteArray(Short.MAX_VALUE, true);
	expected = new byte[] { 0x7F, (byte) 0xFF };
	assertEquals(expected, result);

	result = ShortUtils.toByteArray((short) 0, true);
	expected = new byte[] { 0x00, 0x00 };
	assertEquals(expected, result);

	result = ShortUtils.toByteArray((short) 0, false);
	expected = new byte[] { 0x00 };
	assertEquals(expected, result);

	// and a real life example
	result = ShortUtils.toByteArray((short) 0x9000);
	expected = new byte[] { (byte) 0x90, 0x00 };
	assertEquals(expected, result);
    }

}
