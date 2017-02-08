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

package org.openecard.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import java.math.BigInteger;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.util.StringUtils;
import org.openecard.recognition.CardRecognitionImpl;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Dirk Petrautzki
 */
public class PinCompareMarkerTypeTest {

    private static final byte[] rootApplication = StringUtils.toByteArray("D2760001448000");
    private static final String cardType = "http://ws.gematik.de/egk/1.0.0";
    private static final String didName = "PIN.home";

    /**
     * Simple test for PinCompareMarkerType. After getting the PinCompareMarker for the PIN.home DID in the the root
     * application we check if the get-methods return the expected values.
     *
     * @throws Exception when something in this test went unexpectedly wrong
     */
    @Test
    public void testPinCompareMarkerType() throws Exception {
	Environment env = new ClientEnv();
	CardRecognitionImpl recognition = new CardRecognitionImpl(env);
	CardInfoType cardInfo = recognition.getCardInfo(cardType);
	CardInfoWrapper cardInfoWrapper = new CardInfoWrapper(cardInfo);

	DIDInfoType didInfoWrapper = cardInfoWrapper.getDIDInfo(didName, rootApplication);
	PINCompareMarkerType pinCompareMarker = new PINCompareMarkerType(
		 didInfoWrapper.getDifferentialIdentity().getDIDMarker().getPinCompareMarker());
	assertEquals(pinCompareMarker.getPINRef().getKeyRef(), new byte[] { 0x02 });
	assertNull(pinCompareMarker.getPINValue());
	assertEquals(pinCompareMarker.getPasswordAttributes().getMaxLength(), new BigInteger("8"));
	assertEquals(pinCompareMarker.getProtocol(), ECardConstants.Protocol.PIN_COMPARE);
    }

}
