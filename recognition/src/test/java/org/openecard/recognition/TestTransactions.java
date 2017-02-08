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

package org.openecard.recognition;

import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import java.math.BigInteger;
import org.openecard.common.ClientEnv;
import org.openecard.common.interfaces.Environment;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.ws.IFD;
import org.testng.annotations.Test;


/**
 *
 * @author Dirk Petrautzki
 */
public class TestTransactions {

    /**
     * Manual test to ensure the card recognition with transactions is working as expected. This test starts two
     * Threads. The first one blocks the card for 15 seconds. The second one attempts to recognize the card. This will
     * obviously fail within the 15 seconds and a MessageDialog will be displayed. After the 15 seconds the recognition
     * will succeed.
     *
     * @throws Exception
     */
    @Test(enabled = false) // a reader with an inserted card is needed
    public void testExecute() throws Exception {
	// set up ifd and establish context
	Environment env = new ClientEnv();
	final IFD ifd = new org.openecard.ifd.scio.IFD();
	env.setIFD(ifd);
	EstablishContext eCtx = new EstablishContext();
	EstablishContextResponse eCtxR = ifd.establishContext(eCtx);
	final byte[] ctx = eCtxR.getContextHandle();

	// Set up GUI and card recognition
	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	final CardRecognitionImpl recog = new CardRecognitionImpl(env);
	recog.setGUI(gui);

	// get the first reader
	ListIFDs listIFDs = new ListIFDs();
	listIFDs.setContextHandle(ctx);
	ListIFDsResponse listIFDsResponse = ifd.listIFDs(listIFDs);
	final String ifdName = listIFDsResponse.getIFDName().get(0);

	Thread t1 = new Thread(new BlockingRunnable(ctx, ifdName, ifd));
	t1.start();

	Thread t2 = new Thread(new RecognizeRunnable(ctx, ifdName, recog));
	t2.start();

	t2.join();
    }

    /**
     * This runnable simply tries to recognize the card in the given ifd.
     *
     * @author Dirk Petrautzki
     */
    private final class RecognizeRunnable implements Runnable {

	private final byte[] ctx;
	private final String ifdName;
	private final CardRecognitionImpl recognition;

	private RecognizeRunnable(byte[] ctx, String ifdName, CardRecognitionImpl recognition) {
	    this.ctx = ctx;
	    this.ifdName = ifdName;
	    this.recognition = recognition;
	}

	@Override
	public void run() {
	    try {
		System.out.println("Thread 2 tries to recognize card.");
		RecognitionInfo info = recognition.recognizeCard(ctx, ifdName, BigInteger.ZERO);
		System.out.println("Thread 2 recognized: " + info.getCardType());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * This runnable blocks access to the card for 15 seconds.
     *
     * @author Dirk Petrautzki
     */
    private final class BlockingRunnable implements Runnable {
	private final byte[] ctx;
	private final String ifdName;
	private final IFD ifd;

	private BlockingRunnable(byte[] ctx, String ifdName, IFD ifd) {
	    this.ctx = ctx;
	    this.ifdName = ifdName;
	    this.ifd = ifd;
	}

	@Override
	public void run() {
	    try {
		Connect c = new Connect();
		c.setContextHandle(ctx);
		c.setIFDName(ifdName);
		c.setSlot(BigInteger.ZERO);
		ConnectResponse cr = ifd.connect(c);

		BeginTransaction bt = new BeginTransaction();
		bt.setSlotHandle(cr.getSlotHandle());
		ifd.beginTransaction(bt);

		System.out.println("Thread 1 is blocking the card for " + 15 + " seconds.");
		Thread.sleep(15 * 1000);

		EndTransaction et = new EndTransaction();
		et.setSlotHandle(cr.getSlotHandle());
		ifd.endTransaction(et);

		Disconnect d = new Disconnect();
		d.setSlotHandle(cr.getSlotHandle());
		ifd.disconnect(d);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

}
