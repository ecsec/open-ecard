/****************************************************************************
 * Copyright (C) 2015-2016 ecsec GmbH.
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

package org.openecard.mdlw.sal.exceptions;

import java.util.HashMap;
import javax.annotation.Nonnull;


/**
 *
 * @author Tobias Wich
 */
public abstract class CryptokiException extends Exception {

    private static final HashMap<Long, String> ERRORS = new HashMap<Long, String>() {
	{
	    put(0x00000000L, "CKR_OK");
	    put(0x00000001L, "CKR_CANCE0L");
	    put(0x00000002L, "CKR_HOST_MEMORY");
	    put(0x00000003L, "CKR_SLOT_ID_INVALID");

	    put(0x00000005L, "CKR_GENERAL_ERROR");
	    put(0x00000006L, "CKR_FUNCTION_FAILED");

	    put(0x00000007L, "CKR_ARGUMENTS_BAD");
	    put(0x00000008L, "CKR_NO_EVENT");
	    put(0x00000009L, "CKR_NEED_TO_CREATE_THREADS");
	    put(0x0000000AL, "CKR_CANT_LOCK");

	    put(0x00000010L, "CKR_ATTRIBUTE_READ_ONLY");
	    put(0x00000011L, "CKR_ATTRIBUTE_SENSITIVE");
	    put(0x00000012L, "CKR_ATTRIBUTE_TYPE_INVALID");
	    put(0x00000013L, "CKR_ATTRIBUTE_VALUE_INVALID");

	    put(0x0000001BL, "CKR_ACTION_PROHIBITED");

	    put(0x00000020L, "CKR_DATA_INVALID");
	    put(0x00000021L, "CKR_DATA_LEN_RANGE");
	    put(0x00000030L, "CKR_DEVICE_ERROR");
	    put(0x00000031L, "CKR_DEVICE_MEMORY");
	    put(0x00000032L, "CKR_DEVICE_REMOVED");
	    put(0x00000040L, "CKR_ENCRYPTED_DATA_INVALID");
	    put(0x00000041L, "CKR_ENCRYPTED_DATA_LEN_RANGE");
	    put(0x00000050L, "CKR_FUNCTION_CANCELED");
	    put(0x00000051L, "CKR_FUNCTION_NOT_PARALLEL");

	    put(0x00000054L, "CKR_FUNCTION_NOT_SUPPORTED");

	    put(0x00000060L, "CKR_KEY_HANDLE_INVALID");

	    put(0x00000062L, "CKR_KEY_SIZE_RANGE");
	    put(0x00000063L, "CKR_KEY_TYPE_INCONSISTENT");

	    put(0x00000064L, "CKR_KEY_NOT_NEEDED");
	    put(0x00000065L, "CKR_KEY_CHANGED");
	    put(0x00000066L, "CKR_KEY_NEEDED");
	    put(0x00000067L, "CKR_KEY_INDIGESTIBLE");
	    put(0x00000068L, "CKR_KEY_FUNCTION_NOT_PERMITTED");
	    put(0x00000069L, "CKR_KEY_NOT_WRAPPABLE");
	    put(0x0000006AL, "CKR_KEY_UNEXTRACTABLE");

	    put(0x00000070L, "CKR_MECHANISM_INVALID");
	    put(0x00000071L, "CKR_MECHANISM_PARAM_INVALID");

	    put(0x00000082L, "CKR_OBJECT_HANDLE_INVALID");
	    put(0x00000090L, "CKR_OPERATION_ACTIVE");
	    put(0x00000091L, "CKR_OPERATION_NOT_INITIALIZED");
	    put(0x000000A0L, "CKR_PIN_INCORRECT");
	    put(0x000000A1L, "CKR_PIN_INVALID");
	    put(0x000000A2L, "CKR_PIN_LEN_RANGE");

	    put(0x000000A3L, "CKR_PIN_EXPIRED");
	    put(0x000000A4L, "CKR_PIN_LOCKED");

	    put(0x000000B0L, "CKR_SESSION_CLOSED");
	    put(0x000000B1L, "CKR_SESSION_COUNT");
	    put(0x000000B3L, "CKR_SESSION_HANDLE_INVALID");
	    put(0x000000B4L, "CKR_SESSION_PARALLEL_NOT_SUPPORTED");
	    put(0x000000B5L, "CKR_SESSION_READ_ONLY");
	    put(0x000000B6L, "CKR_SESSION_EXISTS");

	    put(0x000000B7L, "CKR_SESSION_READ_ONLY_EXISTS");
	    put(0x000000B8L, "CKR_SESSION_READ_WRITE_SO_EXISTS");

	    put(0x000000C0L, "CKR_SIGNATURE_INVALID");
	    put(0x000000C1L, "CKR_SIGNATURE_LEN_RANGE");
	    put(0x000000D0L, "CKR_TEMPLATE_INCOMPLETE");
	    put(0x000000D1L, "CKR_TEMPLATE_INCONSISTENT");
	    put(0x000000E0L, "CKR_TOKEN_NOT_PRESENT");
	    put(0x000000E1L, "CKR_TOKEN_NOT_RECOGNIZED");
	    put(0x000000E2L, "CKR_TOKEN_WRITE_PROTECTED");
	    put(0x000000F0L, "CKR_UNWRAPPING_KEY_HANDLE_INVALID");
	    put(0x000000F1L, "CKR_UNWRAPPING_KEY_SIZE_RANGE");
	    put(0x000000F2L, "CKR_UNWRAPPING_KEY_TYPE_INCONSISTENT");
	    put(0x00000100L, "CKR_USER_ALREADY_LOGGED_IN");
	    put(0x00000101L, "CKR_USER_NOT_LOGGED_IN");
	    put(0x00000102L, "CKR_USER_PIN_NOT_INITIALIZED");
	    put(0x00000103L, "CKR_USER_TYPE_INVALID");

	    put(0x00000104L, "CKR_USER_ANOTHER_ALREADY_LOGGED_IN");
	    put(0x00000105L, "CKR_USER_TOO_MANY_TYPES");

	    put(0x00000110L, "CKR_WRAPPED_KEY_INVALID");
	    put(0x00000112L, "CKR_WRAPPED_KEY_LEN_RANGE");
	    put(0x00000113L, "CKR_WRAPPING_KEY_HANDLE_INVALID");
	    put(0x00000114L, "CKR_WRAPPING_KEY_SIZE_RANGE");
	    put(0x00000115L, "CKR_WRAPPING_KEY_TYPE_INCONSISTENT");
	    put(0x00000120L, "CKR_RANDOM_SEED_NOT_SUPPORTED");

	    put(0x00000121L, "CKR_RANDOM_NO_RNG");

	    put(0x00000130L, "CKR_DOMAIN_PARAMS_INVALID");

	    put(0x00000140L, "CKR_CURVE_NOT_SUPPORTED");

	    put(0x00000150L, "CKR_BUFFER_TOO_SMALL");
	    put(0x00000160L, "CKR_SAVED_STATE_INVALID");
	    put(0x00000170L, "CKR_INFORMATION_SENSITIVE");
	    put(0x00000180L, "CKR_STATE_UNSAVEABLE");

	    put(0x00000190L, "CKR_CRYPTOKI_NOT_INITIALIZED");
	    put(0x00000191L, "CKR_CRYPTOKI_ALREADY_INITIALIZED");
	    put(0x000001A0L, "CKR_MUTEX_BAD");
	    put(0x000001A1L, "CKR_MUTEX_NOT_LOCKED");

	    put(0x000001B0L, "CKR_NEW_PIN_MODE");
	    put(0x000001B1L, "CKR_NEXT_OTP");

	    put(0x000001B5L, "CKR_EXCEEDED_MAX_ITERATIONS");
	    put(0x000001B6L, "CKR_FIPS_SELF_TEST_FAILED");
	    put(0x000001B7L, "CKR_LIBRARY_LOAD_FAILED");
	    put(0x000001B8L, "CKR_PIN_TOO_WEAK");
	    put(0x000001B9L, "CKR_PUBLIC_KEY_INVALID");

	    put(0x00000200L, "CKR_FUNCTION_REJECTED");

	    put(0x80000000L, "CKR_VENDOR_DEFINED");
	}
    };

    private final long errorCode;

    public CryptokiException(String msg, long resultCode) {
	super(msg);
	this.errorCode = resultCode;
    }

    @Override
    public String getMessage() {
	return String.format("[%s] %s", getErrorConstantName(), super.getMessage());
    }

    public long getErrorCode() {
	return errorCode;
    }

    @Nonnull
    public String getErrorConstantName() {
	return getErrorConstantName(errorCode);
    }

    @Nonnull
    public static String getErrorConstantName(long resultCode) {
        String res = ERRORS.get(resultCode);
        if (res != null && !res.equals("")) {
            return ERRORS.get(resultCode);
        } else {
            return "UNKOWN ERROR";
        }
    }

}
