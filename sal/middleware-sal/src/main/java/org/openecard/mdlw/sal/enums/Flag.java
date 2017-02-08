/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
 * All rights reserved.
 * Contact), ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met),
 * http),//www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively), this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mdlw.sal.enums;


/**
 *
 * @author Jan Mannsbart
 */
public enum Flag {

    /**
     * The token has its own random number generator.
     */
    CKF_RNG(0x00000001),

    CKF_WRITE_PROTECTED(0x00000002),

    /**
     * There are some cryptographic functions that a user MUST be logged in to perform.
     */
    CKF_LOGIN_REQUIRED(0x00000004),

    CKF_RESTORE_KEY_NOT_NEEDED(0x00000020),

    CKF_CLOCK_ON_TOKEN(0x00000040),

    /**
     * Capture pin directly on reader.
     */
    CKF_PROTECTED_AUTHENTICATION_PATH(0x00000100),

    CKF_DUAL_CRYPTO_OPERATIONS(0x00000200),

    /**
     * The token has been initialized using C_InitToken or an equivalent mechanism outside the scope of this standard.
     * Calling C_InitToken when this flag is set will cause the token to be reinitialized.
     */
    CKF_TOKEN_INITIALIZED(0x00000400),

    CKF_SECONDARY_AUTHENTICATION(0x00000800),

    /**
     * The normal user’s PIN has been initialized.
     */
    CKF_USER_PIN_INITIALIZED(0x00000008),
    /**
     * An incorrect user login PIN has been entered at least once since the last successful authentication.
     */
    CKF_USER_PIN_COUNT_LOW(0x00010000),
    /**
     * Supplying an incorrect user PIN will cause it to become locked.
     */
    CKF_USER_PIN_FINAL_TRY(0x00020000),
    /**
     * The user PIN has been locked. User login to the token is not possible.
     */
    CKF_USER_PIN_LOCKED(0x00040000),
    /**
     * The user PIN value is the default value set by token initialization or manufacturing, or the PIN has been expired by the card.
     */
    CKF_USER_PIN_TO_BE_CHANGED(0x00080000),

    /**
     * An incorrect SO login PIN has been entered at least once since the last successful authentication.
     */
    CKF_SO_PIN_COUNT_LOW(0x00100000),
    /**
     * Supplying an incorrect SO PIN will cause it to become locked.
     */
    CKF_SO_PIN_FINAL_TRY(0x00200000),
    /**
     * The SO PIN has been locked. SO login to the token is not possible.
     */
    CKF_SO_PIN_LOCKED(0x00400000),
    /**
     * The SO PIN value is the default value set by token initialization or manufacturing, or the PIN has been expired by the card.
     */
    CKF_SO_PIN_TO_BE_CHANGED(0x00800000),

    CKF_ERROR_STATE(0x01000000);

    private final long value;

    private Flag(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

}
