
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
package org.openecard.transport.httpcore

import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.message.BasicHttpRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.apache.http.util.EntityUtils
import org.openecard.bouncycastle.tls.DefaultTlsClient
import org.openecard.bouncycastle.tls.ServerName
import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.common.util.FileUtils.toByteArray
import org.openecard.httpcore.HttpRequestHelper.setDefaultHeader
import org.openecard.httpcore.StreamHttpClientConnection
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.IOException
import java.net.Socket
import java.security.SecureRandom
import java.util.*

/**
 *
 * @author Tobias Wich
 */
@Test(groups = ["it"])
class StreamHttpClientConnectionTest {
    private var rand: SecureRandom? = null

    @BeforeClass
    fun setup() {
        rand = SecureRandom()
    }

    @Test
    @Throws(IOException::class, HttpException::class)
    fun testRequestHttpGoogle() {
        val hostName = "www.google.com"
        // open connection
        val socket = Socket(hostName, 80)
        Assert.assertTrue(socket.isConnected)
        val conn = StreamHttpClientConnection(socket.getInputStream(), socket.getOutputStream())
        Assert.assertTrue(conn.isOpen)

        consumeEntity(conn, hostName, 2)
    }

    @Test
    @Throws(IOException::class, HttpException::class)
    fun testRequestHttpsGoogle() {
        val hostName = "www.google.com"
        // open connection
        val socket = Socket(hostName, 443)
        Assert.assertTrue(socket.isConnected)
        val tlsClient: DefaultTlsClient = object : DefaultTlsClientImpl(BcTlsCrypto(rand)) {
            override fun getSNIServerNames(): Vector<*> {
                return Vector(listOf(ServerName(0.toShort(), hostName)))
            }
        }
        val handler = TlsClientProtocol(socket.getInputStream(), socket.getOutputStream())
        handler.connect(tlsClient)
        val conn = StreamHttpClientConnection(handler.inputStream, handler.outputStream)
        Assert.assertTrue(conn.isOpen)

        consumeEntity(conn, hostName, 2)
    }

    @Throws(IOException::class, HttpException::class)
    private fun consumeEntity(conn: StreamHttpClientConnection, hostName: String, numIt: Int) {
        val ctx: HttpContext = BasicHttpContext()
        val httpexecutor = HttpRequestExecutor()
        var response: HttpResponse? = null
        val reuse = DefaultConnectionReuseStrategy()

        var i = 0
        while (i == 0 || (i < numIt && reuse.keepAlive(response, ctx))) {
            i++
            // send request and receive response
            val request: HttpRequest = BasicHttpRequest("GET", "/")
            setDefaultHeader(request, hostName)
            response = httpexecutor.execute(request, conn, ctx)
            conn.receiveResponseEntity(response)
            val entity = response.entity
            Assert.assertNotNull(entity)

            // consume entity
            val content = toByteArray(entity!!.content)

            // read header and check if content size is correct
            val lengthHeader = response.getFirstHeader("Content-Length")
            if (lengthHeader != null) {
                val length = lengthHeader.value.toLong()
                Assert.assertNotNull(lengthHeader)
                Assert.assertEquals(entity.contentLength, length)
                Assert.assertEquals(content.size.toLong(), length)
            }

            // consume everything from the entity and close stream
            EntityUtils.consume(entity)
        }
    }
}
