/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.recognition;

import iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACD;
import iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACDResponse;
import java.io.IOException;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.recognition.staticrepo.LocalCifRepo;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.jaxb.JAXBMarshaller;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestLocalCif {

    @Test
    public void testeGKCif() throws WSMarshallerException, IOException, SAXException {
	LocalCifRepo repo = new LocalCifRepo(new JAXBMarshaller());

	GetCardInfoOrACD req = new GetCardInfoOrACD();
	req.setAction(ECardConstants.CIF.GET_SPECIFIED);
	req.getCardTypeIdentifier().add("http://ws.gematik.de/egk/1.0.0");
	GetCardInfoOrACDResponse res = repo.getCardInfoOrACD(req);
	try {
	    WSHelper.checkResult(res);
	} catch (WSException ex) {
	    Assert.fail("Local repo returned with error\n" + ex.getMessage());
	}
	Assert.assertEquals(1, res.getCardInfoOrCapabilityInfo().size());
    }

}
