/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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

package org.openecard.addons.status;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.ResponseBody;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
import org.openecard.ws.schema.StatusChange;


/**
 * Specialized ResponseBody capable of marshalling wait for change messages.
 *
 * @author Tobias Wich
 */
public class StatusResponseBodyFactory {

    private final WSMarshaller m;

    public StatusResponseBodyFactory() throws WSMarshallerException {
	m = WSMarshallerFactory.createInstance();
    }

    public BindingResult createStatusResponse(@Nonnull Status status) {
	BindingResult result = new BindingResult();
	try {
	    ResponseBody body = new ResponseBody();
	    String value = m.doc2str(m.marshal(status));
	    // TODO: is this mime type an error or is there a reason for that?
	    body.setValue(value, "text/plain");
	    result.setBody(body);
	    result.setResultCode(BindingResultCode.OK);
	} catch (WSMarshallerException | TransformerException ex) {
	    result.setResultCode(BindingResultCode.INTERNAL_ERROR);
	    result.setResultMessage("Failed to marshal Status message.\n  " + ex.getMessage());
	}
	return result;
    }

    public BindingResult createWaitForChangeResponse(@Nullable StatusChange status) {
	BindingResult result = new BindingResult();
	if (status == null) {
	    result.setResultCode(BindingResultCode.RESOURCE_UNAVAILABLE);
	    result.setResultMessage("The requested session does not exist.");
	} else {
	    try {
		ResponseBody body = new ResponseBody();
		String value = m.doc2str(m.marshal(status));
		body.setValue(value, "text/xml");
		result.setBody(body);
		result.setResultCode(BindingResultCode.OK);
	    } catch (WSMarshallerException | TransformerException ex) {
		result.setResultCode(BindingResultCode.INTERNAL_ERROR);
		result.setResultMessage("Failed to marshal StatusChange message.\n  " + ex.getMessage());
	    }
	}
	return result;
    }

}
