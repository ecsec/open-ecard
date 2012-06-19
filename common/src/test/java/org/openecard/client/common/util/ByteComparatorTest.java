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

import java.util.Comparator;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ByteComparatorTest {

    @Test
    public void testCompare() {
	Comparator<byte[]> comp = new ByteComparator();
	byte[] a = new byte[] { 0x00, 0x01, 0x02 }, b = new byte[] { 0x03, 0x04, 0x05 };
	byte[] c = new byte[] { 0x00, 0x01, 0x02 }, d = new byte[] { 0x00 };

	Assert.assertEquals(-3, comp.compare(a, b));
	Assert.assertEquals(0, comp.compare(a, c));
	Assert.assertEquals(3, comp.compare(b, a));
	Assert.assertEquals(2, comp.compare(a, d));
	Assert.assertEquals(0, comp.compare(a, a));
	Assert.assertEquals(1, comp.compare(a, null));
	Assert.assertEquals(-1, comp.compare(null, a));
    }

}
