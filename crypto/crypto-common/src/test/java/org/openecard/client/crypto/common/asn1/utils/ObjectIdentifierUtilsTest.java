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

package org.openecard.client.crypto.common.asn1.utils;

import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ObjectIdentifierUtilsTest {

    @Test
    public void testToByteArray() throws Exception {
	String oid = EACObjectIdentifier.id_PACE;
	byte[] expResult = new byte[]{0x06, 0x08, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	byte[] result = ObjectIdentifierUtils.toByteArray(oid);
	assertEquals(expResult, result);
    }

    @Test
    public void testToString() throws Exception {
	byte[] oid = new byte[]{0x06, 0x08, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	String expResult = "0.4.0.127.0.7.2.2.4";
	String result = ObjectIdentifierUtils.toString(oid);
	assertEquals(expResult, result);
    }

    @Test
    public void testToString2() throws Exception {
	byte[] oid = new byte[]{0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	String expResult = "0.4.0.127.0.7.2.2.4";
	String result = ObjectIdentifierUtils.toString(oid);
	assertEquals(expResult, result);
    }

    @Test
    public void testGetValue() {
	String oid = EACObjectIdentifier.id_PACE;
	byte[] expResult = new byte[]{0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x04};
	byte[] result = ObjectIdentifierUtils.getValue(oid);
	assertEquals(expResult, result);
    }

}
