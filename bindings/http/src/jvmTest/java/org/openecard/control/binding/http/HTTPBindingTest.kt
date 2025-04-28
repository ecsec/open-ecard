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
 */
package org.openecard.control.binding.http

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.openecard.ws.schema.Status
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val logger = KotlinLogging.logger{}
/**
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class HTTPBindingTest {
    /**
     * Tests the HttpStatusChangeHandler by sending:
     * 1. a get status to get the session identifier
     * 2. a get status with session identifier to set up event queue
     * 3. after 30 sec. a waitForChange
     * 4. after 45 sec. a waitForChange to see if the event queue still exists
     * 5. after 70 sec. a waitForChange to see if the event queue has correctly been removed due to timeout
     * 6. a waitForChange as POST request
     */
    @Test(enabled = false)
    fun testWaitForChange() {
        try {
            // Request a "get status" with GET and without optional session parameter
            var u = URL("http", "127.0.0.1", 24727, "/getStatus")
            var response = httpRequest(u, false)

            Assert.assertNotNull(response)

            logger.debug { response }
            val status = m!!.unmarshal(
                m!!.str2doc(
                    response!!
                )
            ) as Status
            val session = status.connectionHandle[0].channelHandle.sessionIdentifier

            // Request a "get status" with GET and with optional session parameter
            u = URL("http", "127.0.0.1", 24727, "/getStatus?session=$session")
            response = httpRequest(u, false)

            Assert.assertNotNull(response)

            logger.debug { response }

            Thread.sleep((30 * 1000).toLong())
            // Request a "waitForChange" with GET
            u = URL("http", "127.0.0.1", 24727, "/waitForChange?session=$session")
            response = httpRequest(u, false)

            Assert.assertNotNull(response)

            logger.debug { response }

            Thread.sleep((45 * 1000).toLong())
            // Request a "waitForChange" with GET
            u = URL("http", "127.0.0.1", 24727, "/waitForChange?session=$session")
            response = httpRequest(u, false)

            Assert.assertNotNull(response)

            logger.debug { response }

            Thread.sleep((70 * 1000).toLong())
            // Request a "waitForChange" with GET
            u = URL("http", "127.0.0.1", 24727, "/waitForChange?session=$session")
            response = httpRequest(u, false)
            // we expect response code 400, therefore response must be null
            Assert.assertNull(response)

            // Request a "waitForChange" with POST
            // response = httpRequest(u, true);

            // we expect response code 405, therefore response must be null
            // Assert.assertNull(response);
        } catch (e: Exception) {
            logger.debug(e.message, e)
            Assert.fail()
        }
    }

    /**
     * Tests the HttpStatusHandler by sending:
     * 1. a GET request without optional session parameter
     * 2. a GET request with optional session parameter
     * 3. a POST request
     * 4. a GET request with optional and malformed session parameter
     */
    @Test(enabled = false)
    fun testGetStatus() {
        try {
            // Request a "get status" with GET and without optional session parameter
            var u = URL("http", "127.0.0.1", 24727, "/getStatus")
            var response = httpRequest(u, false)

            Assert.assertNotNull(response)

            logger.debug { response }
            val status = m!!.unmarshal(
                m!!.str2doc(
                    response!!
                )
            ) as Status
            val session = status.connectionHandle[0].channelHandle.sessionIdentifier

            // Request a "get status" with GET and with optional session parameter
            u = URL("http", "127.0.0.1", 24727, "/getStatus?session=$session")
            response = httpRequest(u, false)

            Assert.assertNotNull(response)

            logger.debug { response }

            // Request a "get status" with POST
            //response = httpRequest(u, true);
            // we expect response code 405, therefore response must be null
            //Assert.assertNull(response);

            // Request a "get status" with GET and with optional malformed session parameter
            u = URL("http", "127.0.0.1", 24727, "/getStatus?session=")
            response = httpRequest(u, false)

            // we expect response code 400, therefore response must be null
            Assert.assertNull(response)
        } catch (e: Exception) {
			logger.debug(e) { "${e.message}" }
            Assert.fail()
        }
    }

    @Test(enabled = false)
    fun testeIDClient() {
        try {
            // Request a "eID-Client"
            val u = URL(
                "http://localhost:24727/eID-Client?tcTokenURL=https%3A%2F%2Feservice.openecard.org%2FtcToken%3Fcard-type%3Dhttp%253A%252F%252Fbsi.bund.de%252Fcif%252Fnpa.xml%26with-html%3D"
            )
            val response = httpRequest(u, false)

            Assert.assertNotNull(response)
        } catch (e: Exception) {
			logger.debug(e) { "${e.message}" }
			Assert.fail()
        }
    }

    companion object {
        private var m: WSMarshaller? = null

        /**
         * Start up the TestClient.
         *
         * @throws Exception
         */
        @BeforeClass
        @Throws(Exception::class)
        fun setUpClass() {
            try {
                val tc = TestClient()
                m = createInstance()
                m!!.removeAllTypeClasses()
                m!!.addXmlTypeClass(Status::class.java)

                // Wait some seconds until the SAL comes up
                Thread.sleep(2500)
            } catch (e: Exception) {
				logger.debug(e) { "${e.message}" }

				Assert.fail()
            }
        }

        /**
         * Performs a HTTP Request (GET or POST) to the specified URL and returns the response as String.
         *
         * @param url URL to connect to
         * @param doPOST true for POST, false for GET
         * @return response as string
         */
        private fun httpRequest(url: URL, doPOST: Boolean): String? {
            var c: HttpURLConnection? = null
            try {
                c = url.openConnection() as HttpURLConnection
                if (doPOST) {
                    c.doOutput = true
                    c.outputStream
                }
                val `in` = BufferedReader(InputStreamReader(c.inputStream))
                var inputLine: String?
                val content = StringBuilder(4096)

                while ((`in`.readLine().also { inputLine = it }) != null) {
                    content.append(inputLine)
                }
                `in`.close()

                return content.toString()
            } catch (e: IOException) {
                if (c!!.errorStream != null) {
                    try {
                        readErrorStream(c.errorStream)
                    } catch (ioe: IOException) {
						logger.debug(e) { "${e.message}" }

					}
                }
				logger.debug(e) { "${e.message}" }

				return null
            }
        }

        /**
         * Reads the HTML Error Response from the Server.
         */
        @Throws(IOException::class)
        private fun readErrorStream(errorStream: InputStream) {
            val bufferedReader = BufferedReader(InputStreamReader(errorStream))
            val stringBuilder = StringBuilder(4096)

            var line: String?
            while ((bufferedReader.readLine().also { line = it }) != null) {
                stringBuilder.append(line)
            }

            logger.error{"HTML Error response from server:\n$stringBuilder"}
        }
    }
}
