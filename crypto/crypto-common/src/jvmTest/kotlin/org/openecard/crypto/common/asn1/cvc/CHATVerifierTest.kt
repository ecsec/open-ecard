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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.util.StringUtils
import org.openecard.crypto.common.asn1.cvc.CHATVerifier.verfiy
import org.testng.annotations.Test
import java.security.GeneralSecurityException

/**
 *
 * @author Moritz Horsch
 */
class CHATVerifierTest {
    @Test
    fun testVerfiy() {
        val chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffb7")

        val c1 = CHAT(chatBytes)
        c1.setReadAccess(CHAT.DataGroup.DG01, !false)
        c1.setReadAccess(CHAT.DataGroup.DG02, !false)

        val c2 = CHAT(chatBytes)
        c2.setReadAccess(CHAT.DataGroup.DG01, false)
        c2.setReadAccess(CHAT.DataGroup.DG02, false)

        verfiy(c1, c2)
    }

    @Test(expectedExceptions = [GeneralSecurityException::class])
    fun testVerfiy2() {
        val chatBytes = StringUtils.toByteArray("7f4c12060904007f0007030102025305300301ffb7")

        val c1 = CHAT(chatBytes)
        c1.setReadAccess(CHAT.DataGroup.DG01, !false)
        c1.setReadAccess(CHAT.DataGroup.DG02, !false)

        val c2 = CHAT(chatBytes)
        c2.setReadAccess(CHAT.DataGroup.DG01, false)
        c2.setReadAccess(CHAT.DataGroup.DG02, false)

        verfiy(c2, c1)
    }
}
