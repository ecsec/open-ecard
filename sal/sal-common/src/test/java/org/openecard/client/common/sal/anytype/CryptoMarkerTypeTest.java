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

package org.openecard.client.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import org.openecard.client.recognition.CardRecognition;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CryptoMarkerTypeTest {

    @Test
    public void testCryptoMarkerType() throws Exception {
	CardRecognition recognition = new CardRecognition(null, null);
	CardInfoType cardInfo = recognition.getCardInfo("http://ws.gematik.de/egk/1.0.0");

	CardApplicationType app = cardInfo.getApplicationCapabilities().getCardApplication().get(1);
	DifferentialIdentityType diffId = app.getDIDInfo().get(0).getDifferentialIdentity();
	CryptoMarkerType cryptoMarker = new CryptoMarkerType(diffId.getDIDMarker().getCryptoMarker());
	assertTrue(cryptoMarker.getAlgorithmInfo().getSupportedOperations().size()>0);
    }

}
