/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

/**
 * Stream based extension to the Apache http-core library and a TLS based HTTP client.
 * <p>Instead of being socket based, the {@link org.apache.http.HttpClientConnection} implementation in this
 * package can operate directly on Java's standard {@link java.io.InputStream} and {@link java.io.OutputStream}
 * class.</p>
 *
 * @see <a href="https://hc.apache.org/httpcomponents-core-ga/tutorial/html/fundamentals.html">http-core Tutorial</a>
 */
package org.openecard.httpcore;
