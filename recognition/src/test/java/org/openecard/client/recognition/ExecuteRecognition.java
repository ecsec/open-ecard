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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import org.openecard.ws.IFD;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecuteRecognition {

    @Test(enabled=false)
    public void testExecute() throws Exception {
	IFD ifd = new org.openecard.client.ifd.scio.IFD();
	byte[] ctx;
	// establish context
	EstablishContext eCtx = new EstablishContext();
	EstablishContextResponse eCtxR = ifd.establishContext(eCtx);
	ctx = eCtxR.getContextHandle();
	// get status to see if we can execute the recognition
	GetStatus status = new GetStatus();
	status.setContextHandle(ctx);
	GetStatusResponse statusR = ifd.getStatus(status);

	if (statusR.getIFDStatus().size() > 0 && statusR.getIFDStatus().get(0).getSlotStatus().get(0).isCardAvailable()) {
	    CardRecognition recog = new CardRecognition(ifd, ctx);
	    IFDStatusType stat = statusR.getIFDStatus().get(0);
	    RecognitionInfo info = recog.recognizeCard(stat.getIFDName(), BigInteger.ZERO);
	    if (info == null) {
		System.out.println("Card not recognized.");
	    } else {
		System.out.println(info.getCardType());
	    }
	}
    }

}
