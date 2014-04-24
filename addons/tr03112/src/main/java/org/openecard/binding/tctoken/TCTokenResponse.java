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

package org.openecard.binding.tctoken;

import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.Future;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;


/**
 * Implements a TCTokenResponse.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenResponse extends BindingResult {

    private Result result;
    private URL refreshAddress;
    private URL communicationErrorAddress;
    private Future<StartPAOSResponse> bindingTask;

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

    /**
     * Returns the refresh address.
     *
     * @return Refresh address
     */
    public URL getRefreshAddress() {
	return refreshAddress;
    }

    /**
     * Sets the refresh address.
     *
     * @param refreshAddress Refresh address
     */
    public void setRefreshAddress(URL refreshAddress) {
	this.refreshAddress = refreshAddress;
    }

    public void setRefreshAddress(String refreshAddress) throws MalformedURLException {
	if (refreshAddress != null) {
	    this.refreshAddress = new URL(refreshAddress);
	}
    }

    public URL getCommunicationErrorAddress() {
	return communicationErrorAddress;
    }

    public void setCommunicationErrorAddress(URL communicationErrorAddress) {
	this.communicationErrorAddress = communicationErrorAddress;
    }

    public void setCommunicationErrorAddress(String communicationErrorAddress) throws MalformedURLException {
	if (communicationErrorAddress != null) {
	    this.communicationErrorAddress = new URL(communicationErrorAddress);
	}
    }

    public void setBindingTask(Future<StartPAOSResponse> bindingTask) {
	this.bindingTask = bindingTask;
    }

    public Future<StartPAOSResponse> getBindingTask() {
	return bindingTask;
    }

    public void finishResponse() throws MalformedURLException, UnsupportedEncodingException, CommunicationError {
	// TODO: localize these error messages
	BindingResult httpResponse = new BindingResult();

	if (getRefreshAddress() == null) {
	    if (getCommunicationErrorAddress() == null) {
		httpResponse.setResultCode(BindingResultCode.WRONG_PARAMETER);
		httpResponse.setResultMessage("No refresh or error address could be determined.");
	    } else {
		String msg = "No refresh address could be determined.";
		throw new CommunicationError(getCommunicationErrorAddress().toString(), "communicationError", msg);
	    }
	} else {
	    // address available
	    if (ECardConstants.Major.OK.equals(result.getResultMajor())) {
		setResultCode(BindingResultCode.REDIRECT);
		URL refreshURL = TCTokenHacks.addParameterToUrl(getRefreshAddress(), "ResultMajor", "ok");
		getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, refreshURL.toString());
	    } else {
		setResultCode(BindingResultCode.REDIRECT);
		URL refreshURL = TCTokenHacks.addParameterToUrl(getRefreshAddress(), "ResultMajor", "error");
		// TODO: set ResultMinor
		String encodedResultMinor = URLDecoder.decode(result.getResultMinor(), "UTF-8");
		refreshURL = TCTokenHacks.addParameterToUrl(refreshURL, "ResultMinor", encodedResultMinor);
		getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, refreshURL.toString());

		if (result.getResultMessage().getValue() != null) {
		    setResultMessage(result.getResultMessage().getValue());
		}
	    }
	}
    }

}
