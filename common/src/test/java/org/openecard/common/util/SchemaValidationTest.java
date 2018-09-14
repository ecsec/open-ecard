/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.interfaces.DocumentValidatorException;
import org.openecard.ws.marshal.WSMarshallerException;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.openecard.common.interfaces.DocumentSchemaValidator;


/**
 *
 * @author Hans-Martin Haase
 */
public class SchemaValidationTest {

    private final DocumentBuilder builder;
    private final DocumentSchemaValidator validator;

    public SchemaValidationTest() throws ParserConfigurationException, IOException, SAXException {
	// instantiate w3 stuff
	DocumentBuilderFactory tmpW3Factory = DocumentBuilderFactory.newInstance();
	tmpW3Factory.setNamespaceAware(true);
	tmpW3Factory.setIgnoringComments(true);
	tmpW3Factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

	builder = tmpW3Factory.newDocumentBuilder();

	validator = JAXPSchemaValidator.load("ISO24727-Protocols.xsd");
    }


    @Test
    public void testDIDAuthEac1InputOk() throws JAXBException, WSMarshallerException, SAXException, IOException,
	    DocumentValidatorException, ParserConfigurationException {
	InputStream dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "DID_EAC1Input.xml");
	Document didAuth = builder.parse(dataStream);
	validator.validate(didAuth);
    }

    @Test
    public void testDIDAuthEac2InputOk() throws JAXBException, WSMarshallerException, SAXException, IOException,
	    DocumentValidatorException, ParserConfigurationException {
	InputStream dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "DIDAuthenticate.xml");
	Document didAuth = builder.parse(dataStream);
	validator.validate(didAuth);
    }

    @Test(expectedExceptions = DocumentValidatorException.class)
    public void testDIDAuthNok() throws IOException, JAXBException, ParserConfigurationException,
	    DocumentValidatorException {
	JAXBContext jc = JAXBContext.newInstance(DIDAuthenticate.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	InputStream dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "DIDAuthenticate.xml");
	DIDAuthenticate didAuth2 = (DIDAuthenticate) unmarshaller.unmarshal(dataStream);
	DIDAuthenticationDataType authData = didAuth2.getAuthenticationProtocolData();
	Document d = builder.newDocument();
	Element sigElem = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Signature");
	sigElem.setTextContent("1254786930AAD4A8");
	authData.getAny().add(sigElem);
	didAuth2.setAuthenticationProtocolData(authData);
	Document target = builder.newDocument();
	jc.createMarshaller().marshal(didAuth2, target);
	validator.validate(target);
    }

    @Test(expectedExceptions = DocumentValidatorException.class)
    public void testDIDAuthEac1InputNoCert() throws JAXBException, WSMarshallerException, SAXException, IOException,
	    DocumentValidatorException, ParserConfigurationException {
	InputStream dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "DIDAuthenticate_EACInput1_nocert.xml");
	Document didAuth = builder.parse(dataStream);
	validator.validate(didAuth);
    }

    @Test
    public void testInitFrameworkOk() throws JAXBException, WSMarshallerException, SAXException, IOException,
	    DocumentValidatorException, ParserConfigurationException {
	InputStream dataStream = FileUtils.resolveResourceAsStream(SchemaValidationTest.class, "InitializeFramework.xml");
	Document initFrame = builder.parse(dataStream);
	validator.validate(initFrame);
    }

}
