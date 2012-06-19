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
