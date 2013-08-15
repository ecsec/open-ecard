/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.addon.manifest;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.TransformerException;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import static org.testng.Assert.assertEquals;


/**
 * Test if an AddonSpecification is marshalled and unmarshalled properly.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AddonBundleDescriptionTest {

    private static final String TEST_DESCRIPTION = "TestAddonBundleDescription.xml";
    private static final String OPENECARD_LOGO = "openecard_logo.png";

    /**
     * This test unmarshals the test xml file, checks some fields of the generated POJO and afterwards marshals it back
     * to xml. Finally the resulting xml and the original xml are compared.
     * 
     * @throws IOException when a {@link FileUtils} operation fails
     * @throws SAXException 
     * @throws WSMarshallerException 
     * @throws TransformerException 
     */
    @Test(enabled=true)
    public void testloadFromManifest() throws IOException, WSMarshallerException, SAXException, TransformerException {
	// read the test xml
	InputStream descriptionStream = FileUtils.resolveResourceAsStream(AddonBundleDescriptionTest.class, TEST_DESCRIPTION);
	String s = FileUtils.toString(descriptionStream);

	// unmarshal it and check fields of POJO
	WSMarshaller marshaller = WSMarshallerFactory.createInstance();
	marshaller.addXmlTypeClass(AddonSpecification.class);
	Object o = marshaller.unmarshal(marshaller.str2doc(s));
	AddonSpecification addonBundleDescription = (AddonSpecification) o;
	assertEquals(addonBundleDescription.getBindingActions().get(0).getResourceName(), "test-Client");
	byte[] actualLogo = addonBundleDescription.getLogoBytes();
	InputStream logoStream = FileUtils.resolveResourceAsStream(AddonBundleDescriptionTest.class, OPENECARD_LOGO);
	byte[] expectedLogo = FileUtils.toByteArray(logoStream);
	assertEquals(actualLogo, expectedLogo);
	assertEquals(addonBundleDescription.getConfigDescription().getEntries().size(), 1);
	assertEquals(addonBundleDescription.getConfigDescription().getEntries().get(0).getKey(), "Testkey");
	// marshal it back and compare the xml's
	Document d = marshaller.marshal(addonBundleDescription);
	// remove xml namespaces and whitespaces
	String expected = s.substring(s.indexOf("<ID")).replaceAll("\\s", "");
	String actual = marshaller.doc2str(d);
	System.out.println(actual);
	actual = actual.substring(actual.indexOf("<ID")).replaceAll("\\s", "");
	assertEquals(actual, expected);
    }

}
