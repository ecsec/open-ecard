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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.tlv.TLVException
import org.openecard.common.util.StringUtils
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import kotlin.test.Test


/**
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class CHATTest {
    private lateinit var chat: CHAT
    private lateinit var chatBytes: ByteArray


    @BeforeMethod
    fun setUp() {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffb7")
        chat = CHAT(chatBytes)
    }

    @Test
    fun testParse() {
        val c = CHAT(StringUtils.toByteArray("7F4C12060904007F0007030102025305000100FA04"))
        Assert.assertEquals(CHAT.Role.AUTHENTICATION_TERMINAL, c.role)
        val data = CHAT.DataGroup.entries
        val specialFunctions = CHAT.SpecialFunction.entries

        // check writeAccess
        for (i in 16..20) {
            Assert.assertFalse(c.getWriteAccess()[data[i]]!!)
        }

        // check readAccess
        for (i in 0..20) {
            if (i == 1 || (i > 2 && i < 8) || i == 16) {
                Assert.assertTrue(c.getReadAccess()[data[i]]!!)
            } else {
                Assert.assertFalse(c.getReadAccess()[data[i]]!!)
            }
        }

        // check special functions
        for (i in 0..7) {
            if (i == CHAT.SpecialFunction.RESTRICTED_IDENTIFICATION.ordinal) {
                Assert.assertTrue(c.getSpecialFunctions()[specialFunctions[i]]!!)
            } else {
                Assert.assertFalse(c.getSpecialFunctions()[specialFunctions[i]]!!)
            }
        }
    }

    @Test
    @Throws(TLVException::class)
    fun testEncoding() {
        Assert.assertEquals(chatBytes, chat.toByteArray())
    }

    @Test(enabled = false)
    @Throws(TLVException::class)
    fun testtoString() {
        val readAccess = chat.getReadAccess()

        for (entry in readAccess.entries) {
            println(entry.key.toString() + " " + entry.value)
        }
    }

    @Test
    @Throws(TLVException::class)
    fun testWriteAccess() {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305000301ffb7")
        chat.setWriteAccess(CHAT.DataGroup.DG17, false)
        chat.setWriteAccess("DG18", false)
        Assert.assertEquals(chat.toByteArray(), chatBytes)
    }

    @Test
    @Throws(TLVException::class)
    fun testReadAccess() {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301f0b7")
        chat.setReadAccess(CHAT.DataGroup.DG01, false)
        chat.setReadAccess(CHAT.DataGroup.DG02, false)
        chat.setReadAccess(CHAT.DataGroup.DG03.name, false)
        chat.setReadAccess("DG04", false)
        Assert.assertEquals(chat.toByteArray(), chatBytes)
    }

    @Test
    @Throws(TLVException::class)
    fun testSpecialFunctions() {
        chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffAF")
        chat.setSpecialFunctions(CHAT.SpecialFunction.PRIVILEGED_TERMINAL, true)
        chat.setSpecialFunction("CAN_ALLOWED", false)
        Assert.assertEquals(chat.toByteArray(), chatBytes)
    }
}
