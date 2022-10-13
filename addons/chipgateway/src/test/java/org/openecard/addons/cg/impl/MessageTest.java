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

package org.openecard.addons.cg.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import java.io.IOException;
import org.openecard.ws.chipgateway.HelloRequestType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class MessageTest {

    @Test
    public void testHelloRequest() throws JsonProcessingException, IOException {
	ObjectMapper mapper = new ObjectMapper();
	mapper.registerModule(new JakartaXmlBindAnnotationModule());

	HelloRequestType req = new HelloRequestType();
	req.setSessionIdentifier("1234abcd");
	req.setChallenge(new byte[]{0, 1, 2, 3});
	req.setVersion("1.2.3");

	String result = mapper.writeValueAsString(req);
	HelloRequestType req1 = mapper.readValue(result, HelloRequestType.class);

	// load reference
	String inputRef = "{\"Challenge\" : \"00010203\", \"Version\" : \"1.2.3\", \"SessionIdentifier\" : \"1234abcd\"}";
	HelloRequestType reference = mapper.readValue(inputRef, HelloRequestType.class);

	Assert.assertEquals(reference.getSessionIdentifier(), req1.getSessionIdentifier());
	Assert.assertEquals(reference.getChallenge(), req1.getChallenge());
	Assert.assertEquals(reference.getVersion(), req1.getVersion());
    }

}
