package org.openecard.addons.tr03124.transport

import org.openecard.utils.common.cast
import java.io.InputStream
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

internal class AttachedSslSocketFactory(
	private val delegate: SSLSocketFactory,
	var allowResumption: Boolean = true,
) : SSLSocketFactory() {
	private fun Socket.restrictSocket(): Socket {
		this.cast<SSLSocket>()?.let { sock ->
			// prevent new sessions if needed
			sock.enableSessionCreation = allowResumption
		}
		return this
	}

	override fun getDefaultCipherSuites(): Array<out String>? = delegate.defaultCipherSuites

	override fun getSupportedCipherSuites(): Array<out String?>? = delegate.supportedCipherSuites

	override fun createSocket(
		s: Socket?,
		host: String?,
		port: Int,
		autoClose: Boolean,
	): Socket = delegate.createSocket(s, host, port, autoClose).restrictSocket()

	override fun createSocket(
		s: Socket?,
		consumed: InputStream?,
		autoClose: Boolean,
	): Socket = delegate.createSocket(s, consumed, autoClose).restrictSocket()

	override fun createSocket(): Socket = delegate.createSocket().restrictSocket()

	override fun createSocket(
		address: InetAddress?,
		port: Int,
		localAddress: InetAddress?,
		localPort: Int,
	): Socket = delegate.createSocket(address, port, localAddress, localPort).restrictSocket()

	override fun createSocket(
		host: InetAddress?,
		port: Int,
	): Socket = delegate.createSocket(host, port).restrictSocket()

	override fun createSocket(
		host: String?,
		port: Int,
	): Socket = delegate.createSocket(host, port).restrictSocket()

	override fun createSocket(
		host: String?,
		port: Int,
		localHost: InetAddress?,
		localPort: Int,
	): Socket = delegate.createSocket(host, port, localHost, localPort).restrictSocket()
}
