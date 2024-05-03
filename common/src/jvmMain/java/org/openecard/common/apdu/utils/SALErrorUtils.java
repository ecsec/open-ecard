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

package org.openecard.common.apdu.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.openecard.common.ECardConstants.*;
import org.openecard.common.util.ByteUtils;


/**
 * Utility functions to determine SAL error codes based on APDU result codes of the tokens.
 *
 * @author Tobias Wich
 */
public class SALErrorUtils {

    @Nonnull
    public static String getMajor(byte[] code) {
	if (code == null || code.length != 2) {
	    throw new IllegalArgumentException("Given response code is not exactly two bytes long.");
	}
	String codeStr = ByteUtils.toHexString(code);

	if ("9000".equals(codeStr) || codeStr.startsWith("61")) {
	    return Major.OK;
	} else {
	    return Major.ERROR;
	}
    }

    @Nullable
    public static String getMinor(byte[] code) {
	if (code == null || code.length != 2) {
	    throw new IllegalArgumentException("Given response code is not exactly two bytes long.");
	}
	String codeStr = ByteUtils.toHexString(code);
	String defaultError = Minor.App.UNKNOWN_ERROR;

	// TODO: add more codes

	if ("9000".equals(codeStr) || codeStr.startsWith("61")) {
	    return null;
	} else if (codeStr.startsWith("69")) {
	    if (codeStr.endsWith("82")) {
		return Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED;
	    } else {
		return defaultError;
	    }
	} else {
	    return defaultError;
	}
    }
}
