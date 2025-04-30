/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
package org.openecard.control.binding.http.interceptor

import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
import org.apache.http.protocol.HttpContext

/**
 * HttpResponseInterceptor implementation which adds a `Cache-Control` header to the response.
 * <br></br>
 * <br></br>
 * The header sets the directive `no-store` to advise the user agent to do not cache the response.
 *
 * @author Hans-Martin Haase
 */
class CacheControlHeaderResponseInterceptor : HttpResponseInterceptor {
	override fun process(
		hr: HttpResponse,
		hc: HttpContext,
	) {
		hr.addHeader("Cache-Control", "no-store")
	}
}
