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
package org.openecard.ifd.scio

import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.GetStatus
import iso.std.iso_iec._24727.tech.schema.Wait
import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Tobias Wich
 */
class WaitTest {
	private val ifd: IFD = IFD()

	@Test(enabled = false)
	fun testBlockingWait() {
		val ctxHandle = ifd.establishContext(EstablishContext()).getContextHandle()
		val gs = GetStatus()
		gs.setContextHandle(ctxHandle)
		val statusResp = ifd.getStatus(gs)
		// wait until first terminal is added without smartcard
		val waitReq = Wait()
		waitReq.getIFDStatus().addAll(statusResp.getIFDStatus())
		waitReq.setContextHandle(ctxHandle)
		//        waitReq.setTimeOut(new BigInteger("1000"));
		val wr = ifd.wait(waitReq)

		Assert.assertTrue(
			wr.getIFDEvent()[0].getSlotStatus()[0].isCardAvailable == true,
			"Wait test failed",
		)

		// add second terminal without smartcard
		val waitReq2 = Wait()
		waitReq2.getIFDStatus().addAll(wr.getIFDEvent())
		waitReq2.setContextHandle(ctxHandle)
		val wr2 = ifd.wait(waitReq2)

		Assert.assertTrue(
			wr2.getIFDEvent()[1].getSlotStatus()[0].isCardAvailable == false,
			"Wait test failed",
		)
		// insert a terminal
		val waitReq3 = Wait()
		waitReq3.getIFDStatus().addAll(wr2.getIFDEvent())
		waitReq3.setContextHandle(ctxHandle)
		val wr3 = ifd.wait(waitReq3)

		Assert.assertTrue(
			wr3.getIFDEvent().size == 1,
			"Wait test failed",
		)
	}
}
