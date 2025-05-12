/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import org.testng.Assert
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 *
 * @author Tobias Wich
 */
class SubjectWhitelistTest {
	@Test
	@Throws(IOException::class, CertificateException::class)
	fun testCert() {
		SubjectWhitelistTest::class.java.getResourceAsStream("/cms_sign_cert.crt").use { certIn ->
			val fac = CertificateFactory.getInstance("X.509")
			val cert = fac.generateCertificate(certIn) as X509Certificate
			val principal = cert.subjectX500Principal
			Assert.assertTrue(AllowedSubjects.isInSubjects(principal))
		}
	}

	@Test
	@Throws(CertificateException::class)
	fun testGoodApiEndpoints() {
		val goodCertStr = (
			"-----BEGIN CERTIFICATE-----\n" +
				"MIIC+DCCAeCgAwIBAgIICE27HuZGvPowDQYJKoZIhvcNAQENBQAwVzELMAkGA1UE\n" +
				"BhMCREUxETAPBgNVBAcTCE1pY2hlbGF1MRMwEQYDVQQKEwplY3NlYyBHbWJIMSAw\n" +
				"HgYDVQQDExdDRyBUZXN0IEludGVybWVkaWF0ZSBDQTAeFw0yMDA5MTAwOTUyMDBa\n" +
				"Fw0yNjA4MTYwOTUyMDBaMFAxCzAJBgNVBAYTAkRFMREwDwYDVQQHEwhNaWNoZWxh\n" +
				"dTETMBEGA1UEChMKZWNzZWMgR21iSDEZMBcGA1UEAxMQY2cub3BlbmVjYXJkLm9y\n" +
				"ZzB2MBAGByqGSM49AgEGBSuBBAAiA2IABBL/RVoLtHmsiso06VUeeVnuzpKD+S39\n" +
				"LHBpNnu38QPbj98KgX1KtokysoMf+vQdbutCqZCGwW7tzEaCRlolZaixofJhEc1F\n" +
				"tmG1kEDVtmojTbN4cA5Je3w/ekb4duDyLaN9MHswDAYDVR0TAQH/BAIwADAdBgNV\n" +
				"HQ4EFgQUudcISQ5seE7NVoCH/GLJXIlm5C4wHwYDVR0jBBgwFoAUIA85kGbHVZJl\n" +
				"jWcsXEFouX85SeUwDgYDVR0PAQH/BAQDAgWgMBsGA1UdEQQUMBKCEGNnLm9wZW5l\n" +
				"Y2FyZC5vcmcwDQYJKoZIhvcNAQENBQADggEBAGCMS1T47g3Ls0/4Z09ekg+A6aMD\n" +
				"LLWP5qZsbe4E7ADiRtiwk2MTVXWjvXb2qY59AWqXv6C6wrcZJqe/uTYuqYQxaNMr\n" +
				"FHUNFufrH558i4T3x2b+K8icC24Czfx7lttgYZBvYoSbiMz7uq7JebSRYHUyC+BG\n" +
				"RldFHj7CTY7F5VABdtHG0XQanscS7ZWvgbI4p/3i++0gIWgqMiKBY+V9dKOtTBxM\n" +
				"YdRBgToFYq+wL3PyE4zx0hqaeC00IgCdExWWsiQ+buh+aGJt3grYsaZkX1GubS47\n" +
				"LkG1/mMHtBn8Xx7TQquNYr//8z5krhC+rXhcOG3Eg/WLx4ZiVCV509Q5iTc=\n" +
				"-----END CERTIFICATE-----\n"
		)
		Assert.assertTrue(matchApiEndpoint(goodCertStr))
	}

	@Test
	@Throws(CertificateException::class)
	fun testBadApiEndpoints() {
		val badCertStr = (
			"-----BEGIN CERTIFICATE-----\n" +
				"MIIC9DCCAdygAwIBAgIIYMw4is2lCZIwDQYJKoZIhvcNAQELBQAwVzELMAkGA1UE\n" +
				"BhMCREUxETAPBgNVBAcTCE1pY2hlbGF1MRMwEQYDVQQKEwplY3NlYyBHbWJIMSAw\n" +
				"HgYDVQQDExdDRyBUZXN0IEludGVybWVkaWF0ZSBDQTAeFw0yMDA5MTAwOTUyMDBa\n" +
				"Fw0yNjA4MTYwOTUyMDBaME4xCzAJBgNVBAYTAkRFMREwDwYDVQQHEwhNaWNoZWxh\n" +
				"dTETMBEGA1UEChMKZWNzZWMgR21iSDEXMBUGA1UEAxMOY2cuc3Bvb2ZlZC5vcmcw\n" +
				"djAQBgcqhkjOPQIBBgUrgQQAIgNiAAQS/0VaC7R5rIrKNOlVHnlZ7s6Sg/kt/Sxw\n" +
				"aTZ7t/ED24/fCoF9SraJMrKDH/r0HW7rQqmQhsFu7cxGgkZaJWWosaHyYRHNRbZh\n" +
				"tZBA1bZqI02zeHAOSXt8P3pG+Hbg8i2jezB5MAwGA1UdEwEB/wQCMAAwHQYDVR0O\n" +
				"BBYEFLnXCEkObHhOzVaAh/xiyVyJZuQuMB8GA1UdIwQYMBaAFCAPOZBmx1WSZY1n\n" +
				"LFxBaLl/OUnlMA4GA1UdDwEB/wQEAwIFoDAZBgNVHREEEjAQgg5jZy5zcG9vZmVk\n" +
				"Lm9yZzANBgkqhkiG9w0BAQsFAAOCAQEAt4YBNWJCyvdbCKGB8ekhMCXcuEQeDc2e\n" +
				"RUAZmnlHIcXWyX3Ea00VuX46t6rofEg1xz5WVTCVQQskqT6FELzFfBx55N5cyPqy\n" +
				"uF7p4zk96E5drxHOjuPBmlBuuTAyqR98lsGWdYREmF1GcDnlWoMnoDAirAfGMeo+\n" +
				"EeP063Ce4mwGR0amUa5Y0jjdQJVtA0A1iOCZgRBDhpXa2IGKTbU2fiS41ttietyg\n" +
				"BUjIBtwI9mLe682jW1UWUoeokhtjacsHoto5avGk6Rn7xrUKBuy6uKMZtNn8zhqQ\n" +
				"7182tLY6qcNA0AmBtELEjVZe0qsI1H2pPxrz+57vqoFLM0HYJSHtOg==\n" +
				"-----END CERTIFICATE-----\n"
		)
		Assert.assertFalse(matchApiEndpoint(badCertStr))
	}

	@Throws(CertificateException::class)
	private fun matchApiEndpoint(certStr: String): Boolean {
		val certStream = ByteArrayInputStream(certStr.toByteArray(StandardCharsets.UTF_8))
		val cf = CertificateFactory.getInstance("X.509")
		val cert = cf.generateCertificate(certStream) as X509Certificate
		val certSub = cert.subjectX500Principal
		return AllowedApiEndpoints.isInSubjects(certSub)
	}
}
