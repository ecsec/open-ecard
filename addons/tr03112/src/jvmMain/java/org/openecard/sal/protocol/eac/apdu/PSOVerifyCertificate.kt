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
 */
package org.openecard.sal.protocol.eac.apdu

import org.openecard.common.apdu.PerformSecurityOperation

/**
 * Implements a PSO:VerifyCertificate APDU for Terminal Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.5.
 *
 * @author Moritz Horsch
 */
class PSOVerifyCertificate(certificate: ByteArray?) : PerformSecurityOperation() {
    /**
     * Creates a new PSO:VerifyCertificate APDU.
     *
     * @param certificate Certificate
     */
    init {
        setP2(0xBE.toByte())
        setData(certificate)
    }
}
