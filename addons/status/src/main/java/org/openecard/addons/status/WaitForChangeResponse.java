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

import javax.annotation.Nullable;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Body;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.StatusChange;
import org.w3c.dom.Node;


/**
 * Wrapper for status change response taking care of the marshalling of the status change message.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class WaitForChangeResponse extends BindingResult {

    public WaitForChangeResponse(@Nullable StatusChange status) {
	if (status == null) {
	    setResultCode(BindingResultCode.RESOURCE_UNAVAILABLE);
	    setResultMessage("The requested session does not exist.");
	}

	try {
	    WSMarshaller m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(StatusChange.class);

	    Node xml = m.marshal(status);
	    Body body = new Body(xml, "text/xml");
	    setBody(body);
	    setResultCode(BindingResultCode.OK);
	} catch (WSMarshallerException ex) {
	    setResultCode(BindingResultCode.INTERNAL_ERROR);
	    setResultMessage("Failed to marshal StatusChange message.\n  " + ex.getMessage());
	}
    }

}
