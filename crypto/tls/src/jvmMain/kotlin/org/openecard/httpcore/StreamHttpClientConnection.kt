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
import org.apache.http.*
import org.apache.http.config.MessageConstraints
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.entity.ContentLengthStrategy
import org.apache.http.impl.HttpConnectionMetricsImpl
import org.apache.http.impl.entity.LaxContentLengthStrategy
import org.apache.http.impl.entity.StrictContentLengthStrategy
import org.apache.http.impl.io.*
import org.apache.http.io.HttpMessageParser
import org.apache.http.io.HttpMessageWriter
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HTTP
import org.openecard.common.AppVersion.name
import org.openecard.common.AppVersion.version
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private val LOG = KotlinLogging.logger {  }

/**
 * Stream based HTTP client. <br></br>
 * In contrast to the default client ([org.apache.http.impl.DefaultBHttpClientConnection], this
 * implementation uses an already existing connection instead of creating a new one.
 *
 * @author Tobias Wich
 */
open class StreamHttpClientConnection(private val `in`: InputStream, private val out: OutputStream) : HttpClientConnection {

    private val sin: StreamSessionInputBuffer = StreamSessionInputBuffer(`in`, BUFSIZE)
	private val sout: StreamSessionOutputBuffer = StreamSessionOutputBuffer(out, BUFSIZE)
	private val metrics: HttpConnectionMetricsImpl = HttpConnectionMetricsImpl(sin.metrics, sout.metrics)
	private val responseParser: HttpMessageParser<HttpResponse> = DefaultHttpResponseParserFactory.INSTANCE.create(sin, MessageConstraints.DEFAULT)
	private val requestWriter: HttpMessageWriter<HttpRequest> = DefaultHttpRequestWriterFactory.INSTANCE.create(sout)
	private val incomingContentStrategy: ContentLengthStrategy = LaxContentLengthStrategy.INSTANCE
	private val outgoingContentStrategy: ContentLengthStrategy = StrictContentLengthStrategy.INSTANCE

	private var open = true


	override fun isOpen(): Boolean {
        return open
    }

    @Throws(IOException::class)
    override fun shutdown() {
        open = false
        try {
            `in`.close()
        } catch (ex: IOException) {
			LOG.warn { "Error forcibly closing input stream." }
        }
        try {
            out.close()
        } catch (ex: IOException) {
			LOG.warn { "Error forcibly closing output stream." }
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (open) {
            open = false
            // send pending bytes
            flush()
            // close streams gracefully
            try {
                `in`.close()
            } catch (ex: IOException) {
                // ignore
            }
            try {
                out.close()
            } catch (ex: IOException) {
                // ignore
            }
        }
    }

    @Throws(IOException::class)
    private fun assertOpen() {
        if (!isOpen()) {
            throw IOException("HTTP connection is closed.")
        }
    }

    override fun setSocketTimeout(timeout: Int) {
        // ignore
		LOG.info { "Not supported in this type of connection." }
    }

    override fun getSocketTimeout(): Int {
        // pretend to know the timeout value, set to infinite
        return 0
    }

    @Throws(IOException::class)
    override fun isResponseAvailable(timeout: Int): Boolean {
        assertOpen()
        return sin.isDataAvailable(timeout)
    }

    @Throws(HttpException::class, IOException::class)
    override fun sendRequestHeader(request: HttpRequest) {
        assertOpen()
        request.setHeader(BasicHeader("User-Agent", "$name/$version"))
        this.requestWriter.write(request)
        incrementRequestCount()
    }

    @Throws(HttpException::class, IOException::class)
    override fun sendRequestEntity(request: HttpEntityEnclosingRequest) {
        assertOpen()
        val entity = request.entity
        if (entity == null) {
            return
        }
        prepareOutput(request).use { outstream ->
            entity.writeTo(outstream)
        }
    }

    @Throws(HttpException::class, IOException::class)
    override fun receiveResponseHeader(): HttpResponse {
        assertOpen()
        val response = this.responseParser.parse()
        if (response.statusLine.statusCode >= HttpStatus.SC_OK) {
            incrementResponseCount()
        }
        return response
    }

    @Throws(HttpException::class, IOException::class)
    override fun receiveResponseEntity(response: HttpResponse) {
        assertOpen()
        val entity = prepareInput(response)
		response.entity = entity
    }

    @Throws(IOException::class)
    override fun flush() {
        sout.flush()
    }

    override fun isStale(): Boolean {
        // TODO: determine whether the connection is still established by probing the input stream
        return isOpen
    }

    override fun getMetrics(): HttpConnectionMetrics {
        return metrics
    }

    protected fun incrementRequestCount() {
        metrics.incrementRequestCount()
    }

    protected fun incrementResponseCount() {
        metrics.incrementResponseCount()
    }

    protected fun createOutputStream(len: Long): OutputStream {
        if (len == ContentLengthStrategy.CHUNKED.toLong()) {
            return ChunkedOutputStream(2048, sout)
        } else if (len == ContentLengthStrategy.IDENTITY.toLong()) {
            return IdentityOutputStream(sout)
        } else {
            return ContentLengthOutputStream(sout, len)
        }
    }

    @Throws(HttpException::class)
    protected fun prepareOutput(message: HttpMessage): OutputStream {
        val len = this.outgoingContentStrategy.determineLength(message)
        return createOutputStream(len)
    }

    protected fun createInputStream(len: Long): InputStream {
		return if (len == ContentLengthStrategy.CHUNKED.toLong()) {
			ChunkedInputStream(sin)
		} else if (len == ContentLengthStrategy.IDENTITY.toLong()) {
			IdentityInputStream(sin)
		} else {
			ContentLengthInputStream(sin, len)
		}
    }

    @Throws(HttpException::class)
    protected fun prepareInput(message: HttpMessage): HttpEntity {
        val entity = BasicHttpEntity()

        val len = this.incomingContentStrategy.determineLength(message)
        val instream = createInputStream(len)
        if (len == ContentLengthStrategy.CHUNKED.toLong()) {
			entity.isChunked = true
			entity.contentLength = -1
			entity.content = instream
        } else if (len == ContentLengthStrategy.IDENTITY.toLong()) {
			entity.isChunked = false
			entity.contentLength = -1
			entity.content = instream
        } else {
			entity.isChunked = false
			entity.contentLength = len
			entity.content = instream
        }

        val contentTypeHeader = message.getFirstHeader(HTTP.CONTENT_TYPE)
        if (contentTypeHeader != null) {
            entity.setContentType(contentTypeHeader)
        }
        val contentEncodingHeader = message.getFirstHeader(HTTP.CONTENT_ENCODING)
        if (contentEncodingHeader != null) {
            entity.setContentEncoding(contentEncodingHeader)
        }
        return entity
    }

}

private const val BUFSIZE = 4 * 1024
