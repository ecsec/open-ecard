/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.TLV

/**
 * The class models the AccessControlRule data type from ISO 7816-15.
 *
 * @author Hans-Martin Haase
 */
class AccessControlRule(tlv: TLV) : TLVType(tlv) {
    /**
     * Gets the value of the accessMode property.
     *
     * @return A [TLVBitString] object which codes the access modes.
     */
    // first tag have to be the access mode
    /**
     * The access modes coded a bit string.
     */
    val accessMode: TLVBitString = TLVBitString(tlv)

    /**
     * Gets the value of the securityCondition property.
     *
     * @return A [SecurityConditionChoice] object which codes the specific security conditions which have to be
     * full filled to get access to the operations defined in the access mode byte.
     */

    // second element have t be the security condition
    /**
     * The security conditions.
     */
    val securityCondition: SecurityConditionChoice = SecurityConditionChoice(tlv.next)
}
