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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.json.JSONObject;


/**
 * Result class for PKCS11 functions.
 * The result contains the result code as it is used in the C version's return value, JSON data that may be used to
 * update reference parameters and an optional error message describing the result code.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PKCS11Result {

    /**
     * Result code of the function call.
     * The possible codes are defined in {@link PKCS11ReturnCode}, but other non-standard codes may be used.
     */
    public final long resultCode;
    /**
     * Result data of the function call.
     * This value is always a valid JSON string.
     */
    @Nonnull
    public final String jsonData;
    /**
     * Error message describing the result code.
     * This value may be null.
     */
    @Nullable
    public final String errorMessage;


    /**
     * Creates a PKCS11Result instance for the given parameters.
     *
     * @param resultCode Result code of the called function.
     * @param jsonObj JSON object with the result data.
     * @param errorMessage Message describing the error or null.
     */
    public PKCS11Result(long resultCode, @Nonnull JSONObject jsonObj, @Nullable String errorMessage) {
	String data = jsonObj.toString();
	if (data != null) {
	    this.resultCode = resultCode;
	    this.jsonData = data;
	    this.errorMessage = errorMessage;
	} else {
	    // JSON data iss errornous
	    PKCS11ReturnCode code = PKCS11ReturnCode.CKR_GENERAL_ERROR;
	    this.resultCode = code.code;
	    this.jsonData = "{}";
	    this.errorMessage = code.getMessage();
	}
    }

    /**
     * Creates a PKCS11Result instance for the given parameters.
     *
     * @param resultCode Result code of the called function.
     * @param jsonObj JSON object with the result data.
     * @param errorMessage Message describing the error or null.
     */
    public PKCS11Result(@Nonnull PKCS11ReturnCode resultCode, @Nonnull JSONObject jsonObj, @Nullable String errorMessage) {
	this(resultCode.code, jsonObj, errorMessage);
    }

    /**
     * Creates a PKCS11Result instance for the given parameters.
     * <p>The result data is set to an empty JSON object ({@code {}}).</p>
     *
     * @param resultCode Result code of the called function.
     * @param errorMessage Message describing the error or null.
     */
    public PKCS11Result(long resultCode, @Nullable String errorMessage) {
	this(resultCode, "{}", errorMessage);
    }

    /**
     * Creates a PKCS11Result instance for the given parameters.
     * <p>The result data is set to an empty JSON object ({@code {}}).</p>
     *
     * @param resultCode Result code of the called function.
     * @param errorMessage Message describing the error or null.
     */
    public PKCS11Result(@Nonnull PKCS11ReturnCode resultCode, @Nullable String errorMessage) {
	this(resultCode.code, errorMessage);
    }

    /**
     * Creates a PKCS11Result instance for the given parameters.
     * <p>The result data is set to an empty JSON object ({@code {}}).<br/>
     * The error message is set to an appropriate value.</p>
     *
     * @param resultCode Result code of the called function.
     */
    public PKCS11Result(long resultCode) {
	this(resultCode, PKCS11ReturnCode.valueOf(resultCode).getMessage());
    }

    /**
     * Creates a PKCS11Result instance for the given parameters.
     * <p>The result data is set to an empty JSON object ({@code {}}).<br/>
     * The error message is set to an appropriate value.</p>
     *
     * @param resultCode Result code of the called function.
     */
    public PKCS11Result(@Nonnull PKCS11ReturnCode resultCode) {
	this(resultCode.code, resultCode.getMessage());
    }


    private PKCS11Result(long resultCode, @Nonnull String jsonData, @Nullable String errorMessage) {
	this.resultCode = resultCode;
	this.jsonData = jsonData;
	this.errorMessage = errorMessage;
    }

}
