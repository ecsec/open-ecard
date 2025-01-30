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

/**
 * Implements the SecurityInfos for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class PACESecurityInfos {
    /**
     * Returns the PACEDomainParameterInfos.
     *
     * @return PACEDomainParameterInfos
     */
    /**
     * Sets the PACEDomainParameterInfos.
     *
     * @param paceDomainParameterInfos PACEDomainParameterInfos
     */
    var pACEDomainParameterInfos: MutableList<PACEDomainParameterInfo> = mutableListOf()
	/**
     * Returns the PACEInfos.
     *
     * @return PACEInfos
     */
    /**
     * Sets the PACEInfos.
     *
     * @param paceInfos PACEInfos
     */
    var pACEInfos: MutableList<PACEInfo> = mutableListOf()

	/**
	 * Gets the PACEInfo pairs that are contained in SecurityInfos object.
	 *
	 * @return List containing all PACEInfo pairs.
	 */
	 val pACEInfoPairs: List<PACESecurityInfoPair> by lazy {
		createPACEInfoPairs()
	}
	/**
     * Adds a PACEDomainParameterInfo.
     *
     * @param paceDomainParameterInfo PACEDomainParameterInfo
     */
    fun addPACEDomainParameterInfo(paceDomainParameterInfo: PACEDomainParameterInfo) {
        this.pACEDomainParameterInfos.add(paceDomainParameterInfo)
    }

    /**
     * Adds a PACEInfo.
     *
     * @param paceInfo PACEInfo
     */
    fun addPACEInfo(paceInfo: PACEInfo) {
        this.pACEInfos.add(paceInfo)
    }


    fun getPACEInfoPairs(
        supportedProtocols: List<String>,
        supportedParams: List<Int>,
    ): List<PACESecurityInfoPair> {
        val result = mutableListOf<PACESecurityInfoPair>()
        for (next in getPACEInfoPairs(supportedProtocols)) {
            if (supportedParams.contains(next.pACEInfo.parameterID)) {
                result.add(next)
            }
        }
        return result
    }

    fun getPACEInfoPairs(supportedProtocols: List<String>): List<PACESecurityInfoPair> {
        val result = mutableListOf<PACESecurityInfoPair>()
        for (next in this.pACEInfoPairs) {
            if (supportedProtocols.contains(next.pACEInfo.protocol)) {
                result.add(next)
            }
        }
        return result
    }

    private fun createPACEInfoPairs(): MutableList<PACESecurityInfoPair> {
        val result = mutableListOf<PACESecurityInfoPair>()

        // special case when there is only one element
        // in that case the parameter id is optional because a binding of explicit Domain Parameters is implicit
        if (pACEInfos.size == 1) {
            if (pACEDomainParameterInfos.isEmpty()) {
                result.add(PACESecurityInfoPair(pACEInfos[0], null))
            } else {
                result.add(PACESecurityInfoPair(pACEInfos[0], pACEDomainParameterInfos[0]))
            }
            return result
        }

        for (pi in this.pACEInfos) {
            val id = pi.parameterID
            var found = false
            if (id != -1) {
                for (dpi in this.pACEDomainParameterInfos) {
                    if (id == dpi.parameterID) {
                        found = true
                        result.add(PACESecurityInfoPair(pi, dpi))
                        break
                    }
                }
            }
            if (!found) {
                result.add(PACESecurityInfoPair(pi, null))
            }
        }
        return result
    }
}
