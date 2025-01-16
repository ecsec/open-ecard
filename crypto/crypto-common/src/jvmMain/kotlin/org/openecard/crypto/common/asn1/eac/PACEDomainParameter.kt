/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.security.spec.AlgorithmParameterSpec

/**
 * Wrapper for [PACEDomainParameterInfo] with some convenience functions.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class PACEDomainParameter {
    private var domainParameter: AlgorithmParameterSpec
    private val pip: PACESecurityInfoPair
    private val pi: PACEInfo

    /**
     * Create new PACEDomainParameter. Loads parameter as defined in the PACEInfo.
     *
     * @param pip PACESecurityInfoPair
     */
    constructor(pip: PACESecurityInfoPair) {
        this.pip = pip
        this.pi = pip.pACEInfo

        this.domainParameter = loadParameters(pip, pi)
    }

    /**
     * Create new PACEDomainParameter.
     *
     * @param pip PACESecurityInfoPair
     * @param domainParameter AlgorithmParameterSpec
     */
    constructor(pip: PACESecurityInfoPair, domainParameter: AlgorithmParameterSpec) {
        this.pip = pip
        this.pi = pip.pACEInfo
        this.domainParameter = domainParameter
    }

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
        get() = pi.isDH

    val isECDH: Boolean
        /**
         * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
         *
         * @return True if elliptic curve Diffie-Hellman is used, otherwise false
         */
        get() = pi.isECDH

	companion object {
		private fun loadParameters(pip: PACESecurityInfoPair, pi: PACEInfo): AlgorithmParameterSpec {
			// see if this is a standardized parameter or not
			return if (pip.isStandardizedParameter) {
				val index = pi.parameterID
				StandardizedDomainParameters(index).parameter
			} else {
				// use explicit domain parameters
				val pdp = requireNotNull(pip.pACEDomainParameterInfo) { "Cannot load domain parameter" }
				ExplicitDomainParameters(pdp.domainParameter).parameter
			}

		}
	}
}
