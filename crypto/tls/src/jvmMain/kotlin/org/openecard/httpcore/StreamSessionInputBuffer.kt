/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
package org.openecard.httpcore

import org.apache.http.impl.io.HttpTransportMetricsImpl
import org.apache.http.impl.io.SessionInputBufferImpl
import java.io.InputStream

/**
 * Stream based input buffer for use in Apache httpcore.
 *
 * @author Tobias Wich
 */
class StreamSessionInputBuffer(
	`in`: InputStream?,
	bufsize: Int,
) : SessionInputBufferImpl(HttpTransportMetricsImpl(), bufsize) {
	/**
	 * Creates a StreamSessionInputBuffer instance based on a given InputStream.
	 *
	 * @param in The destination input stream.
	 * @param bufsize The size of the internal buffer.
	 */
	init {
		// use a buffer stream, so the mark/reset operation is supported
		bind(`in`)
	}
}
