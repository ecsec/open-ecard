/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

package org.openecard.mobile.activation;


/**
 *
 * @author Mike
 */
public class ActivationResult {

    private final String redirectUrl;
    private final ActivationResultCode resultCode;
    private final String errorMessage;

    public ActivationResult(ActivationResultCode resultCode) {
	this(null, resultCode, null);
    }

    public ActivationResult(ActivationResultCode resultCode, String errorMessage) {
	this(null, resultCode, errorMessage);
    }

    public ActivationResult(String redirectUrl, ActivationResultCode resultCode) {
	this(redirectUrl, resultCode, null);
    }

    public ActivationResult(String redirectUrl, ActivationResultCode resultCode, String errorMessage) {
	this.redirectUrl = redirectUrl;
	this.resultCode = resultCode;
	this.errorMessage = errorMessage;
    }

    public String getRedirectUrl() {
	return redirectUrl;
    }

    public ActivationResultCode getResultCode() {
	return resultCode;
    }

    public String getErrorMessage() {
	return errorMessage;
    }

}
