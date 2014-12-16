/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.ResponseBody;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
import org.w3c.dom.Node;


/**
 * Wrapper for status response taking care of the marshalling of the status message.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public final class StatusResponse extends BindingResult {

    public StatusResponse(Status status) {
	try {
	    WSMarshaller m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(Status.class);

	    Node xml = m.marshal(status);
	    ResponseBody body = new ResponseBody(xml, "text/xml");
	    setBody(body);
	    setResultCode(BindingResultCode.OK);
	} catch (WSMarshallerException ex) {
	    setResultCode(BindingResultCode.INTERNAL_ERROR);
	    setResultMessage("Failed to marshal Status message.\n  " + ex.getMessage());
	}
    }

}
