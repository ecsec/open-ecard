/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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
 * @author Tobias Wich
 */
public class StringUtilsTest {

    @Test
    public void testToByteArray() {
	String hex = "00112233";
	String hex2 = "00             11        22 33";
	byte[] array = StringUtils.toByteArray(hex);
	byte[] expected = new byte[] { 0x00, 0x11, 0x22, 0x33 };
	assertEquals(expected, array);

	array = StringUtils.toByteArray(hex2, true);
	expected = new byte[] { 0x00, 0x11, 0x22, 0x33 };
	assertEquals(expected, array);

	array = StringUtils.toByteArray(hex, false);
	expected = new byte[] { 0x00, 0x11, 0x22, 0x33 };
	assertEquals(expected, array);
    }

    @Test
    public void testNullFunctions() {
	assertTrue(StringUtils.isNullOrEmpty(""));
	assertTrue(StringUtils.isNullOrEmpty(null));
	assertFalse(StringUtils.isNullOrEmpty(" "));
	assertFalse(StringUtils.isNullOrEmpty("foo"));

	assertNull(StringUtils.emptyToNull(""));
	assertNull(StringUtils.emptyToNull(null));
	assertEquals(StringUtils.emptyToNull(" "), " ");
	assertEquals(StringUtils.emptyToNull("foo"), "foo");

	assertEquals(StringUtils.nullToEmpty(null), "");
	assertEquals(StringUtils.nullToEmpty(""), "");
	assertEquals(StringUtils.nullToEmpty(" "), " ");
	assertEquals(StringUtils.nullToEmpty("foo"), "foo");
    }

}
