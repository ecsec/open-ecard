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

import static org.junit.Assert.*;
import org.junit.Test;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ValueGeneratorsTest {

    @Test
    public void testGeneratePSK() {
	String psk = ValueGenerators.generatePSK();
	assertEquals(64, psk.length());
    }

    @Test
    public void testGenerateSessionID() {
	String session = ValueGenerators.generateSessionID();
	assertEquals(32, session.length());
    }

    @Test
    public void testGenerateRandomHex() {
	String randomHex = ValueGenerators.generateRandomHex(40);
	assertEquals(40, randomHex.length());
    }

    @Test
    public void testGenerateUUID() {
	String uuid = ValueGenerators.generateUUID();
	assertTrue(uuid.startsWith("urn:uuid:"));
	assertEquals(45, uuid.length());
    }

}
