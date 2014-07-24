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

package org.openecard.addon;

import java.util.HashSet;
import org.openecard.addon.manifest.AddonSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The method tests the HighestVersionSelector class.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class HighestVersionSelectorTest {

    /**
     * Test semanticVersioning without additional string for pre release or something else.
     */
    @Test
    public void testSemanticVersioning() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setVersion("1.0.0");
	spec1.setId("Test");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setVersion("2.5.0");
	spec2.setId("Test");
	AddonSpecification spec3 = new AddonSpecification();
	spec3.setVersion("0.2.1");
	spec3.setId("Test");

	HashSet<AddonSpecification> set = new HashSet<>();
	set.add(spec3);
	set.add(spec1);
	set.add(spec2);

	HighestVersionSelector hvs = new HighestVersionSelector();
	AddonSpecification resSpec = hvs.select(set);
	Assert.assertEquals(resSpec.getVersion(), spec2.getVersion());
    }

    /**
     * Test semanticVersioning with additional string for pre release or something else.
     */
    @Test
    public void testSemanticVersioningWithAdditionalLabel() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setVersion("1.0.0-alpha");
	spec1.setId("Test");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setVersion("1.0.0-alpha.5");
	spec2.setId("Test");
	AddonSpecification spec3 = new AddonSpecification();
	spec3.setVersion("1.0.0");
	spec3.setId("Test");

	HashSet<AddonSpecification> set1 = new HashSet<>();
	set1.add(spec3);
	set1.add(spec1);
	set1.add(spec2);

	HighestVersionSelector hvs = new HighestVersionSelector();
	AddonSpecification resSpec = hvs.select(set1);
	Assert.assertEquals(resSpec.getVersion(), spec3.getVersion());

	AddonSpecification spec4 = new AddonSpecification();
	spec4.setVersion("1.0.0-beta");
	spec4.setId("Test");
	HashSet<AddonSpecification> set2 = new HashSet<>();
	set2.add(spec4);
	set2.add(spec2);
	set2.add(spec1);

	AddonSpecification resSpec2 = hvs.select(set2);
	Assert.assertEquals(resSpec2.getVersion(), spec4.getVersion());
    }

    /**
     * Test pure alphabetical versions.
     */
    @Test
    public void testLexicalVersioning() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setVersion("moonshine release");
	spec1.setId("Test");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setVersion("alpha");
	spec2.setId("Test");
	AddonSpecification spec3 = new AddonSpecification();
	spec3.setVersion("zeta");
	spec3.setId("Test");

	HashSet<AddonSpecification> set1 = new HashSet<>();
	set1.add(spec3);
	set1.add(spec1);
	set1.add(spec2);

	HighestVersionSelector hvs = new HighestVersionSelector();
	AddonSpecification resSpec = hvs.select(set1);
	Assert.assertEquals(resSpec.getVersion(), spec3.getVersion());
    }

    /**
     * Test semanticVersioninig together with lexical versioning.
     */
    @Test
    public void testLexicalAndSemanticVersioning() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setVersion("moonshine release");
	spec1.setId("Test");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setVersion("1.0.2");
	spec2.setId("Test");
	AddonSpecification spec3 = new AddonSpecification();
	spec3.setVersion("1.0.2-pre1");
	spec3.setId("Test");

	HashSet<AddonSpecification> set1 = new HashSet<>();
	set1.add(spec3);
	set1.add(spec1);
	set1.add(spec2);

	HighestVersionSelector hvs = new HighestVersionSelector();
	AddonSpecification resSpec = hvs.select(set1);
	Assert.assertEquals(resSpec.getVersion(), spec2.getVersion());
    }

}
