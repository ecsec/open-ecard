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

import org.openecard.common.util.SchemaValidator;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.openecard.common.util.FileUtils;
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
    public void validatorTest1() throws JAXBException, SAXException, IOException, ParserConfigurationException {
	SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	JAXBContext jc = JAXBContext.newInstance(DIDAuthenticate.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	URL schemaURL = FileUtils.resolveResourceAsURL(SchemaValidator.class, "ISO24727-Protocols.xsd");
	Schema schema = sf.newSchema(schemaURL);
	unmarshaller.setSchema(schema);

	DIDAuthenticate didAuth = (DIDAuthenticate) unmarshaller.unmarshal(FileUtils.resolveResourceAsStream(this.getClass(), "DIDAuthenticate.xml"));
	Assert.assertEquals(SchemaValidator.validateObject(didAuth, "ISO24727-Protocols.xsd"), true);

	DIDAuthenticate didAuth2 = (DIDAuthenticate) unmarshaller.unmarshal(FileUtils.resolveResourceAsStream(this.getClass(), "DIDAuthenticate.xml"));
	DIDAuthenticationDataType authData = didAuth2.getAuthenticationProtocolData();
	DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFac.newDocumentBuilder();
	Document d = docBuilder.newDocument();
	Element sigElem = d.createElement("ns4:Signature");
	sigElem.setTextContent("1254786930AAD4A8");
	authData.getAny().add(sigElem);
	didAuth2.setAuthenticationProtocolData(authData);
	Assert.assertEquals(SchemaValidator.validateObject(didAuth2, "ISO24727-Protocols.xsd"), false);
    }

}
