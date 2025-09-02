/*
 * Copyright (C) 2024 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bchateau.pskfactories

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.nio.channels.SocketChannel
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLSocket

/**
 * This class delegates most functionality to a wrapped regular Socket.
 */
internal abstract class WrappedSSLSocket(
	private val socket: Socket,
) : SSLSocket() {
	private val listeners: MutableList<HandshakeCompletedListener> = ArrayList(2)

	@Throws(IOException::class)
	override fun connect(
		endpoint: SocketAddress?,
		timeout: Int,
	) {
		socket.connect(endpoint, timeout)
	}

	override fun getInetAddress(): InetAddress? = socket.getInetAddress()

	override fun getLocalAddress(): InetAddress? = socket.getLocalAddress()

	override fun getPort(): Int = socket.getPort()

	override fun getLocalPort(): Int = socket.getLocalPort()

	@Throws(IOException::class)
	abstract override fun getInputStream(): InputStream?

	@Throws(IOException::class)
	abstract override fun getOutputStream(): OutputStream?

	@Throws(SocketException::class)
	override fun setTcpNoDelay(on: Boolean) {
		socket.setTcpNoDelay(on)
	}

	@Throws(SocketException::class)
	override fun getTcpNoDelay(): Boolean = socket.getTcpNoDelay()

	@Throws(SocketException::class)
	override fun setSoLinger(
		on: Boolean,
		`val`: Int,
	) {
		socket.setSoLinger(on, `val`)
	}

	@Throws(SocketException::class)
	override fun getSoLinger(): Int = socket.getSoLinger()

	@Throws(SocketException::class)
	override fun setSoTimeout(timeout: Int) {
		socket.setSoTimeout(timeout)
	}

	@Throws(SocketException::class)
	override fun getSoTimeout(): Int = socket.getSoTimeout()

	@Throws(IOException::class)
	override fun close() {
		socket.close()
	}

	override fun isClosed(): Boolean = socket.isClosed

	override fun isConnected(): Boolean = socket.isConnected

	override fun isBound(): Boolean = socket.isBound

	@Throws(IOException::class)
	override fun shutdownInput() {
		socket.shutdownInput()
	}

	@Throws(IOException::class)
	override fun shutdownOutput() {
		socket.shutdownOutput()
	}

	override fun isInputShutdown(): Boolean = socket.isInputShutdown

	override fun isOutputShutdown(): Boolean = socket.isOutputShutdown

	override fun getRemoteSocketAddress(): SocketAddress? = socket.getRemoteSocketAddress()

	override fun getLocalSocketAddress(): SocketAddress? = socket.getLocalSocketAddress()

	@Throws(SocketException::class)
	override fun setOOBInline(on: Boolean): Unit = throw UnsupportedOperationException()

	@Throws(SocketException::class)
	override fun getOOBInline(): Boolean = false

	override fun getChannel(): SocketChannel? = null

	@Throws(SocketException::class)
	override fun setSendBufferSize(size: Int) {
		socket.setSendBufferSize(size)
	}

	@Throws(SocketException::class)
	override fun getSendBufferSize(): Int = socket.getSendBufferSize()

	@Throws(SocketException::class)
	override fun setReceiveBufferSize(size: Int) {
		socket.setReceiveBufferSize(size)
	}

	@Throws(SocketException::class)
	override fun getReceiveBufferSize(): Int = socket.getReceiveBufferSize()

	@Throws(SocketException::class)
	override fun setKeepAlive(on: Boolean) {
		socket.setKeepAlive(on)
	}

	@Throws(SocketException::class)
	override fun getKeepAlive(): Boolean = socket.getKeepAlive()

	@Throws(SocketException::class)
	override fun setTrafficClass(tc: Int) {
		socket.setTrafficClass(tc)
	}

	@Throws(SocketException::class)
	override fun getTrafficClass(): Int = socket.trafficClass

	@Throws(SocketException::class)
	override fun setReuseAddress(on: Boolean) {
		socket.setReuseAddress(on)
	}

	@Throws(SocketException::class)
	override fun getReuseAddress(): Boolean = socket.getReuseAddress()

	override fun setPerformancePreferences(
		connectionTime: Int,
		latency: Int,
		bandwidth: Int,
	) {
		socket.setPerformancePreferences(connectionTime, latency, bandwidth)
	}

	@Throws(IOException::class)
	override fun bind(bindpoint: SocketAddress?) {
		socket.bind(bindpoint)
	}

	@Throws(IOException::class)
	override fun sendUrgentData(data: Int): Unit = throw UnsupportedOperationException()

	override fun addHandshakeCompletedListener(listener: HandshakeCompletedListener) {
		listeners.add(listener)
	}

	override fun removeHandshakeCompletedListener(listener: HandshakeCompletedListener) {
		require(listeners.remove(listener)) { "'listener' is not registered" }
	}

	override fun toString(): String = "WrappedSSL$socket"
}
