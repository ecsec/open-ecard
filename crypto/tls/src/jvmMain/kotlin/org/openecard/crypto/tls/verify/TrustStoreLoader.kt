/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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
package org.openecard.crypto.tls.verify

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.TrustAnchor
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
open class TrustStoreLoader {
	protected open val storeFileName: String = "oec_cacerts.zip"

	protected fun load() {
		try {
			val tmAlg = TrustManagerFactory.getDefaultAlgorithm()
			val tmFactory = TrustManagerFactory.getInstance(tmAlg)

			// try to load internal keystore, if none is present or deactivated, fall back to system trust store
			// the fallback is implicit
			var ks = loadInternalStore()
			tmFactory.init(ks)

			// create trustmanager and extract trust anchors
			val anchors = mutableSetOf<TrustAnchor>()
			val tms = tmFactory.trustManagers
			// pick first X509 tm
			for (tm in tms) {
				if (tm is X509TrustManager) {
					val x509Tm = tm
					for (cert in x509Tm.acceptedIssuers) {
						val ta = TrustAnchor(cert, null)
						anchors.add(ta)
					}
				}
			}

			if (anchors.isEmpty()) {
				// no hard fail nevertheless, validation will just not work
				LOG.error { "No trusted CAs found." }
			}

			// make sure that we set a keystore object for this file
			if (ks == null) {
				ks = KeyStore.getInstance(KeyStore.getDefaultType())
				ks.load(null)

				// add anchors to the file
				for (a in anchors) {
					val cert = a.trustedCert
					if (ks.getCertificateAlias(cert) == null) {
						ks.setCertificateEntry(cert.getSubjectX500Principal().name, cert)
					}
				}
			}

			synchronized(TrustStoreLoader::class.java) {
				TRUST_STORES.put(storeFileName, ks)
				TRUST_ANCHORS.put(
					storeFileName,
					anchors.toSet(),
				)
			}
		} catch (ex: IOException) {
			val msg = "Failed to create or initialize TrustManagerFactory."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: CertificateException) {
			val msg = "Failed to create or initialize TrustManagerFactory."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: NoSuchAlgorithmException) {
			val msg = "Failed to create or initialize TrustManagerFactory."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: KeyStoreException) {
			val msg = "Failed to create or initialize TrustManagerFactory."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		}
	}

	protected open fun useInternalStore(): Boolean = false

	protected fun loadInternalStore(): KeyStore? {
		if (useInternalStore()) {
			// The internal keystore is a zip file containing DER encoded certificates.
			// This is due to the following problems:
			// - Java keystore not supported on Android
			// - PKCS12 can not be used as truststore in Desktop Java
			// - Bundled BC JCE has no Oracle signature and can thus not be used that way
			try {
				// create keystore object to be able to save the certificates later
				val ks = KeyStore.getInstance(KeyStore.getDefaultType())
				ks.load(null)
				val cf = CertificateFactory.getInstance("X.509")

				// get stream to zip file containing the certificates
				val `is` =
					resolveResourceAsStream(
						TrustStoreLoader::class.java,
						storeFileName,
					)
				val zis = if (`is` != null) ZipInputStream(`is`) else null
				if (zis != null) {
					var entry: ZipEntry?
					while ((zis.getNextEntry().also { entry = it }) != null) {
						// only read entry if it is not a directory
						if (!entry!!.isDirectory) {
							val baos = ByteArrayOutputStream()
							// read entry (certificates can not be longer than int)
							val data = ByteArray(4096)
							var numRead: Int
							do {
								numRead = zis.read(data, 0, 4096)
								if (numRead != -1) {
									baos.write(data, 0, numRead)
								}
							} while (numRead != -1)
							// convert bytes to x509 cert
							val dataStream = ByteArrayInputStream(baos.toByteArray())
							val cert = cf.generateCertificate(dataStream)
							ks.setCertificateEntry(entry.getName(), cert)
						}
					}

					return ks
				} else {
					error { "Internal keystore not found, falling back to next available trust store." }
				}
			} catch (ex: IOException) {
				LOG.error(ex) { "Error reading embedded keystore." }
			} catch (ex: KeyStoreException) {
				LOG.error(ex) { "Failed to obtain keystore or save entry in it." }
			} catch (ex: NoSuchAlgorithmException) {
				LOG.error(ex) { "Failed to obtain keystore or save entry in it." }
			} catch (ex: CertificateException) {
				LOG.error(ex) { "Failed to obtain keystore or save entry in it." }
			}
		}
		// error or different keystore requested
		return null
	}

	val trustAnchors: Set<TrustAnchor>
		get() {
			val result = TRUST_ANCHORS[storeFileName]
			if (result != null) {
				return result
			} else {
				// load truststore and try again
				load()
				return trustAnchors
			}
		}

	val trustStore: KeyStore?
		get() {
			val result: KeyStore? = TRUST_STORES[storeFileName]
			if (result != null) {
				return result
			} else {
				// load truststore and try again
				load()
				return trustStore
			}
		}

	companion object {
		private val TRUST_STORES = mutableMapOf<String, KeyStore>()
		private val TRUST_ANCHORS = mutableMapOf<String, Set<TrustAnchor>>()

		fun reset() {
			synchronized(TrustStoreLoader::class.java) {
				TRUST_STORES.clear()
				TRUST_ANCHORS.clear()
			}
		}
	}
}
