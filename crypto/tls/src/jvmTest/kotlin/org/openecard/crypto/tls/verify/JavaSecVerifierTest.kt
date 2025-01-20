/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
package org.openecard.crypto.tls.verify

import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.Test
import java.io.IOException
import java.net.Socket

/**
 *
 * @author Tobias Wich
 */
@Test(groups = ["it"])
class JavaSecVerifierTest {
    @Test
    @Throws(IOException::class)
    fun testVerificationNoError() {
        val hostName = "github.com"
        val c: DefaultTlsClientImpl?
		val handler = try {
            // open connection
            val socket = Socket(hostName, 443)
            Assert.assertTrue(socket.isConnected)
            Assert.assertTrue(socket.isBound)
            Assert.assertFalse(socket.isClosed)
            // connect client
            c = DefaultTlsClientImpl(hostName)
            TlsClientProtocol(socket.getInputStream(), socket.getOutputStream())
        } catch (ex: Exception) {
            throw SkipException("Unable to create TLS client.")
        }
        // do TLS handshake
        handler.connect(c)
        handler.close()
    }

    @Test(expectedExceptions = [IOException::class])
    @Throws(IOException::class)
    fun testVerificationError() {
        val hostName = "www.google.com"
        val actualHostName = "github.com"
        val c: DefaultTlsClientImpl?
		val handler = try {
            // open connection
            val socket = Socket(actualHostName, 443)
            Assert.assertTrue(socket.isConnected)
            Assert.assertTrue(socket.isBound)
            Assert.assertFalse(socket.isClosed)
            // connect client
            c = DefaultTlsClientImpl(hostName)
            TlsClientProtocol(socket.getInputStream(), socket.getOutputStream())
        } catch (ex: Exception) {
            throw SkipException("Unable to create TLS client.")
        }
        // do TLS handshake
        handler.connect(c)
        handler.close()
    }
}
