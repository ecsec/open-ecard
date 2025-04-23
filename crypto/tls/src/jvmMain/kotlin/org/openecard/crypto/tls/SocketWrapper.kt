/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
package org.openecard.crypto.tls

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.nio.channels.SocketChannel

/**
 * Wrapper class to simulate socket for TLS in- and output streams.
 * BouncyCastle is only able to emit InputStream and OutputStream classes which represent the tunneled channel. If it is
 * desirable to operate on a socket, say because some API needs a socket, then this class can be used to bring the
 * tunneled streams and the originating socket together.
 *
 * @author Tobias Wich
 */
class SocketWrapper(
	private val parent: Socket,
	private val `in`: InputStream,
	private val out: OutputStream,
) : Socket() {
	/**
	 * Creates an instance of a SocketWrapper binding the given streams to the socket.
	 * The socket must be opened and the streams must belong to the socket. The latter requirement is not checked.
	 *
	 * @param parent Connected socket which should be wrapped.
	 * @param `in` Input stream belonging to the socket.
	 * @param out Output stream belonging to the socket.
	 * @throws IOException Thrown in case the socket is not connected.
	 */
	init {
		// assert that the socket is really open. An unconnected socket can not have open streams
		if (!parent.isConnected) {
			throw IOException("Socket is not connected.")
		}
	}

	@Throws(IOException::class)
	override fun getInputStream(): InputStream {
		// call original to check if the socket is closed already
		parent.getInputStream()
		return `in`
	}

	@Throws(IOException::class)
	override fun getOutputStream(): OutputStream {
		// call original to check if the socket is closed already
		parent.getInputStream()
		return out
	}

	@Throws(IOException::class)
	override fun bind(bindpoint: SocketAddress?) {
		parent.bind(bindpoint)
	}

	@Synchronized
	@Throws(IOException::class)
	override fun close() {
		parent.close()
	}

	@Throws(IOException::class)
	override fun connect(endpoint: SocketAddress) {
		parent.connect(endpoint)
	}

	@Throws(IOException::class)
	override fun connect(
		endpoint: SocketAddress,
		timeout: Int,
	) {
		parent.connect(endpoint, timeout)
	}

	override fun getChannel(): SocketChannel? = parent.channel

	override fun getInetAddress(): InetAddress? = parent.getInetAddress()

	@Throws(SocketException::class)
	override fun getKeepAlive(): Boolean = parent.getKeepAlive()

	override fun getLocalAddress(): InetAddress = parent.getLocalAddress()

	override fun getLocalPort(): Int = parent.getLocalPort()

	override fun getLocalSocketAddress(): SocketAddress? = parent.getLocalSocketAddress()

	@Throws(SocketException::class)
	override fun getOOBInline(): Boolean = parent.getOOBInline()

	override fun getPort(): Int = parent.getPort()

	@Synchronized
	@Throws(SocketException::class)
	override fun getReceiveBufferSize(): Int = parent.getReceiveBufferSize()

	override fun getRemoteSocketAddress(): SocketAddress? = parent.getRemoteSocketAddress()

	@Throws(SocketException::class)
	override fun getReuseAddress(): Boolean = parent.getReuseAddress()

	@Synchronized
	@Throws(SocketException::class)
	override fun getSendBufferSize(): Int = parent.getSendBufferSize()

	@Throws(SocketException::class)
	override fun getSoLinger(): Int = parent.getSoLinger()

	@Synchronized
	@Throws(SocketException::class)
	override fun getSoTimeout(): Int = parent.getSoTimeout()

	@Throws(SocketException::class)
	override fun getTcpNoDelay(): Boolean = parent.getTcpNoDelay()

	@Throws(SocketException::class)
	override fun getTrafficClass(): Int = parent.trafficClass

	override fun isBound(): Boolean = parent.isBound

	override fun isClosed(): Boolean = parent.isClosed

	override fun isConnected(): Boolean = parent.isConnected

	override fun isInputShutdown(): Boolean = parent.isInputShutdown

	override fun isOutputShutdown(): Boolean = parent.isOutputShutdown

	@Throws(IOException::class)
	override fun sendUrgentData(data: Int) {
		parent.sendUrgentData(data)
	}

	@Throws(SocketException::class)
	override fun setKeepAlive(on: Boolean) {
		parent.setKeepAlive(on)
	}

	@Throws(SocketException::class)
	override fun setOOBInline(on: Boolean) {
		parent.setOOBInline(on)
	}

	override fun setPerformancePreferences(
		connectionTime: Int,
		latency: Int,
		bandwidth: Int,
	) {
		parent.setPerformancePreferences(connectionTime, latency, bandwidth)
	}

	@Synchronized
	@Throws(SocketException::class)
	override fun setReceiveBufferSize(size: Int) {
		parent.setReceiveBufferSize(size)
	}

	@Throws(SocketException::class)
	override fun setReuseAddress(on: Boolean) {
		parent.setReuseAddress(on)
	}

	@Synchronized
	@Throws(SocketException::class)
	override fun setSendBufferSize(size: Int) {
		parent.setSendBufferSize(size)
	}

	@Throws(SocketException::class)
	override fun setSoLinger(
		on: Boolean,
		linger: Int,
	) {
		parent.setSoLinger(on, linger)
	}

	@Synchronized
	@Throws(SocketException::class)
	override fun setSoTimeout(timeout: Int) {
		parent.setSoTimeout(timeout)
	}

	@Throws(SocketException::class)
	override fun setTcpNoDelay(on: Boolean) {
		parent.setTcpNoDelay(on)
	}

	@Throws(SocketException::class)
	override fun setTrafficClass(tc: Int) {
		parent.setTrafficClass(tc)
	}

	@Throws(IOException::class)
	override fun shutdownInput() {
		parent.shutdownInput()
	}

	@Throws(IOException::class)
	override fun shutdownOutput() {
		parent.shutdownOutput()
	}

	override fun toString(): String = parent.toString()
}
