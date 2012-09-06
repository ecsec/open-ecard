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

package org.openecard.client.common.apdu.common;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardResponseAPDUTest {

    @Test
    public void testGetSW1() {
	byte[] apdu = new byte[]{(byte) 0x63, (byte) 0xC2};
	CardResponseAPDU instance = new CardResponseAPDU(apdu);
	byte expResult = 99;
	byte result = instance.getSW1();
	assertEquals(expResult, result);
    }

    @Test
    public void testGetSW2() {
	byte[] apdu = new byte[]{(byte) 0x63, (byte) 0xC2};
	CardResponseAPDU instance = new CardResponseAPDU(apdu);
	byte expResult = (byte) 194;
	byte result = instance.getSW2();
	assertEquals(expResult, result);
    }

    @Test
    public void testGetSW() {
	byte[] apdu = new byte[]{(byte) 0x63, (byte) 0xC2};
	CardResponseAPDU instance = new CardResponseAPDU(apdu);
	short expResult = 25538;
	short result = instance.getSW();
	assertEquals(expResult, result);
    }

}
