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
 ***************************************************************************/

package org.openecard.common.tlv.iso7816;

import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;


/**
 * The class models the AccessControlRule data type from ISO 7816-15.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AccessControlRule extends TLVType {

    /**
     * The access modes coded a bit string.
     */
    private TLVBitString accessMode;

    /**
     * The security conditions.
     */
    private SecurityConditionChoice securityCondition;

    /**
     * The constructor sets the TLV which contains the AccessControlRule.
     *
     * @param tlv AccessControlRule containing TLV.
     * @throws TLVException
     */
    public AccessControlRule(TLV tlv) throws TLVException {
	super(tlv);

	// first tag have to be the access mode
	accessMode = new TLVBitString(tlv);

	// second element have t be the security condition
	securityCondition = new SecurityConditionChoice(tlv.getNext());
    }

    /**
     * Gets the value of the accessMode property.
     *
     * @return A {@link TLVBitString} object which codes the access modes.
     */
    public TLVBitString getAccessMode() {
	return accessMode;
    }

    /**
     * Gets the value of the securityCondition property.
     *
     * @return A {@link SecurityConditionChoice} object which codes the specific security conditions which have to be
     * full filled to get access to the operations defined in the access mode byte.
     */
    public SecurityConditionChoice getSecurityCondition() {
	return securityCondition;
    }

}
