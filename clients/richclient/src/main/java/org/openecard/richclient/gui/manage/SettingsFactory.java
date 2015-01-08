/****************************************************************************
 * Copyright (C) 2014-2015 ecsec GmbH.
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

package org.openecard.richclient.gui.manage;

import java.io.IOException;
import java.util.Properties;
import org.openecard.addon.AddonProperties;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.common.OpenecardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory implementation which provides a Settings object which wraps a Properties or AddonProperties object.
 *
 * @author Hans-Martin Haase
 */
public class SettingsFactory {

    private static final Logger logger = LoggerFactory.getLogger(AddonPropertiesWrapper.class);

    /**
     * Get a Settings object from a fresh Properties object obtained from OpenecardProperties.
     *
     * @return A Settings object which wraps the {@code pops} object.
     */
    public static Settings getInstance() {
	return new OpenecardPropertiesWrapper();
    }

    /**
     * Get a Settings object from the given AddonProperties object.
     *
     * @param props The AddonProperties to wrap in the Settings object.
     * @return A Settings object which wraps the {@code props} object.
     */
    public static Settings getInstance(AddonProperties props) {
	return new AddonPropertiesWrapper(props);
    }


    /**
     * The class extends the Settings class and wraps an AddonProperties object.
     *
     * @author Hans-Martin Haase
     */
    public static class AddonPropertiesWrapper extends Settings {

	private final AddonProperties props;

	private AddonPropertiesWrapper(AddonProperties props) {
	    this.props = props;

	    try {
		props.loadProperties();
	    } catch (AddonPropertiesException ex) {
		logger.error("Failed to load AddonProperties.", ex);
	    }
	}

	@Override
	public void setProperty(String key, String value) {
	    props.setProperty(key, value);
	}

	@Override
	public String getProperty(String key) {
	    return props.getProperty(key);
	}

	@Override
	public void store() throws AddonPropertiesException{
	    props.saveProperties();
	}
    }

    /**
     * The class extends the Settings class and wraps a Properties object.
     *
     * @author Hans-Martin Haase
     */
    public static class OpenecardPropertiesWrapper extends Settings {

	private final Properties props;

	private OpenecardPropertiesWrapper() {
	    props = OpenecardProperties.properties();
	}

	@Override
	public void setProperty(String key, String value) {
	    props.setProperty(key, value);
	}

	@Override
	public String getProperty(String key) {
	    return props.getProperty(key);
	}

	@Override
	public void store() throws IOException {
	    OpenecardProperties.writeChanges(props);
	}
    }

}
