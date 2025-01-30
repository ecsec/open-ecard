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

import iso.std.iso_iec._24727.tech.schema.Connect
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import iso.std.iso_iec._24727.tech.schema.ReleaseContext
import iso.std.iso_iec._24727.tech.schema.Transmit
import org.openecard.common.ClientEnv
import org.openecard.common.ECardConstants
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.testng.annotations.AfterTest
import org.testng.annotations.Test
import java.lang.Boolean
import java.math.BigInteger
import kotlin.ByteArray
import kotlin.String
import kotlin.byteArrayOf
import kotlin.test.assertEquals

/**
 *
 * @author Tobias Wich
 */
class TerminalTest {
    private var ifd: IFD? = null
    private var ctxHandle: ByteArray? = null
    private var ifdName: String? = null


    fun init() {
        val env: ClientEnv = ClientEnv()
		env.gui = SwingUserConsent(SwingDialogWrapper())

        ifd = IFD()
        ifd!!.setEnvironment(env)

        val eCtx = EstablishContext()
        ctxHandle = ifd!!.establishContext(eCtx).getContextHandle()

        val listIFDs: ListIFDs = ListIFDs()
        listIFDs.setContextHandle(ctxHandle)
        ifdName = ifd!!.listIFDs(listIFDs).getIFDName().get(0)
    }

    @AfterTest
    fun kill() {
        if (ifd != null) {
            val rCtx = ReleaseContext()
            rCtx.setContextHandle(ctxHandle)
            ifd!!.releaseContext(rCtx)
        }
        ifd = null
    }


    @Test(enabled = false)
    fun testTransmit() {
        init()

        val con: Connect = Connect()
        con.setContextHandle(ctxHandle)
        con.setIFDName(ifdName)
        con.setSlot(BigInteger.ZERO)
		con.isExclusive = Boolean.FALSE
        val slotHandle = ifd!!.connect(con).getSlotHandle()

        val t = Transmit()
        val apdu = InputAPDUInfoType()
        apdu.getAcceptableStatusCode().add(byteArrayOf(0x90.toByte(), 0x00.toByte()))
        apdu.setInputAPDU(byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x0C.toByte()))
        t.getInputAPDUInfo().add(apdu)
        t.setSlotHandle(slotHandle)

        val res = ifd!!.transmit(t)
        assertEquals(ECardConstants.Major.OK, res.getResult().getResultMajor())
    }


    @Test(enabled = false)
    fun testFeatures() {
        init()

        val con: Connect = Connect()
        con.setContextHandle(ctxHandle)
        con.setIFDName(ifdName)
        con.setSlot(BigInteger.ZERO)
		con.isExclusive = Boolean.FALSE
        val slotHandle = ifd!!.connect(con).getSlotHandle()

        val cap = GetIFDCapabilities()
        cap.setContextHandle(ctxHandle)
        cap.setIFDName(ifdName)
        val capR = ifd!!.getIFDCapabilities(cap)
    }
}
