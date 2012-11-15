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

package org.openecard.client.control.binding.http.handler;

import java.net.URL;
import java.net.URLEncoder;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.openecard.client.common.util.FileUtils;
import org.openecard.client.control.module.tctoken.TCTokenRequest;
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenHandlerTest {

    // TODO fix this test or remove
    /*TCTokenHandler instance = new TCTokenHandler();

    @Test
    public void testHandleRequest() throws Exception {
	URL tokenUrl = FileUtils.resolveResourceAsURL(getClass(), "TCToken.xml");
	String tokenUrlCoded = URLEncoder.encode(tokenUrl.toExternalForm(), "utf-8");
	String uri = "/eID-Client?tcTokenURL=" + tokenUrlCoded;
	HttpRequest httpRequest = new BasicHttpRequest("GET", uri);

	TCTokenRequest result = (TCTokenRequest) instance.handleRequest(httpRequest);
    }*/

}
