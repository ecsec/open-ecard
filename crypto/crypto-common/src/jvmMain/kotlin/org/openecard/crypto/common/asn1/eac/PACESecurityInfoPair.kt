/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
 * Wrapper to bind [PACEInfo] and [PACEDomainParameterInfo] together.
 *
 * @author Tobias Wich
 */
class PACESecurityInfoPair
/**
 * Creates pair based on the given values.
 *
 * @param pACEInfo PACEInfo object.
 * @param pACEDomainParameterInfo Domain Parameters. `null` for standard parameters.
 */(val pACEInfo: PACEInfo, val pACEDomainParameterInfo: PACEDomainParameterInfo?) {
    val isStandardizedParameter: Boolean
        get() {
            val id: Int = pACEInfo.parameterID
            return id >= 0 && id <= 31
        }

    /**
     * Creates a PACEDomainParameter object based on this pair.
     *
     * @return The new PACEDomainParameter object.
     */
    fun createPACEDomainParameter(): PACEDomainParameter {
        return PACEDomainParameter(this)
    }
}
