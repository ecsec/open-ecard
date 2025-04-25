/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
import org.apache.http.*
import org.apache.http.HttpException
import org.apache.http.impl.DefaultBHttpServerConnection
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.impl.DefaultHttpResponseFactory
import org.apache.http.protocol.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

private val logger = KotlinLogging.logger{}

/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class HttpService(
    port: Int, handler: HttpRequestHandler, reqInterceptors: List<HttpRequestInterceptor>,
    respInterceptors: List<HttpResponseInterceptor>
) : Runnable {
    private val thread: Thread
    private val service: org.apache.http.protocol.HttpService
    protected val server: ServerSocket = ServerSocket(port, BACKLOG, InetAddress.getByName("127.0.0.1"))

    /**
     * Creates a new HTTPService.
     *
     * @param port Port
     * @param handler Handler
     * @param reqInterceptors
     * @param respInterceptors
     * @throws Exception
     */
    init {
        logger.debug{
            "Starting HTTP Binding on port ${this.port}"
        }
        thread = Thread(this, "Open-eCard Localhost-Binding-" + this.port)

        // Reuse strategy
        val connectionReuseStrategy: ConnectionReuseStrategy = DefaultConnectionReuseStrategy()
        // Response factory
        val responseFactory: HttpResponseFactory = DefaultHttpResponseFactory()
        // Interceptors
        val httpProcessor: HttpProcessor = ImmutableHttpProcessor(reqInterceptors, respInterceptors)

        // Set up handler registry
        val handlerRegistry = UriHttpRequestHandlerMapper()
        logger.debug{"Add handler [${handler.javaClass.canonicalName}] for ID[*]"}
        handlerRegistry.register("*", handler)

        // create service instance
        service = HttpService(httpProcessor, connectionReuseStrategy, responseFactory, handlerRegistry)
    }

    /**
     * Starts the server.
     */
    fun start() {
        thread.start()
    }

    /**
     * Interrupts the server.
     */
    fun interrupt() {
        try {
            thread.interrupt()
            server.close()
        } catch (ignore: Exception) {
        }
    }

    @Throws(IOException::class, HttpServiceError::class)
    protected fun accept(): Socket {
        return server.accept()
    }

    override fun run() {
        while (!Thread.interrupted()) {
            try {
                val connection: DefaultBHttpServerConnection
                val dec = Charset.forName("UTF-8").newDecoder()
                val enc = Charset.forName("UTF-8").newEncoder()
                connection = DefaultBHttpServerConnection(8192, dec, enc, null)
                connection.bind(accept())

                object : Thread() {
                    override fun run() {
                        try {
                            while (connection.isOpen) {
                                service.handleRequest(connection, BasicHttpContext())
                            }
                        } catch (ex: ConnectionClosedException) {
                            // connection closed by client, this is the expected outcome
                        } catch (ex: HttpException) {
                            logger.error(ex){"Error processing HTTP request or response."}
                        } catch (ex: IOException) {
                            logger.error(ex){"IO Error while processing HTTP request or response."}
                        } finally {
                            try {
                                connection.shutdown()
                            } catch (ignore: IOException) {
                            }
                        }
                    }
                }.start()
            } catch (ex: IOException) {
                // if interrupted the error is intentionally (SocketClosedException)
                if (!Thread.interrupted()) {
                    logger.error(ex) { "${ex.message}" }
                } else {
                    // set interrupt status again after reading it
                    thread.interrupt()
                }
            } catch (ex: HttpServiceError) {
                if (!Thread.interrupted()) {
                    logger.error(ex) { "${ex.message}" }
                } else {
                    thread.interrupt()
                }
            }
        }
    }

    val port: Int
        /**
         * Returns the port number on which the HTTP binding is listening.
         *
         * @return Port
         */
        get() = server.localPort

    companion object {
        private const val BACKLOG = 10
    }
}
