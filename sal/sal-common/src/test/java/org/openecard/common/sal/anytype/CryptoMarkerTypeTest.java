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

package org.openecard.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import org.openecard.common.ECardConstants;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.util.StringUtils;
import org.openecard.recognition.CardRecognition;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CryptoMarkerTypeTest {

    private static final String cardType = "http://ws.gematik.de/egk/1.0.0";
    private static final byte[] esignApplication = StringUtils.toByteArray("A000000167455349474E");
    private static final String didName = "PrK.CH.AUT_signPKCS1_V1_5";

    /**
     * Simple test for CryptoMarkerType. After getting the CryptoMarker for the PrK.CH.AUT_signPKCS1_V1_5 DID in the the
     * ESIGN application we check if the get-methods return the expected values.
     *
     * @throws Exception
     *             when something in this test went unexpectedly wrong
     */
    @Test
    public void testCryptoMarkerType() throws Exception {
	CardRecognition recognition = new CardRecognition(null, null);
	CardInfoType cardInfo = recognition.getCardInfo(cardType);
	CardInfoWrapper wrapper = new CardInfoWrapper(cardInfo);

	CardApplicationWrapper app = wrapper.getCardApplication(esignApplication);
	DifferentialIdentityType diffId = app.getDIDInfo(didName).getDIDInfo().getDifferentialIdentity();
	CryptoMarkerType cryptoMarker = new CryptoMarkerType(diffId.getDIDMarker().getCryptoMarker());
	assertTrue(cryptoMarker.getAlgorithmInfo().getSupportedOperations().size() > 0);
	assertEquals(cryptoMarker.getSignatureGenerationInfo(), new String[] { "MSE_KEY_DS", "PSO_CDS" });
	assertEquals(cryptoMarker.getCryptoKeyInfo().getKeyRef().getKeyRef(), new byte[] { 0x02 });
	assertEquals(cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm(),
		"urn:oid:1.2.840.113549.1.1");
	assertNull(cryptoMarker.getLegacyKeyName());
	assertNull(cryptoMarker.getHashGenerationInfo());
	assertEquals(cryptoMarker.getCertificateRef().getDataSetName(), "EF.C.CH.AUT");
	// assertEquals(cryptoMarker.getStateInfo(), "");
	assertEquals(cryptoMarker.getProtocol(), ECardConstants.Protocol.GENERIC_CRYPTO);
    }

}
