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
import org.openecard.crypto.common.asn1.eac.CADomainParameterInfo
import org.openecard.crypto.common.asn1.eac.CAInfo
import org.openecard.crypto.common.asn1.eac.CASecurityInfos
import org.openecard.crypto.common.asn1.eac.CardInfoLocator
import org.openecard.crypto.common.asn1.eac.PACEDomainParameterInfo
import org.openecard.crypto.common.asn1.eac.PACEInfo
import org.openecard.crypto.common.asn1.eac.PACESecurityInfos
import org.openecard.crypto.common.asn1.eac.PrivilegedTerminalInfo
import org.openecard.crypto.common.asn1.eac.SecurityInfos
import org.openecard.crypto.common.asn1.eac.TAInfo
import org.openecard.crypto.common.asn1.eac.TASecurityInfos
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
		fun getInstance(sis: SecurityInfos): EFCardAccess = decodeSecurityInfos(sis)

		@JvmStatic
		fun getInstance(sis: ByteArray): EFCardAccess = decodeSecurityInfos(SecurityInfos.Companion.getInstance(sis))

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

				if (PACEInfo.Companion.isPACEObjectIdentifer(oid)) {
					// PACEInfo (REQUIRED)
					LOG.debug { "Found PACEInfo object identifier" }
					val pi = PACEInfo(securityInfo)
					psi.addPACEInfo(pi)
				} else if (PACEDomainParameterInfo.Companion.isPACEObjectIdentifer(oid)) {
					// PACEDoaminParameterInfo (CONDITIONAL)
					LOG.debug { "Found PACEDomainParameterInfo object identifier" }
					val pdp = PACEDomainParameterInfo(securityInfo)
					psi.addPACEDomainParameterInfo(pdp)
				} else if (CAInfo.Companion.isObjectIdentifier(oid)) {
					// ChipAuthenticationInfo (CONDITIONAL)
					LOG.debug { "Found ChipAuthenticationInfo object identifier" }
					val ci = CAInfo(securityInfo)
					csi.addCAInfo(ci)
				} else if (CADomainParameterInfo.Companion.isObjectIdentifier(oid)) {
					// ChipAuthenticationDomainParameterInfo (CONDITIONAL)
					LOG.debug { "Found ChipAuthenticationDomainParameterInfo object identifier" }
					val cdp = CADomainParameterInfo(securityInfo)
					csi.addCADomainParameterInfo(cdp)
				} else if (EACObjectIdentifier.ID_TA == oid) {
					// TerminalAuthenticationInfo (CONDITIONAL)
					LOG.debug { "Found TerminalAuthenticationInfo object identifier" }
					val ta = TAInfo(securityInfo)
					tsi.addTAInfo(ta)
				} else if (EACObjectIdentifier.ID_CI == oid) {
					// CardInfoLocator (RECOMMENDED)
					LOG.debug { "Found CardInfoLocator object identifier" }
					cil = CardInfoLocator.Companion.getInstance(securityInfo)
				} else if (EACObjectIdentifier.ID_PT == oid) {
					// PrivilegedTerminalInfo (CONDITIONAL)
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
