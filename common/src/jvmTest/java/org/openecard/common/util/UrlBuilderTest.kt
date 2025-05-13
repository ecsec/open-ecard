/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.common.util

import org.testng.Assert
import org.testng.annotations.Test
import java.net.URI

/**
 *
 * @author Tobias Wich
 */
class UrlBuilderTest {
	@Test
	fun testAddUrlQuery() {
		val base = "http://foo:12"
		var result = UrlBuilder.fromUrl(base).queryParam("foo", "http://bar/?query1=foo&query2=bar%20ui#foo").build()
		val resultUrl = "http://foo:12/?foo=http%3A%2F%2Fbar%2F%3Fquery1%3Dfoo%26query2%3Dbar%20ui%23foo"
		Assert.assertEquals(result, URI(resultUrl))

		// test overwrite
		result =
			UrlBuilder
				.fromUrl(base)
				.queryParam("foo", "1")
				.queryParam("foo", "2")
				.build()
		Assert.assertEquals(result, URI("http://foo:12/?foo=2"))
		result =
			UrlBuilder
				.fromUrl(base)
				.queryParam("foo", "1")
				.queryParam("foo", "2", false)
				.build()
		Assert.assertEquals(result, URI("http://foo:12/?foo=1"))
	}

	@Test
	fun replaceScheme() {
		val base = "http://foo:12"
		val result = UrlBuilder.fromUrl(base).scheme("https").build()
		Assert.assertEquals(result, URI("https://foo:12/"))
	}

	@Test
	fun deletePort() {
		val base = "http://foo:12"
		val result = UrlBuilder.fromUrl(base).port(-1).build()
		Assert.assertEquals(result, URI("http://foo/"))
	}

	@Test
	fun addPathSegment() {
		var b = UrlBuilder.fromUrl("http://foo/")

		b = b.addPathSegment("/foo")
		Assert.assertEquals(b.build(), URI("http://foo/foo"))

		b = b.addPathSegment("bar/")
		Assert.assertEquals(b.build(), URI("http://foo/foo/bar/"))

		b = b.addPathSegment("/quack")
		Assert.assertEquals(b.build(), URI("http://foo/foo/bar/quack"))
	} // TODO: add more tests
}
