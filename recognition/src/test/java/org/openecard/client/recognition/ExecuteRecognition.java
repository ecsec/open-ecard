/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.recognition;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.ws.WSClassLoader;
import org.openecard.ws.GetRecognitionTree;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecuteRecognition {

    @Ignore
    @Test
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
            GetRecognitionTree client = (GetRecognitionTree) WSClassLoader.getClientService(RecognitionProperties.getServiceName(), RecognitionProperties.getServiceAddr());
	    CardRecognition recog = new CardRecognition(ifd, ctx, client);
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
