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

import java.io.File;
import java.io.IOException;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AddonPropertiesTest {

    private static final Logger logger = LoggerFactory.getLogger(AddonPropertiesTest.class);

    private static final String NAME_KEY = "name";
    private static final String NAME = "test-addon";
    private static final String VERSION_KEY = "version";
    private static final String VERSION = "1.0.0";
    private static final String URL_KEY = "url";
    private static final String URL = "http://www.test-addon.com";

    private boolean addonsExists;
    private File path;
    private final AddonSpecification addonSpec = new AddonSpecification();

    /**
     * Initialize a Context object just for testing purpose.
     * @throws java.io.IOException
     */
    @BeforeTest
    public void init() throws IOException {
	addonSpec.setId("test-addon");
	path = new File(FileUtils.getAddonsConfDir(), addonSpec.getId());
	if (! path.exists()) {
	    path.mkdirs();
	    addonsExists = false;
	} else {
	    addonsExists = true;
	}
    }

    /**
     * Test the set and get method.
     */
    @Test
    public void testSetAndGet() {
	AddonProperties props = new AddonProperties(addonSpec);
	props.setProperty(NAME_KEY, NAME);
	props.setProperty(VERSION_KEY, VERSION);
	props.setProperty(URL_KEY, URL);

	Assert.assertEquals(props.getProperty(NAME_KEY), NAME);
	Assert.assertEquals(props.getProperty(VERSION_KEY), VERSION);
	Assert.assertEquals(props.getProperty(URL_KEY), URL);
    }

    /**
     * Test the save and load method.
     */
    @Test
    public void testLoadAndSave() {
	AddonProperties props = new AddonProperties(addonSpec);
	props.setProperty(NAME_KEY, NAME);
	props.setProperty(VERSION_KEY, VERSION);
	props.setProperty(URL_KEY, URL);

	try {
	    props.saveProperties();
	} catch (AddonPropertiesException ex) {
	    logger.error("Properties not stored.", ex);
	}

	AddonProperties props2 = new AddonProperties(addonSpec);
	try {
	    props2.loadProperties();
	} catch (AddonPropertiesException ex) {
	    logger.error("Properties not loaded", ex);
	}

	Assert.assertEquals(props2.getProperty(NAME_KEY), NAME);
	Assert.assertEquals(props2.getProperty(VERSION_KEY), VERSION);
	Assert.assertEquals(props2.getProperty(URL_KEY), URL);
    }

    /**
     * Remove created folders and files
     * @throws java.io.IOException
     */
    @AfterSuite
    public void clear() throws IOException {
	if (! addonsExists) {
	    new File(FileUtils.getAddonsConfDir().getPath() + "/test-addon.conf").delete();
	    path.delete();
	} else {
	    new File(FileUtils.getAddonsConfDir().getPath() + "/test-addon.conf").delete();
	}
    }
}
