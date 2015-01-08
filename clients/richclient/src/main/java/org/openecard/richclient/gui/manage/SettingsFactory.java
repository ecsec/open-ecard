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
 * Factory implementation which provides a various Setting objects.
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

    public static Settings getInstance(Properties props) {
	return new NonSavingProperties(props);
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
     * The class extends the Settings class wrapping an AddonProperties object.
     *
     * @author Hans-Martin Haase
     */
    public static class AddonPropertiesWrapper implements Settings {

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
     * The class extends the NonSavingProperties class and wraps OpenecardProperties.
     *
     * @author Hans-Martin Haase
     */
    public static class OpenecardPropertiesWrapper extends NonSavingProperties {

	private OpenecardPropertiesWrapper() {
	    super(OpenecardProperties.properties());
	}

	@Override
	public void store() throws IOException {
	    OpenecardProperties.writeChanges(props);
	}
    }

    /**
     * This class wraps a properties object but is not able to save it.
     *
     * @author Tobias Wich
     */
    private static class NonSavingProperties implements Settings {

	protected final Properties props;

	public NonSavingProperties(Properties props) {
	    this.props = props;
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
