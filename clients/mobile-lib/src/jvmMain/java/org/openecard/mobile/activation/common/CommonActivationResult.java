/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.mobile.activation.common;

import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;

import java.util.*;

/**
 *
 * @author Neil Crossley
 */
public class CommonActivationResult implements ActivationResult {

    private final String redirectUrl;
    private final ActivationResultCode resultCode;
    private final String errorMessage;
    private String processMinor;
	private Map<String, String> resultParameters;

    public CommonActivationResult(ActivationResultCode resultCode) {
	this(null, resultCode, null);
    }

    public CommonActivationResult(ActivationResultCode resultCode, String errorMessage) {
	this(null, resultCode, errorMessage);
    }

    public CommonActivationResult(String redirectUrl, ActivationResultCode resultCode) {
	this(redirectUrl, resultCode, null);
    }

    public CommonActivationResult(String redirectUrl, ActivationResultCode resultCode, String errorMessage) {
	this.redirectUrl = redirectUrl;
	this.resultCode = resultCode;
	this.errorMessage = errorMessage;
	this.resultParameters = new HashMap();
    }

    public void setProcessMinor(String processMinor) {
	this.processMinor = processMinor;
    }

    @Override
    public String getRedirectUrl() {
	return redirectUrl;
    }

    @Override
    public ActivationResultCode getResultCode() {
	return resultCode;
    }

    @Override
    public String getErrorMessage() {
	return errorMessage;
    }

    @Override
    public String getProcessResultMinor() {
	return processMinor;
    }

	public void addParams(Map<String, String> params) {
		if (params != null) {
			resultParameters.putAll(params);
		}
	}

	public Set<String> getResultParameterKeys() {
		return resultParameters.keySet();
	}

	public String getResultParameter(String key) {
		return resultParameters.get(key);
	}

	@Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("[ActivationResult resultCode:");
	builder.append(resultCode);
	if (processMinor != null) {
	    builder.append(", processMinor:");
	    builder.append(processMinor);
	}
	if (redirectUrl != null) {
	    builder.append(", redirectUrl:");
	    builder.append(redirectUrl);
	}
	if (errorMessage != null) {
	    builder.append(", errorMessage:");
	    builder.append(errorMessage);
	}
	builder.append("]");
	return builder.toString();
    }
}
