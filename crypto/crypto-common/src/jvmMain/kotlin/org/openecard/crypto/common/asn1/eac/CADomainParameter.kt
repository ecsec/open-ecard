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

import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier
import java.security.spec.AlgorithmParameterSpec

/**
 *
 * @author Moritz Horsch
 */
class CADomainParameter(
	private val csi: CASecurityInfos,
	var domainParameter: AlgorithmParameterSpec,
) {
	private val ci: CAInfo = requireNotNull(csi.cAInfo)

	/**
	 * Create new CADomainParameter.
	 *
	 * @param csi CASecurityInfos
	 */
	constructor(csi: CASecurityInfos) : this(csi, loadParameters(csi))

	var parameter: AlgorithmParameterSpec
		/**
		 * Returns the domain parameter.
		 *
		 * @return Domain parameter
		 */
		get() = domainParameter

		/**
		 * Sets the domain parameter.
		 *
		 * @param domainParameter Domain parameter
		 */
		set(domainParameter) {
			this.domainParameter = domainParameter
		}

	val isDH: Boolean
		/**
		 * Checks if the protocol identifier indicates Diffie-Hellman.
		 *
		 * @return True if Diffie-Hellman is used, otherwise false
		 */
		get() = ci.isDH

	val isECDH: Boolean
		/**
		 * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
		 *
		 * @return True if elliptic curve Diffie-Hellman is used, otherwise false
		 */
		get() = ci.isECDH

	companion object {
		private fun loadParameters(csi: CASecurityInfos): AlgorithmParameterSpec {
			val cdp = requireNotNull(csi.cADomainParameterInfo)
			val ai = cdp.domainParameter

			return if (ai.objectIdentifier == EACObjectIdentifier.standardized_Domain_Parameters) {
				val index = ai.parameters.toString().toInt()
				StandardizedDomainParameters(index).parameter
			} else {
				ExplicitDomainParameters(ai).parameter
			}
		}
	}
}
