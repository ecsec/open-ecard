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

package org.openecard.client.sal.protocol.genericcryptography;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.sal.protocol.genericcryptography.GenericCryptoProtocol;
import org.openecard.client.sal.protocol.genericcryptography.GenericCryptoProtocolFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;


/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class GenericCryptographyProtocolFactoryTest {

    /**
     * Test if the Factory returns the right Protocol.
     */
    @Test
    public void test() {
	GenericCryptoProtocolFactory pinCompareProtocolFactory = new GenericCryptoProtocolFactory();
	assertEquals(pinCompareProtocolFactory.getProtocol(), ECardConstants.Protocol.GENERIC_CRYPTO);
	assertEquals(pinCompareProtocolFactory.createInstance(null, null).getClass(), GenericCryptoProtocol.class);
	assertEquals(pinCompareProtocolFactory.createInstance(null, null).toString(), "Generic cryptography");
    }

}
