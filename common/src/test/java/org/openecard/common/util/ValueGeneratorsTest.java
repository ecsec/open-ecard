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
public class ValueGeneratorsTest {

    @Test
    public void testGeneratePSK() {
	String psk = ValueGenerators.generatePSK();
	assertEquals(psk.length(), 64);
    }

    @Test
    public void testGenHexSession() {
	String session = ValueGenerators.genHexSession();
	assertEquals(session.length(), 32);
	session = ValueGenerators.genHexSession(64);
	assertEquals(session.length(), 64);
    }

    @Test
    public void testGenBase64Session() {
	String session = ValueGenerators.genBase64Session();
	assertEquals(session.length(), 22);
	session = ValueGenerators.genBase64Session(64);
	assertEquals(session.length(), 43);
    }

    @Test
    public void testGenerateRandomHex() {
	String randomHex = ValueGenerators.generateRandomHex(40);
	assertEquals(randomHex.length(), 40);
    }

    @Test
    public void testGenerateUUID() {
	String uuid = ValueGenerators.generateUUID();
	assertTrue(uuid.startsWith("urn:uuid:"));
	assertEquals(uuid.length(), 45);
    }

}
