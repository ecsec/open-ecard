/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon.bind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class BindingResult {

    private BindingResultCode resultCode;
    private Body body;
    private String resultMessage;
    private Map<String, String> parameters = new HashMap<String, String>();

    public BindingResult() {
	resultCode = BindingResultCode.OK;
    }

    public BindingResult(BindingResultCode resultCode) {
	this.resultCode = resultCode;
    }

    public void setResultCode(BindingResultCode resultCode) {
	this.resultCode = resultCode;
    }

    public BindingResultCode getResultCode() {
	return resultCode;
    }

    public void setBody(Body body) {
	this.body = body;
    }

    public Body getBody() {
	return this.body;
    }

    public Map<String, String> getParameters() {
	return this.parameters;
    }

    public String addParameter(String key, String value) {
	return parameters.put(key, value);
    }

    public void addParameters(Map<String, String> parameters) {
	this.parameters.putAll(parameters);
    }

    public List<Attachment> getAttachments() {
	throw new UnsupportedOperationException();
    }

    public String getResultMessage() {
	return resultMessage;
    }

    public Map<String, String> getAuxResultData() {
	throw new UnsupportedOperationException();
    }

    public void setResultMessage(String resultMessage) {
	this.resultMessage = resultMessage;
    }

}
