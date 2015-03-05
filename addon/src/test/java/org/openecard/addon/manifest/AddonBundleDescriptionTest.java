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
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;


/**
 * Test if an AddonSpecification is marshalled and unmarshalled properly.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AddonBundleDescriptionTest {

    private static final String TEST_DESCRIPTION = "TestAddonBundleDescription.xml";
    private static final String OPENECARD_LOGO = "openecard_logo.png";
    private static final String LICENSE_TEXT_EN = "This is a dummy license text.";

    private AddonSpecification addonBundleDescription;

    @BeforeSuite
    public void setup() throws IOException, WSMarshallerException, SAXException {
	// read the test xml
	InputStream descriptionStream = FileUtils.resolveResourceAsStream(AddonBundleDescriptionTest.class, TEST_DESCRIPTION);
	String s = FileUtils.toString(descriptionStream);

	// unmarshal it and check fields of POJO
	WSMarshaller marshaller = WSMarshallerFactory.createInstance();
	marshaller.removeAllTypeClasses();
	marshaller.addXmlTypeClass(AddonSpecification.class);
	Object o = marshaller.unmarshal(marshaller.str2doc(s));
	addonBundleDescription = (AddonSpecification) o;
    }

    /**
     * This test unmarshals the test xml file, checks some fields of the generated POJO.
     */
    @Test(enabled=true)
    public void testBaseInformation() {

	// check base add-on information
	assertEquals(addonBundleDescription.getId(), "123");
	assertEquals(addonBundleDescription.getVersion(), "1.0");
	assertEquals(addonBundleDescription.getLicense(), "License");
	assertEquals(addonBundleDescription.getLicenseText("EN"), LICENSE_TEXT_EN);
	assertEquals(addonBundleDescription.getLocalizedName("EN"), "Test-Addon");
	assertEquals(addonBundleDescription.getLocalizedDescription("DE"), "Testbeschreibung");
	assertEquals(addonBundleDescription.getAbout("DE"), "About");
	assertEquals(addonBundleDescription.getLogo(), OPENECARD_LOGO);
	assertEquals(addonBundleDescription.getLocalizedName().size(), 2);





//	assertEquals(addonBundleDescription.getBindingActions().get(0).getResourceName(), "test-Client");
//	byte[] actualLogo = addonBundleDescription.getLogoBytes();
//	InputStream logoStream = FileUtils.resolveResourceAsStream(AddonBundleDescriptionTest.class, OPENECARD_LOGO);
//	byte[] expectedLogo = FileUtils.toByteArray(logoStream);
//	assertEquals(actualLogo, expectedLogo);
//	assertEquals(addonBundleDescription.getConfigDescription().getEntries().size(), 2);
//	assertEquals(addonBundleDescription.getConfigDescription().getEntries().get(0).getKey(), "Testkey");
    }

    @Test
    public void testBaseConfiguration() {
	for (ConfigurationEntry entry : addonBundleDescription.getConfigDescription().getEntries()) {
	    if (entry instanceof FileEntry) {
		FileEntry fEntry = (FileEntry) entry;
		assertEquals(fEntry.getFileType(), "pem");
		assertEquals(fEntry.isRequiredBeforeAction(), true);
		assertEquals(fEntry.getLocalizedName("EN"), "Certificate file");
		assertEquals(fEntry.getLocalizedDescription("EN"), "Enter the path to the certificate in pem format.");
		assertEquals(fEntry.getKey(), "Blub");
	    } else if (entry instanceof EnumEntry) {
		EnumEntry eEntry = (EnumEntry) entry;
		assertEquals(eEntry.getKey(), "Testkey");
		assertEquals(eEntry.getValues().get(0), "foo");
		assertEquals(eEntry.getValues().get(1), "bar");
		assertEquals(eEntry.getValues().size(), 2);
	    } else if (entry instanceof ScalarEntry) {
		ScalarEntry sEntry = (ScalarEntry) entry;
		assertEquals(sEntry.getKey(), "ScalarKey");
		assertEquals(sEntry.getType(), "BIGINTEGER");
	    }
	}
    }

    @Test
    public void testBindingAction() {
	AppPluginSpecification appPluginSpec = addonBundleDescription.getBindingActions().get(0);
	assertEquals(appPluginSpec.getClassName(), "de.test.class");
	assertEquals(appPluginSpec.getResourceName(), "test-Client");
	assertTrue(appPluginSpec.isLoadOnStartup());
	assertEquals(appPluginSpec.getParameters().get(0).getName(), "Test parameter");
	assertEquals(appPluginSpec.getParameters().get(0).getValue(), "Test value");
	assertEquals(appPluginSpec.getParameters().get(1).getName(), "Test parameter 2");
	assertEquals(appPluginSpec.getParameters().get(1).getValue(), "Test Value2");
	assertEquals(appPluginSpec.getBody().getMimeType(), "application/ogg");
	assertEquals(appPluginSpec.getBody().getNode(), "<html><body>test</body></html>");
	assertEquals(appPluginSpec.getAttachments().size(), 2);
	assertEquals(appPluginSpec.getAttachments().get(0).getMimeType().get(0), "application/pdf");
	assertEquals(appPluginSpec.getAttachments().get(0).getName(), "Documentation");
	assertEquals(appPluginSpec.getAttachments().get(1).getMimeType().get(0), "text/plain");
	assertEquals(appPluginSpec.getAttachments().get(1).getName(), "plaintext");
    }

    @Test
    public void testApplicationAction() {
	AppExtensionSpecification appExtSpec = addonBundleDescription.getApplicationActions().get(0);
	assertEquals(appExtSpec.getClassName(), "de.test.class");
	assertFalse(appExtSpec.isLoadOnStartup());
	assertEquals(appExtSpec.getId(), "123");
    }

    @Test
    public void testIFDAction() {
	ProtocolPluginSpecification protPlugSpec = addonBundleDescription.getIfdActions().get(0);
	assertEquals(protPlugSpec.getClassName(), "de.test.class");
	assertEquals(protPlugSpec.getUri(), "http://www.test.de");
	assertFalse(protPlugSpec.isLoadOnStartup());
    }

    @Test
    public void testSALAction() {
	ProtocolPluginSpecification protPlugSpec = addonBundleDescription.getSalActions().get(0);
	assertEquals(protPlugSpec.getClassName(), "de.test.class");
	assertEquals(protPlugSpec.getUri(), "http://www.test.de");
	assertFalse(protPlugSpec.isLoadOnStartup());
    }

}
