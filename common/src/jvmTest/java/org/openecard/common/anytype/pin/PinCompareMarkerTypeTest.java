/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

package org.openecard.common.anytype.pin;

import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.math.BigInteger;
import javax.xml.transform.stream.StreamSource;
import org.openecard.common.ECardConstants;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class PinCompareMarkerTypeTest {

    /**
     * Simple test for PinCompareMarkerType. After getting the PinCompareMarker for the PIN.home DID in the the root
     * application we check if the get-methods return the expected values.
     *
     * @throws Exception when something in this test went unexpectedly wrong
     */
    @Test
    public void testPinCompareMarkerType() throws Exception {
	JAXBContext ctx = JAXBContext.newInstance(PinCompareMarkerType.class);
	Unmarshaller um = ctx.createUnmarshaller();
	InputStream res = getClass().getResourceAsStream("/anytype/pin/egk_pin_home_marker.xml");
	JAXBElement<PinCompareMarkerType> elem = um.unmarshal(new StreamSource(res), PinCompareMarkerType.class);

	PINCompareMarkerType pinCompareMarker = new PINCompareMarkerType(elem.getValue());
	assertEquals(pinCompareMarker.getPINRef().getKeyRef(), new byte[] { 0x02 });
	assertNull(pinCompareMarker.getPINValue());
	assertEquals(pinCompareMarker.getPasswordAttributes().getMaxLength(), new BigInteger("8"));
	assertEquals(pinCompareMarker.getProtocol(), ECardConstants.Protocol.PIN_COMPARE);
    }

}
