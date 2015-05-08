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

package org.openecard.control.binding.http.handler.common;

import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.control.binding.http.HttpException;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.openecard.control.binding.http.common.Http11Response;
import org.openecard.control.binding.http.handler.ControlCommonHandler;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class IndexHandler extends ControlCommonHandler {

    /**
     * Create a new debug handler.
     */
    public IndexHandler() {
	super("/");
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpException, Exception {
	HttpResponse httpResponse = new Http11Response(HttpStatus.SC_SEE_OTHER);
	httpResponse.setHeader(HeaderTypes.LOCATION.fieldName(), "/index.html");

	return httpResponse;
    }

}
