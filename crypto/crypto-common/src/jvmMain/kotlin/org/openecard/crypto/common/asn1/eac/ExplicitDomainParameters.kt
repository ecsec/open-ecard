/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.crypto.common.asn1.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.asn1.ASN1Integer
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.bouncycastle.asn1.x9.X9ECParameters
import org.openecard.bouncycastle.jce.spec.ECParameterSpec

private val LOG = KotlinLogging.logger { }

/**
 * See BSI-TR-03110, version 2.10, part 3, section A.2.1.2.
 *
 * @author Moritz Horsch
 */
class ExplicitDomainParameters(
	ai: AlgorithmIdentifier,
) : DomainParameters(loadParameters(ai)) {
	companion object {
		/**
		 * dhpublicnumber OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) ansi-x942(10046) number-type(2) 1}
		 */
		const val DH_PUBLIC_NUMBER: String = "1.2.840.10046.2.1"

		/**
		 * ecPublicKey OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) ansi-x962(10045) keyType(2) 1}
		 */
		const val EC_PUBLIC_KEY: String = "1.2.840.10045.2.1"

		/**
		 * Creates new ExplicitDomainParameters.
		 *
		 * @param ai AlgorithmIdentifier
		 */
		private fun loadParameters(ai: AlgorithmIdentifier): ECParameterSpec {
			val oid = ai.objectIdentifier
			return if (oid == DH_PUBLIC_NUMBER) {
				loadDHParameter(ai.parameters as ASN1Sequence)
			} else if (oid == EC_PUBLIC_KEY) {
				loadECDHParameter((ai.parameters as ASN1Sequence))
			} else {
				throw IllegalArgumentException("Cannot parse explicit domain parameters")
			}
		}

		private fun loadECDHParameter(seq: ASN1Sequence): ECParameterSpec {
//        ASN1Integer version = (ASN1Integer) seq.getObjectAt(0);
//        ASN1Sequence modulus = (ASN1Sequence) seq.getObjectAt(1);
//        ASN1Sequence coefficient = (ASN1Sequence) seq.getObjectAt(2);
//        ASN1OctetString basepoint = (ASN1OctetString) seq.getObjectAt(3);
			val order = seq.getObjectAt(4) as ASN1Integer
			val cofactor = seq.getObjectAt(5) as ASN1Integer

			return try {
//            BigInteger p = new BigInteger(modulus.getObjectAt(1).toASN1Primitive().getEncoded());
//            BigInteger a = new BigInteger(coefficient.getObjectAt(0).toASN1Primitive().getEncoded());
//            BigInteger b = new BigInteger(coefficient.getObjectAt(1).toASN1Primitive().getEncoded());
				val r = order.value
				val f = cofactor.value

				val ecParameters = X9ECParameters.getInstance(seq)
				val curve = ecParameters.curve

				ECParameterSpec(curve, ecParameters.g, r, f)
			} catch (e: Exception) {
				LOG.error(e) { "Failed to load proprietary domain parameters" }
				throw IllegalArgumentException("Cannot parse explicit domain parameters")
			}
		}

		private fun loadDHParameter(seq: ASN1Sequence): ECParameterSpec =
			throw UnsupportedOperationException("Not implemented yet")
	}
}
