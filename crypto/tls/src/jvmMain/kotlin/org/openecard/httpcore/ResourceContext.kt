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
package org.openecard.httpcore

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.util.FileUtils
import org.openecard.common.util.Pair
import org.openecard.crypto.tls.ClientCertTlsClient
import java.io.IOException
import java.io.InputStream
import java.net.URL

private val LOG = KotlinLogging.logger {  }

/**
 * Implements a grabber to fetch TCTokens from a URL.
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
open class ResourceContext(
    val tlsClient: ClientCertTlsClient?,
	val tlsClientProto: TlsClientProtocol?,
    val certs: List<Pair<URL, TlsServerCertificate>>,
	val stream: InputStream?
) {
    @get:Throws(IOException::class)
    @get:Synchronized
    var data: String? = null
        get() {
            // load data from stream first
            if (field == null) {
                try {
                    field = stream?.let { FileUtils.toString(it) }
                } finally {
                    stream?.let {
                        try {
                            stream.close()
                        } catch (ex: IOException) {
							LOG.debug(ex) { "Failed to close stream." }
                        }
                    }
                }
            }
            return field
        }
        private set

    constructor(
        tlsClient: ClientCertTlsClient?,
		tlsClientProto: TlsClientProtocol?,
        certs: List<Pair<URL, TlsServerCertificate>>
    ) : this(tlsClient, tlsClientProto, certs, null)

    fun closeStream() {
        if (stream != null) {
            try {
                stream.close()
            } catch (ex: IOException) {
				LOG.debug(ex) { "Failed to close stream." }
            }
        }
        if (tlsClientProto != null) {
            try {
                tlsClientProto.close()
            } catch (ex: IOException) {
				LOG.debug(ex) { "Failed to close connection." }
            }
        }
    }

    fun hasData(): Boolean {
        return stream != null
    }

}
