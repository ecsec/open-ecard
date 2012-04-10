/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openecard.client.recognition.CardRecognition;


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
