/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.common.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.RequestType;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import javax.annotation.Nonnull;
import org.openecard.common.util.JAXBUtils;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshallerException;


/**
 *
 * @author Tobias Wich
 * @param <Request>
 * @param <Response>
 */
public class ApiCallEventObject <Request extends RequestType, Response extends ResponseType> extends EventObject {

    protected final Request req;
    protected Response res;

    public ApiCallEventObject(ConnectionHandleType handle, Request req) {
	super(handle);
	this.req = copyMessage(req);
    }

    @Nonnull
    private <T> T copyMessage(@Nonnull T msg) {
	try {
	    return JAXBUtils.deepCopy(msg);
	} catch (MarshallingTypeException ex) {
	    throw new RuntimeException("The requested type is not supported by the marshaller.", ex);
	} catch (WSMarshallerException ex) {
	    throw new RuntimeException("Error initializing the marshaller.", ex);
	}
    }

    public void setResponse(@Nonnull Response res) {
	this.res = copyMessage(res);
    }

    public Request getRequest() {
	return req;
    }

    public Response getResponse() {
	return res;
    }

    public boolean hasResponse() {
	return getResponse() != null;
    }

}
