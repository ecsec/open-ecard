/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import javax.annotation.Nullable;


/**
 *
 * @author Tobias Wich
 */
public class ChipGatewayStatusCodes {

    private static final String PREFIX = "http://ws.openecard.org/result/";
    private static final String PREFIX_ERR = "http://ws.openecard.org/result/error/";
    private static final String PREFIX_WARN = "http://ws.openecard.org/result/warning/";

    public static final String OK = PREFIX + "ok";

    public static final String UNKNOWN_SESSION = PREFIX_ERR + "unknownSessionIdentifier";
    public static final String UNSUITABLE_SESSION = PREFIX_ERR + "unsuitableSessionIdentifier";
    public static final String UNSUITABLE_CHALLENGE = PREFIX_ERR + "unsuitableChallenge";
    public static final String INCORRECT_PARAMETER = PREFIX_ERR + "incorrectParameter";
    public static final String OTHER = PREFIX_ERR + "other";
    public static final String STOPPED = PREFIX_ERR + "stopped";
    public static final String UPDATE_REQUIRED = PREFIX_ERR + "updateRequired";
    public static final String UPDATE_RECOMMENDED = PREFIX_WARN + "updateRecommended";
    public static final String TIMEOUT = PREFIX_ERR + "timeout";
    public static final String UNKNOWN_SLOT = PREFIX_ERR + "unknownSlotHandle";
    public static final String SECURITY_NOT_SATISFIED = PREFIX_ERR + "securityConditionNotSatisfied";
    public static final String UNKNOWN_DID = PREFIX_ERR + "unknownDIDName";

    public static boolean isOk(@Nullable String code) {
	return OK.equals(code) || UPDATE_RECOMMENDED.equals(code);
    }

    public static boolean isError(@Nullable String code) {
	return ! isOk(code);
    }

}
