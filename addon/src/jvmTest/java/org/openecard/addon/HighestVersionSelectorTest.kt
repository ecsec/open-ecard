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
 */
package org.openecard.addon

import org.openecard.addon.manifest.AddonSpecification
import org.testng.Assert
import org.testng.annotations.Test

/**
 * The method tests the HighestVersionSelector class.
 *
 * @author Hans-Martin Haase
 */
class HighestVersionSelectorTest {
	/**
	 * Test semanticVersioning without additional string for pre release or something else.
	 */
	@Test
	fun testSemanticVersioning() {
		val spec1 = AddonSpecification()
		spec1.setVersion("1.0.0")
		spec1.setId("Test")
		val spec2 = AddonSpecification()
		spec2.setVersion("2.5.0")
		spec2.setId("Test")
		val spec3 = AddonSpecification()
		spec3.setVersion("0.2.1")
		spec3.setId("Test")

		val set = HashSet<AddonSpecification>()
		set.add(spec3)
		set.add(spec1)
		set.add(spec2)

		val hvs = HighestVersionSelector()
		val resSpec = hvs.select(set)
		Assert.assertEquals(resSpec.getVersion(), spec2.getVersion())
	}

	/**
	 * Test semanticVersioning with additional string for pre release or something else.
	 */
	@Test
	fun testSemanticVersioningWithAdditionalLabel() {
		val spec1 = AddonSpecification()
		spec1.setVersion("1.0.0-alpha")
		spec1.setId("Test")
		val spec2 = AddonSpecification()
		spec2.setVersion("1.0.0-alpha.5")
		spec2.setId("Test")
		val spec3 = AddonSpecification()
		spec3.setVersion("1.0.0")
		spec3.setId("Test")

		val set1 = HashSet<AddonSpecification>()
		set1.add(spec3)
		set1.add(spec1)
		set1.add(spec2)

		val hvs = HighestVersionSelector()
		val resSpec = hvs.select(set1)
		Assert.assertEquals(resSpec.getVersion(), spec3.getVersion())

		val spec4 = AddonSpecification()
		spec4.setVersion("1.0.0-beta")
		spec4.setId("Test")
		val set2 = HashSet<AddonSpecification>()
		set2.add(spec4)
		set2.add(spec2)
		set2.add(spec1)

		val resSpec2 = hvs.select(set2)
		Assert.assertEquals(resSpec2.getVersion(), spec4.getVersion())
	}

	/**
	 * Test pure alphabetical versions.
	 */
	@Test
	fun testLexicalVersioning() {
		val spec1 = AddonSpecification()
		spec1.setVersion("moonshine release")
		spec1.setId("Test")
		val spec2 = AddonSpecification()
		spec2.setVersion("alpha")
		spec2.setId("Test")
		val spec3 = AddonSpecification()
		spec3.setVersion("zeta")
		spec3.setId("Test")

		val set1 = HashSet<AddonSpecification>()
		set1.add(spec3)
		set1.add(spec1)
		set1.add(spec2)

		val hvs = HighestVersionSelector()
		val resSpec = hvs.select(set1)
		Assert.assertEquals(resSpec.getVersion(), spec3.getVersion())
	}

	/**
	 * Test semanticVersioninig together with lexical versioning.
	 */
	@Test
	fun testLexicalAndSemanticVersioning() {
		val spec1 = AddonSpecification()
		spec1.setVersion("moonshine release")
		spec1.setId("Test")
		val spec2 = AddonSpecification()
		spec2.setVersion("1.0.2")
		spec2.setId("Test")
		val spec3 = AddonSpecification()
		spec3.setVersion("1.0.2-pre1")
		spec3.setId("Test")

		val set1 = HashSet<AddonSpecification>()
		set1.add(spec3)
		set1.add(spec1)
		set1.add(spec2)

		val hvs = HighestVersionSelector()
		val resSpec = hvs.select(set1)
		Assert.assertEquals(resSpec.getVersion(), spec2.getVersion())
	}
}
