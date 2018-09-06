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

package org.openecard.common.util;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.interfaces.ObjectSchemaValidator;
import org.openecard.common.interfaces.ObjectValidatorException;
import org.openecard.ws.marshal.WSMarshallerException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 *
 * @author Hans-Martin Haase
 */
public class SchemaValidationTest {

    @Test
    public void testDIDAuth() throws JAXBException, WSMarshallerException, SAXException, IOException, ObjectValidatorException,
	    ParserConfigurationException {
	ObjectSchemaValidator validator;
	JAXBContext jc = JAXBContext.newInstance(DIDAuthenticate.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();

	InputStream dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "DIDAuthenticate.xml");
	DIDAuthenticate didAuth = (DIDAuthenticate) unmarshaller.unmarshal(dataStream);
	validator = MarshallerSchemaValidator.load(didAuth.getClass(), "ISO24727-Protocols.xsd");
	Assert.assertEquals(validator.validateObject(didAuth), true);

	dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "DIDAuthenticate.xml");
	DIDAuthenticate didAuth2 = (DIDAuthenticate) unmarshaller.unmarshal(dataStream);
	DIDAuthenticationDataType authData = didAuth2.getAuthenticationProtocolData();
	DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFac.newDocumentBuilder();
	Document d = docBuilder.newDocument();
	Element sigElem = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Signature");
	sigElem.setTextContent("1254786930AAD4A8");
	authData.getAny().add(sigElem);
	didAuth2.setAuthenticationProtocolData(authData);
	validator = MarshallerSchemaValidator.load(didAuth2.getClass(), "ISO24727-Protocols.xsd");
	Assert.assertEquals(validator.validateObject(didAuth2), false);
    }

}
