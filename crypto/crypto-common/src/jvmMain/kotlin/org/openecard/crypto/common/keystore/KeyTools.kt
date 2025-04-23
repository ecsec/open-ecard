/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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
package org.openecard.crypto.common.keystore

import org.openecard.bouncycastle.asn1.x500.X500Name
import org.openecard.bouncycastle.asn1.x500.style.IETFUtils
import org.openecard.bouncycastle.asn1.x509.Certificate
import org.openecard.bouncycastle.crypto.params.*
import org.openecard.bouncycastle.crypto.util.PublicKeyFactory
import org.openecard.bouncycastle.tls.crypto.TlsCertificate
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.Key
import java.security.cert.CertPath
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.interfaces.DSAKey
import java.security.interfaces.ECKey
import java.security.interfaces.RSAKey
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.interfaces.DHKey

/**
 * Class with helper functions regarding cryptographic keys.
 *
 * @author Tobias Wich
 */
object KeyTools {
	fun getKeySize(key: Key): Int =
		when (key) {
			is RSAKey -> {
				val rsaPk = key as RSAKey
				val mod = rsaPk.modulus
				mod.bitLength()
			}

			is DSAKey -> {
				val dsaPk = key as DSAKey
				val p = dsaPk.params.p
				p.bitLength()
			}

			is ECKey -> {
				val ecPk = key as ECKey
				val order = ecPk.params.order
				order.bitLength()
			}

			is DHKey -> {
				val dhKey = key as DHKey
				val p = dhKey.params.p
				p.bitLength()
			}

			is SecretKey -> {
				val sKey = key
				if ("RAW" == sKey.format) {
					val data = sKey.encoded
					if (data != null) {
						data.size * 8
					} else {
						// key has no encoding
						-1
					}
				} else {
					// unknown key format
					-1
				}
			}

			else -> {
				// unkown or inaccessible key (e.g. on secure storage device)
				-1
			}
		}

	fun getKeySize(key: AsymmetricKeyParameter): Int =
		when (key) {
			is RSAKeyParameters -> {
				val rsaKey = key
				val mod = rsaKey.modulus
				mod.bitLength()
			}

			is DSAKeyParameters -> {
				val dsaKey = key
				val p = dsaKey.parameters.p
				p.bitLength()
			}

			is ECKeyParameters -> {
				val ecKey = key
				val order = ecKey.parameters.curve.getOrder()
				order.bitLength()
			}

			is DHKeyParameters -> {
				val dhKey = key
				val p = dhKey.parameters.p
				p.bitLength()
			}

			is ElGamalKeyParameters -> {
				val egKey = key
				val p = egKey.parameters.p
				p.bitLength()
			}

			else -> {
				-1
			}
		}

	fun getReferenceKeySize(key: AsymmetricKeyParameter): Int {
		if (Calendar.getInstance().get(Calendar.YEAR) < 2025) {
			return if (key is RSAKeyParameters) {
				2048
			} else if (key is DSAKeyParameters) {
				2048
			} else if (key is DHKeyParameters) {
				2048
			} else if (key is ElGamalKeyParameters) {
				2048
			} else if (key is ECKeyParameters) {
				224
			} else {
				-1
			}
		} else {
			return if (key is RSAKeyParameters) {
				3072
			} else if (key is DSAKeyParameters) {
				3072
			} else if (key is DHKeyParameters) {
				3072
			} else if (key is ElGamalKeyParameters) {
				3072
			} else if (key is ECKeyParameters) {
				256
			} else {
				-1
			}
		}
	}

	/**
	 * Checks the given key if it satisfies the key length requirements defined in BSI TR-03116-4.
	 *
	 * @param x509 The certificate containing the key to test.
	 * @throws KeyLengthException Thrown in case the key is too short.
	 * @throws IOException Thrown in case the certificate could not be parsed.
	 * @throws UnsupportedOperationException Thrown in case no reference value could be obtained for the given keytype.
	 */
	@JvmStatic
	@Throws(KeyLengthException::class, IOException::class)
	fun assertKeyLength(x509: Certificate) {
		val pkInfo = x509.subjectPublicKeyInfo
		val key = PublicKeyFactory.createKey(pkInfo)
		assertKeyLength(key, x509.subject)
	}

	/**
	 * Checks the given key if it satisfies the key length requirements defined in BSI TR-03116-4.
	 *
	 * @param key The key to test.
	 * @throws KeyLengthException Thrown in case the key is too short.
	 * @throws UnsupportedOperationException Thrown in case no reference value could be obtained for the given keytype.
	 */
	@Throws(KeyLengthException::class)
	fun assertKeyLength(key: AsymmetricKeyParameter) {
		KeyTools.assertKeyLength(key!!, null)
	}

	@Throws(KeyLengthException::class)
	private fun assertKeyLength(
		key: AsymmetricKeyParameter,
		subj: X500Name?,
	) {
		val subStr = if (subj != null) IETFUtils.valueToString(subj) else "UNKNOWN"
		val keyClass = key.javaClass.getName()
		val reference = getReferenceKeySize(key)
		val numbits = getKeySize(key)
		if (reference == -1) {
			val msg = String.format("The key type %s is unsupported in certificate [%s].", keyClass, subStr)
			throw UnsupportedOperationException(msg)
		}

		if (numbits < reference) {
			val msg = "The key size does not meet the requirements ($numbits < $reference) in certificate [$subStr]."
			throw KeyLengthException(msg)
		}
	}

	/**
	 * Converts the given certificate chain to a JCA CertPath.
	 *
	 * @param chain BouncyCastle certificates instance.
	 * @return CertPath instance with the exact same certificate chain.
	 * @throws CertificateException Thrown in case the JCA has problems supporting X509 or one of the certificates.
	 * @throws IOException Thrown in case there is en encoding error.
	 */
	@Throws(CertificateException::class, IOException::class)
	fun convertCertificates(chain: org.openecard.bouncycastle.tls.Certificate): CertPath =
		convertCertificates(*chain.getCertificateList())

	/**
	 * Converts the given certificate chain to a JCA CertPath.
	 *
	 * @param chain BouncyCastle list of certificates.
	 * @return CertPath instance with the exact same certificate chain.
	 * @throws CertificateException Thrown in case the JCA has problems supporting X509 or one of the certificates.
	 * @throws IOException Thrown in case there is en encoding error.
	 */
	@Throws(CertificateException::class, IOException::class)
	fun convertCertificates(vararg chain: TlsCertificate): CertPath {
		val numCerts = chain.size
		val result = ArrayList<java.security.cert.Certificate?>(numCerts)
		val cf = CertificateFactory.getInstance("X.509")

		for (next in chain) {
			val nextData = next.encoded
			val nextDataStream = ByteArrayInputStream(nextData)
			val nextConverted = cf.generateCertificate(nextDataStream)
			result.add(nextConverted)
		}

		return cf.generateCertPath(result)
	}

	//
	//    /**
	//     * Converts the given certificate chain to a BouncyCastle Certificate chain.
	//     *
	//     * @param chain JCA list of certificates.
	//     * @return BC Certificate instance.
	//     * @throws CertificateException Thrown in case one of the given certificates could not be encoded.
	//     */
	//    public static Certificate convertCertificates(java.security.cert.Certificate... chain) throws CertificateException {
	// 	org.openecard.bouncycastle.asn1.x509.Certificate[] certs;
	// 	certs = new org.openecard.bouncycastle.asn1.x509.Certificate[chain.length];
	//
	// 	for (int i = 0; i < chain.length; i++) {
	// 	    java.security.cert.Certificate next = chain[i];
	// 	    byte[] encCert = next.getEncoded();
	// 	    certs[i] = org.openecard.bouncycastle.asn1.x509.Certificate.getInstance(encCert);
	// 	}
	//
	// 	Certificate cert = new Certificate(certs);
	// 	return cert;
	//    }
}
