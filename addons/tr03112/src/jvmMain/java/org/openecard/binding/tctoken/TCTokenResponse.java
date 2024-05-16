/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import java.net.URISyntaxException;
import java.util.concurrent.Future;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.common.util.UrlBuilder;


/**
 * Implements a TCTokenResponse.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class TCTokenResponse extends BindingResult {

    private static final I18n LANG = I18n.getTranslation("tr03112");

    private Result result;
    private TCToken token;
    private Future<?> bindingTask;

    /**
     * Returns the result of the client request.
     *
     * @return Result
     */
    public Result getResult() {
	if (result == null) {
	    result = WSHelper.makeResultOK();
	}
	return result;
    }

    /**
     * Sets the result of the client request.
     *
     * @param result
     */
    public void setResult(Result result) {
	this.result = result;
    }

    public void setAdditionalResultMinor(String minor) {
	this.addAuxResultData(AuxDataKeys.MINOR_PROCESS_RESULT, minor);
    }

    /**
     * Sets the TCToken as received in the request.
     *
     * @param token The TCToken.
     */
    public void setTCToken(TCToken token) {
	this.token = token;
    }

    /**
     * Gets the TCToken of the request.
     *
     * @return The TCToken.
     */
    public TCToken getTCToken() {
	return token;
    }

    /**
     * Returns the refresh address.
     *
     * @return Refresh address
     */
    public String getRefreshAddress() {
	return token.getRefreshAddress();
    }

    /**
     * Sets the refresh address in the underlying TCToken.
     *
     * @param addr The new refresh address.
     */
    public void setRefreshAddress(String addr) {
	token.setRefreshAddress(addr);
    }

    public void setBindingTask(Future<?> bindingTask) {
	this.bindingTask = bindingTask;
    }

    public Future<?> getBindingTask() {
	return bindingTask;
    }

    /**
     * Completes the response, so that it can be used in the binding.
     * The values extended include result code, result message and the redirect address.
     *
     * @throws InvalidRedirectUrlException Thrown in case the error redirect URL could not be determined.
     */
    public void finishResponse() throws InvalidRedirectUrlException {
	try {
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    UrlBuilder ub = UrlBuilder.fromUrl(getRefreshAddress());
	    if (ECardConstants.Major.OK.equals(result.getResultMajor())) {
		setResultCode(BindingResultCode.REDIRECT);
		String refreshURL = ub.queryParam("ResultMajor", "ok").build().toString();
		getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, refreshURL);
	    } else {
		boolean isRefreshAddressValid = (Boolean) dynCtx.get(TR03112Keys.IS_REFRESH_URL_VALID);
		String minor = result.getResultMinor();
		setResultCode(BindingResultCode.REDIRECT);
		String refreshURL;

		if (isRefreshAddressValid) {
		    String fixedMinor = TCTokenHacks.fixResultMinor(minor);
		    refreshURL = ub.queryParam("ResultMajor", "error")
			    .queryParamUrl("ResultMinor", fixedMinor)
			    .build().toString();
		} else {
		    refreshURL = token.getComErrorAddressWithParams(minor);
		}

		getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, refreshURL);

		if (result.getResultMessage().getValue() != null) {
		    setResultMessage(result.getResultMessage().getValue());
		}
	    }
	} catch (URISyntaxException ex) {
	    // this is a code failure as the URLs are verified upfront
	    // TODO: translate when exception changes
	    throw new IllegalArgumentException(LANG.getOriginalMessage(INVALID_URL), ex);
	}
    }

}
