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
 */
package org.openecard.addon.manifest

import org.openecard.common.util.FileUtils
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.testng.Assert
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test
import org.xml.sax.SAXException
import java.io.IOException

/**
 * Test if an AddonSpecification is marshalled and unmarshalled properly.
 *
 * @author Dirk Petrautzki
 */
class AddonBundleDescriptionTest {
	private var addonBundleDescription: AddonSpecification? = null

	@BeforeSuite
	@Throws(IOException::class, WSMarshallerException::class, SAXException::class)
	fun setup() {
		// read the test xml
		val descriptionStream = resolveResourceAsStream(AddonBundleDescriptionTest::class.java, TEST_DESCRIPTION)
		val s = FileUtils.toString(descriptionStream!!)

		// unmarshal it and check fields of POJO
		val marshaller = createInstance()
		marshaller.removeAllTypeClasses()
		marshaller.addXmlTypeClass(AddonSpecification::class.java)
		val o = marshaller.unmarshal(marshaller.str2doc(s))
		addonBundleDescription = o as AddonSpecification
	}

	/**
	 * This test unmarshals the test xml file, checks some fields of the generated POJO.
	 */
	@Test(enabled = true)
	fun testBaseInformation() {
		// check base add-on information

		Assert.assertEquals(addonBundleDescription!!.getId(), "123")
		Assert.assertEquals(addonBundleDescription!!.getVersion(), "1.0")
		Assert.assertEquals(addonBundleDescription!!.license, "License")
		Assert.assertEquals(addonBundleDescription!!.getLicenseText("EN"), LICENSE_TEXT_EN)
		Assert.assertEquals(addonBundleDescription!!.getLocalizedName("EN"), "Test-Addon")
		Assert.assertEquals(addonBundleDescription!!.getLocalizedDescription("DE"), "Testbeschreibung")
		Assert.assertEquals(addonBundleDescription!!.getAbout("DE"), "About")
		Assert.assertEquals(addonBundleDescription!!.getLogo(), OPENECARD_LOGO)
		Assert.assertEquals(addonBundleDescription!!.localizedName.size, 2)

		// 	assertEquals(addonBundleDescription.getBindingActions().get(0).getResourceName(), "test-Client");
// 	byte[] actualLogo = addonBundleDescription.getLogoBytes();
// 	InputStream logoStream = FileUtils.resolveResourceAsStream(AddonBundleDescriptionTest.class, OPENECARD_LOGO);
// 	byte[] expectedLogo = FileUtils.toByteArray(logoStream);
// 	assertEquals(actualLogo, expectedLogo);
// 	assertEquals(addonBundleDescription.getConfigDescription().getEntries().size(), 2);
// 	assertEquals(addonBundleDescription.getConfigDescription().getEntries().get(0).getKey(), "Testkey");
	}

	@Test
	fun testBaseConfiguration() {
		for (entry in addonBundleDescription!!.configDescription!!.entries) {
			if (entry is FileEntry) {
				val fEntry = entry
				Assert.assertEquals(fEntry.fileType, "pem")
				Assert.assertEquals(fEntry.isRequiredBeforeAction, true as Boolean?)
				Assert.assertEquals(fEntry.getLocalizedName("EN"), "Certificate file")
				Assert.assertEquals(
					fEntry.getLocalizedDescription("EN"),
					"Enter the path to the certificate in pem format.",
				)
				Assert.assertEquals(fEntry.key, "Blub")
			} else if (entry is EnumEntry) {
				val eEntry = entry
				Assert.assertEquals(eEntry.key, "Testkey")
				Assert.assertEquals(eEntry.values.get(0), "foo")
				Assert.assertEquals(eEntry.values.get(1), "bar")
				Assert.assertEquals(eEntry.values.size, 2)
			} else if (entry is ScalarEntry) {
				val sEntry = entry
				Assert.assertEquals(sEntry.key, "ScalarKey")
				Assert.assertEquals(sEntry.getType(), "BIGINTEGER")
			}
		}
	}

	@Test
	fun testBindingAction() {
		val appPluginSpec = addonBundleDescription!!.bindingActions.get(0)
		Assert.assertEquals(appPluginSpec.className, "de.test.class")
		Assert.assertEquals(appPluginSpec.resourceName, "test-Client")
		Assert.assertTrue(appPluginSpec.isLoadOnStartup!!)
		Assert.assertEquals(appPluginSpec.parameters.get(0)!!.name, "Test parameter")
		Assert.assertEquals(appPluginSpec.parameters.get(0)!!.value, "Test value")
		Assert.assertEquals(appPluginSpec.parameters.get(1)!!.name, "Test parameter 2")
		Assert.assertEquals(appPluginSpec.parameters.get(1)!!.value, "Test Value2")
		Assert.assertEquals(appPluginSpec.body!!.mimeType, "application/ogg")
		Assert.assertEquals(appPluginSpec.body!!.node, "<html><body>test</body></html>")
		Assert.assertEquals(appPluginSpec.attachments.size, 2)
		Assert.assertEquals(
			appPluginSpec.attachments
				.get(0)!!
				.mimeType
				.get(0),
			"application/pdf",
		)
		Assert.assertEquals(appPluginSpec.attachments.get(0)!!.name, "Documentation")
		Assert.assertEquals(
			appPluginSpec.attachments
				.get(1)!!
				.mimeType
				.get(0),
			"text/plain",
		)
		Assert.assertEquals(appPluginSpec.attachments.get(1)!!.name, "plaintext")
	}

	@Test
	fun testApplicationAction() {
		val appExtSpec = addonBundleDescription!!.applicationActions.get(0)
		Assert.assertEquals(appExtSpec.className, "de.test.class")
		Assert.assertFalse(appExtSpec.isLoadOnStartup!!)
		Assert.assertEquals(appExtSpec.id, "123")
	}

	@Test
	fun testIFDAction() {
		val protPlugSpec = addonBundleDescription!!.ifdActions.get(0)
		Assert.assertEquals(protPlugSpec.className, "de.test.class")
		Assert.assertEquals(protPlugSpec.uri, "http://www.test.de")
		Assert.assertFalse(protPlugSpec.isLoadOnStartup!!)
	}

	@Test
	fun testSALAction() {
		val protPlugSpec = addonBundleDescription!!.salActions.get(0)
		Assert.assertEquals(protPlugSpec.className, "de.test.class")
		Assert.assertEquals(protPlugSpec.uri, "http://www.test.de")
		Assert.assertFalse(protPlugSpec.isLoadOnStartup!!)
	}

	companion object {
		private const val TEST_DESCRIPTION = "TestAddonBundleDescription.xml"
		private const val OPENECARD_LOGO = "openecard_logo.png"
		private const val LICENSE_TEXT_EN = "This is a dummy license text."
	}
}
