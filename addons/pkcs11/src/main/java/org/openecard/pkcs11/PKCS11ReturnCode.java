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

package org.openecard.pkcs11;

import java.util.HashMap;


/**
 * All return codes defined in {@code pkcs11_t.h}.
 * The define name is mapped to the code number.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public enum PKCS11ReturnCode {

    CKR_OK(0x00000000),
    CKR_CANCEL(0x00000001),
    CKR_HOST_MEMORY(0x00000002),
    CKR_SLOT_ID_INVALID(0x00000003),

    /* CKR_FLAGS_INVALID was removed for v2.0 */

    /* CKR_GENERAL_ERROR and CKR_FUNCTION_FAILED are new for v2.0 */
    CKR_GENERAL_ERROR(0x00000005),
    CKR_FUNCTION_FAILED(0x00000006),

    /* CKR_ARGUMENTS_BAD, CKR_NO_EVENT, CKR_NEED_TO_CREATE_THREADS,
     * and CKR_CANT_LOCK are new for v2.01 */
    CKR_ARGUMENTS_BAD(0x00000007),
    CKR_NO_EVENT(0x00000008),
    CKR_NEED_TO_CREATE_THREADS(0x00000009),
    CKR_CANT_LOCK(0x0000000A),

    CKR_ATTRIBUTE_READ_ONLY(0x00000010),
    CKR_ATTRIBUTE_SENSITIVE(0x00000011),
    CKR_ATTRIBUTE_TYPE_INVALID(0x00000012),
    CKR_ATTRIBUTE_VALUE_INVALID(0x00000013),
    CKR_DATA_INVALID(0x00000020),
    CKR_DATA_LEN_RANGE(0x00000021),
    CKR_DEVICE_ERROR(0x00000030),
    CKR_DEVICE_MEMORY(0x00000031),
    CKR_DEVICE_REMOVED(0x00000032),
    CKR_ENCRYPTED_DATA_INVALID(0x00000040),
    CKR_ENCRYPTED_DATA_LEN_RANGE(0x00000041),
    CKR_FUNCTION_CANCELED(0x00000050),
    CKR_FUNCTION_NOT_PARALLEL(0x00000051),

    /* CKR_FUNCTION_NOT_SUPPORTED is new for v2.0 */
    CKR_FUNCTION_NOT_SUPPORTED(0x00000054),

    CKR_KEY_HANDLE_INVALID(0x00000060),

    /* CKR_KEY_SENSITIVE was removed for v2.0 */

    CKR_KEY_SIZE_RANGE(0x00000062),
    CKR_KEY_TYPE_INCONSISTENT(0x00000063),

    /* CKR_KEY_NOT_NEEDED, CKR_KEY_CHANGED, CKR_KEY_NEEDED,
     * CKR_KEY_INDIGESTIBLE, CKR_KEY_FUNCTION_NOT_PE,RMITTED,
     * CKR_KEY_NOT_WRAPPABLE, and CKR_KEY_UNEXTRACTABLE are new for
     * v2.0 */
    CKR_KEY_NOT_NEEDED(0x00000064),
    CKR_KEY_CHANGED(0x00000065),
    CKR_KEY_NEEDED(0x00000066),
    CKR_KEY_INDIGESTIBLE(0x00000067),
    CKR_KEY_FUNCTION_NOT_PERMITTED(0x00000068),
    CKR_KEY_NOT_WRAPPABLE(0x00000069),
    CKR_KEY_UNEXTRACTABLE(0x0000006A),

    CKR_MECHANISM_INVALID(0x00000070),
    CKR_MECHANISM_PARAM_INVALID(0x00000071),

    /* CKR_OBJECT_CLASS_INCONSISTENT and CKR_OBJECT_CLASS_INVALID
     * were removed for v2.0 */
    CKR_OBJECT_HANDLE_INVALID(0x00000082),
    CKR_OPERATION_ACTIVE(0x00000090),
    CKR_OPERATION_NOT_INITIALIZED(0x00000091),
    CKR_PIN_INCORRECT(0x000000A0),
    CKR_PIN_INVALID(0x000000A1),
    CKR_PIN_LEN_RANGE(0x000000A2),

    /* CKR_PIN_EXPIRED and CKR_PIN_LOCKED are new for v2.0 */
    CKR_PIN_EXPIRED(0x000000A3),
    CKR_PIN_LOCKED(0x000000A4),

    CKR_SESSION_CLOSED(0x000000B0),
    CKR_SESSION_COUNT(0x000000B1),
    CKR_SESSION_HANDLE_INVALID(0x000000B3),
    CKR_SESSION_PARALLEL_NOT_SUPPORTED(0x000000B4),
    CKR_SESSION_READ_ONLY(0x000000B5),
    CKR_SESSION_EXISTS(0x000000B6),

    /* CKR_SESSION_READ_ONLY_EXISTS and
     * CKR_SESSION_READ_WRITE_SO_EXISTS are new for v2.0 */
    CKR_SESSION_READ_ONLY_EXISTS(0x000000B7),
    CKR_SESSION_READ_WRITE_SO_EXISTS(0x000000B8),

    CKR_SIGNATURE_INVALID(0x000000C0),
    CKR_SIGNATURE_LEN_RANGE(0x000000C1),
    CKR_TEMPLATE_INCOMPLETE(0x000000D0),
    CKR_TEMPLATE_INCONSISTENT(0x000000D1),
    CKR_TOKEN_NOT_PRESENT(0x000000E0),
    CKR_TOKEN_NOT_RECOGNIZED(0x000000E1),
    CKR_TOKEN_WRITE_PROTECTED(0x000000E2),
    CKR_UNWRAPPING_KEY_HANDLE_INVALID(0x000000F0),
    CKR_UNWRAPPING_KEY_SIZE_RANGE(0x000000F1),
    CKR_UNWRAPPING_KEY_TYPE_INCONSISTENT(0x000000F2),
    CKR_USER_ALREADY_LOGGED_IN(0x00000100),
    CKR_USER_NOT_LOGGED_IN(0x00000101),
    CKR_USER_PIN_NOT_INITIALIZED(0x00000102),
    CKR_USER_TYPE_INVALID(0x00000103),

    /* CKR_USER_ANOTHER_ALREADY_LOGGED_IN and CKR_USER_TOO_MANY_TYPES
     * are new to v2.01 */
    CKR_USER_ANOTHER_ALREADY_LOGGED_IN(0x00000104),
    CKR_USER_TOO_MANY_TYPES(0x00000105),

    CKR_WRAPPED_KEY_INVALID(0x00000110),
    CKR_WRAPPED_KEY_LEN_RANGE(0x00000112),
    CKR_WRAPPING_KEY_HANDLE_INVALID(0x00000113),
    CKR_WRAPPING_KEY_SIZE_RANGE(0x00000114),
    CKR_WRAPPING_KEY_TYPE_INCONSISTENT(0x00000115),
    CKR_RANDOM_SEED_NOT_SUPPORTED(0x00000120),

    /* These are new to v2.0 */
    CKR_RANDOM_NO_RNG(0x00000121),

    /* These are new to v2.11 */
    CKR_DOMAIN_PARAMS_INVALID(0x00000130),

    /* These are new to v2.0 */
    CKR_BUFFER_TOO_SMALL(0x00000150),
    CKR_SAVED_STATE_INVALID(0x00000160),
    CKR_INFORMATION_SENSITIVE(0x00000170),
    CKR_STATE_UNSAVEABLE(0x00000180),

    /* These are new to v2.01 */
    CKR_CRYPTOKI_NOT_INITIALIZED(0x00000190),
    CKR_CRYPTOKI_ALREADY_INITIALIZED(0x00000191),
    CKR_MUTEX_BAD(0x000001A0),
    CKR_MUTEX_NOT_LOCKED(0x000001A1),

    /* The following return values are new for PKCS #11 v2.20 amendment 3 */
    CKR_NEW_PIN_MODE(0x000001B0),
    CKR_NEXT_OTP(0x000001B1),

    /* This is new to v2.20 */
    CKR_FUNCTION_REJECTED(0x00000200),
    CKR_VENDOR_DEFINED(0x80000000);


    public long code;

    private static final HashMap<Long, PKCS11ReturnCode> index;

    static {
	index = new HashMap<Long, PKCS11ReturnCode>();
	for (PKCS11ReturnCode next : values()) {
	    index.put(next.code, next);
	}
    }

    private PKCS11ReturnCode(long code) {
	this.code = code;
    }

    public String getMessage() {
	// TODO: create user readable message
	return name();
    }

    public static PKCS11ReturnCode valueOf(long code) {
	PKCS11ReturnCode result = index.get(code);
	return result != null ? result : CKR_GENERAL_ERROR;
    }

}
