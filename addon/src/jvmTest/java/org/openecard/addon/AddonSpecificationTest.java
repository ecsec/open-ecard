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

import org.openecard.addon.manifest.AddonSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Hans-Martin Haase
 */
public class AddonSpecificationTest {

    @Test
    public void testCompareToAllEquals() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.0");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setId("ABC");
	spec2.setVersion("1.0.0");

	Assert.assertEquals(spec1.compareTo(spec2), 0);
    }

    @Test
    public void testCompareToVersionEquals() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABD");
	spec1.setVersion("1.0.0");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setId("ABC");
	spec2.setVersion("1.0.0");

	Assert.assertEquals(spec1.compareTo(spec2), 1);
    }

    @Test
    public void testCompareToIdEquals() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.2");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setId("ABC");
	spec2.setVersion("1.0.5");

	Assert.assertEquals(spec1.compareTo(spec2), -1);
    }

    @Test
    public void testCompareToAllDifferent() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.0");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setId("ZBD");
	spec2.setVersion("1.0.8");
	AddonSpecification spec3 = new AddonSpecification();
	spec3.setId("ABE");
	spec3.setVersion("1.0.25");

	Assert.assertEquals(spec1.compareTo(spec3), -1);
	Assert.assertEquals(spec1.compareTo(spec2), -1);
	Assert.assertEquals(spec2.compareTo(spec3), 1);
    }

    @Test
    public void testHashCode() {
	int versionHash = "1.0.0".hashCode();
	int idHash = "ABC".hashCode();
	int res = versionHash + idHash;
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.0");

	Assert.assertEquals(spec1.hashCode(), res);
    }

    @Test
    public void testEqualsDifferentObjects() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.0");

	StringBuilder strBuilder = new StringBuilder();
	Assert.assertFalse(spec1.equals(strBuilder));
    }

    @Test
    public void testEqualsSameObjectType() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.0");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setId("ABC");
	spec2.setVersion("1.0.0");

	Assert.assertTrue(spec1.equals(spec2));
    }

    @Test
    public void testEqualsSameObjectTypeDiffAttributes() {
	AddonSpecification spec1 = new AddonSpecification();
	spec1.setId("ABC");
	spec1.setVersion("1.0.0");
	AddonSpecification spec2 = new AddonSpecification();
	spec2.setId("ABk");
	spec2.setVersion("1.0.5");

	Assert.assertFalse(spec1.equals(spec2));
    }

}
