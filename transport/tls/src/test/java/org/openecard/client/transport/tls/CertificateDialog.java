/*
 * Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
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

package org.openecard.client.transport.tls;

import java.util.List;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.client.gui.definition.*;


/**
 *
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 */
public class CertificateDialog {

    UserConsentDescription uc = new UserConsentDescription("Wählen Sie ein Zertifikat");

    public CertificateDialog(List<Certificate> allcerts) {

	Step s1 = new Step("Certificate Selection");
	uc.getSteps().add(s1);
	Text msg = new Text();
	msg.setText("Bitte Wählen Sie ein Zertifikat aus.");
	s1.getInputInfoUnits().add(msg);
	int index = 0;

	for (Certificate certificateChain : allcerts) {
	    Text i1 = new Text();
	    StringBuilder sb = new StringBuilder();

	    sb.append("------------------------------------------------------------------------------");
	    sb.append("\n");
	    sb.append("Issuer: ");
	    sb.append(certificateChain.getCerts()[0].getIssuer().toString());
	    sb.append("\n");
	    sb.append("Subject: ");
	    sb.append(certificateChain.getCerts()[0].getSubject().toString());
	    sb.append("\n");
	    sb.append("Startdate: ");
	    sb.append(certificateChain.getCerts()[0].getStartDate().toString());
	    sb.append("\n");
	    sb.append("Enddate: ");
	    sb.append(certificateChain.getCerts()[0].getEndDate().toString());
	    sb.append("\n");
	    sb.append("SN: ");
	    sb.append(certificateChain.getCerts()[0].getSerialNumber().toString());
	    sb.append("\n");
	    sb.append("------------------------------------------------------------------------------");
	    sb.append("\n");

	    i1.setText(sb.toString());
	    s1.getInputInfoUnits().add(i1);
	    Checkbox i2 = new Checkbox();
	    BoxItem b = new BoxItem();

	    b.setName(String.valueOf(index));
	    index++;
	    i2.getBoxItems().add(b);
	    i2.setGroupText("Zertifikat wählen");
	    s1.getInputInfoUnits().add(i2);
	}
    }

    public UserConsentDescription getUserConsent() {
	return uc;
    }

}
