package org.openecard.client.recognition;

import org.openecard.client.recognition.CardRecognition;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import java.math.BigInteger;
import org.junit.Test;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecuteRecognition {

    @Test
    public void testExecute() throws Exception {
	IFD ifd = new org.openecard.client.ifd.IFD();
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
