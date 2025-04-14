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

package org.openecard.richclient

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

val logger = KotlinLogging.logger {}

/**
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class RichClientTest {

	private lateinit var tcTokenURL: URL
	private lateinit var statusURL: URL
	private lateinit var waitForChangeURL: URL

	/**
	 * Starts up the RichClient.
	 */
	@BeforeTest
	fun setUp() {
		tcTokenURL = URL(
			"http", "127.0.0.1", 24727,
			"/eID-Client?tcTokenURL=http%3A%2F%2Fopenecard-demo.vserver-001.urospace.de%2FtcToken%3Fcard-type%3Dhttp%3A%2F%2Fbsi.bund.de%2Fcif%2Fnpa.xml"
		)
		statusURL = URL("http", "127.0.0.1", 24727, "/getStatus")
		waitForChangeURL = URL("http", "127.0.0.1", 24727, "/waitForChange")
		val client = RichClient()
		client.setup()
		// Wait some seconds until the client comes up
		Thread.sleep(2500)
	}

	/**
	 * Test the Response of the RichClient to a TCTokenRequest.
	 */
	@Test(enabled = false)
	fun testTCToken() {
		try {
			val urlConnection = tcTokenURL.openConnection() as HttpURLConnection
			getResponse(urlConnection)
		} catch (e: Exception) {
			logger.error(e) { e.message }
			fail(e.message)
		}
	}

	/**
	 * Test the Response of the RichClient to a StatusRequest.
	 */
	@Test(enabled = false)
	fun testStatus() {
		try {
			val urlConnection = statusURL.openConnection() as HttpURLConnection
			getResponse(urlConnection)
		} catch (e: Exception) {
			logger.error(e) { e.message }
			fail(e.message)
		}
	}

	/**
	 * Test the Response of the RichClient to a WaitForChangeReuquest.
	 */
	@Test(enabled = false)
	fun testWaitForChange() {
		try {
			val urlConnection = waitForChangeURL.openConnection() as HttpURLConnection
			getResponse(urlConnection)
		} catch (e: Exception) {
			logger.error(e) { e.message }
			fail(e.message)
		}
	}


	/**
	 * Opens the URLConnection, gets the Response and checks the ResponseCode.
	 *
	 * @param urlConnection the connection to open
	 * @throws IOException if an I/O error occurs
	 */
	@Throws(IOException::class)
	private fun getResponse(urlConnection: HttpURLConnection) {
		try {
			val res = urlConnection.inputStream.readAllBytes().toString(Charsets.UTF_8)
			logger.debug { res }
			assertTrue { checkResponseCode(urlConnection.responseCode) }
		} finally {
			urlConnection.disconnect()
		}
	}

	/**
	 * Check for a successful status code (2xx).
	 *
	 * @param code status code to be checked
	 * @return true if successful, else false
	 */
	private fun checkResponseCode(code: Int): Boolean {
		return ((code >= 200) && (code < 300))
	}
}
