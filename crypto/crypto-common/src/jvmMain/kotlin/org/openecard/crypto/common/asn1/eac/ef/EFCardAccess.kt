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
package org.openecard.crypto.common.asn1.eac.ef

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.crypto.common.asn1.eac.*
import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier

private val LOG = KotlinLogging.logger { }

/**
 * Implements a EF.CardAccess file.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.2.1.
 *
 * @author Moritz Horsch
 */
class EFCardAccess(
	private val sis: SecurityInfos,
	/**
	 * Gets the PACESecurityInfos.
	 *
	 * @return PACESecurityInfos
	 */
	val pACESecurityInfos: PACESecurityInfos,
	/**
	 * Gets the TASecurityInfos.
	 *
	 * @return TASecurityInfos
	 */
	val tASecurityInfos: TASecurityInfos,
	/**
	 * Gets the CASecurityInfos.
	 *
	 * @return CASecurityInfos
	 */
	val cASecurityInfos: CASecurityInfos,
	/**
	 * Gets the CardInfoLocator.
	 *
	 * @return CardInfoLocator
	 */
	val cardInfoLocator: CardInfoLocator?,
	/**
	 * Gets the PrivilegedTerminalInfo.
	 *
	 * @return PrivilegedTerminalInfo
	 */
	val privilegedTerminalInfo: PrivilegedTerminalInfo?,
) {


	companion object {

		@JvmStatic
		fun getInstance(sis: SecurityInfos): EFCardAccess {
			return decodeSecurityInfos(sis)
		}

		@JvmStatic
		fun getInstance(sis: ByteArray): EFCardAccess {
			return decodeSecurityInfos(SecurityInfos.Companion.getInstance(sis))
		}


		/**
		 * Decode the SecurityInfos.
		 */
		private fun decodeSecurityInfos(sis: SecurityInfos): EFCardAccess {
			val securityInfos = sis.securityInfos
			val length = securityInfos.size()

			val psi = PACESecurityInfos()
			val tsi = TASecurityInfos()
			val csi = CASecurityInfos()
			var cil: CardInfoLocator? = null
			var pti: PrivilegedTerminalInfo? = null

			for (i in 0..<length) {
				val securityInfo = securityInfos.getObjectAt(i) as ASN1Sequence
				val oid = securityInfo.getObjectAt(0).toString()

				// PACEInfo (REQUIRED)
				if (PACEInfo.Companion.isPACEObjectIdentifer(oid)) {
					LOG.debug { "Found PACEInfo object identifier" }
					val pi = PACEInfo(securityInfo)
					psi.addPACEInfo(pi)
				} // PACEDoaminParameterInfo (CONDITIONAL)
				else if (PACEDomainParameterInfo.Companion.isPACEObjectIdentifer(oid)) {
					LOG.debug { "Found PACEDomainParameterInfo object identifier" }
					val pdp = PACEDomainParameterInfo(securityInfo)
					psi.addPACEDomainParameterInfo(pdp)
				} // ChipAuthenticationInfo (CONDITIONAL)
				else if (CAInfo.Companion.isObjectIdentifier(oid)) {
					LOG.debug { "Found ChipAuthenticationInfo object identifier" }
					val ci = CAInfo(securityInfo)
					csi.addCAInfo(ci)
				} // ChipAuthenticationDomainParameterInfo (CONDITIONAL)
				else if (CADomainParameterInfo.Companion.isObjectIdentifier(oid)) {
					LOG.debug { "Found ChipAuthenticationDomainParameterInfo object identifier" }
					val cdp = CADomainParameterInfo(securityInfo)
					csi.addCADomainParameterInfo(cdp)
				} // TerminalAuthenticationInfo (CONDITIONAL)
				else if (EACObjectIdentifier.id_TA == oid) {
					LOG.debug { "Found TerminalAuthenticationInfo object identifier" }
					val ta = TAInfo(securityInfo)
					tsi.addTAInfo(ta)
				} // CardInfoLocator (RECOMMENDED)
				else if (EACObjectIdentifier.id_CI == oid) {
					LOG.debug { "Found CardInfoLocator object identifier" }
					cil = CardInfoLocator.Companion.getInstance(securityInfo)
				} // PrivilegedTerminalInfo (CONDITIONAL)
				else if (EACObjectIdentifier.id_PT == oid) {
					LOG.debug { "Found PrivilegedTerminalInfo object identifier" }
					pti = PrivilegedTerminalInfo.Companion.getInstance(securityInfo)
				} else {
					LOG.debug { "Found unknown object identifier: $oid" }
				}
			}

			return EFCardAccess(sis, psi, tsi, csi, cil, pti)
		}
	}

}
