/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import java.net.MalformedURLException;
import java.net.URL;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;


/**
 * Base class for communication errors.
 * This is to be used when the binding should perform a redirect to the communication address given in the TCToken.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CommunicationError extends Exception {

    public final String communicationErrorAddress;
    public final String minor;

    public CommunicationError(String communicationErrorAddress, String minor, String msg) {
	this(communicationErrorAddress, minor, msg, null);
    }

    public CommunicationError(String communicationErrorAddress, String minor, String msg, Throwable cause) {
	super(msg, cause);
	this.communicationErrorAddress = communicationErrorAddress;
	this.minor = minor;
    }

    public BindingResult getResult() throws MalformedURLException {
	BindingResult result = new BindingResult(BindingResultCode.REDIRECT);
	URL commUrl = new URL(communicationErrorAddress);
	commUrl = TCTokenHacks.addParameterToUrl(commUrl, "ResultMajor", "error");
	commUrl = TCTokenHacks.addParameterToUrl(commUrl, "ResultMinor", minor);
	result.getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, commUrl.toString());
	result.setResultMessage(getMessage());
	return result;
    }

}
