/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * Basic Properties class which overrides default values from System properties on its creation.
 * When looking up a key, it is first searched in the system properties, then in the properties structure instantiated
 * in the constructor.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OverridingProperties {

    private final Properties properties;

    /**
     * Loads properties from named resource.
     * The resource must be located in the classpath.
     *
     * @param fName Name of the properties file.
     * @throws IOException If loading of the resource failed.
     */
    public OverridingProperties(String fName) throws IOException {
	InputStream in = this.getClass().getResourceAsStream("/" + fName);
	if (in == null) {
	    in = this.getClass().getResourceAsStream(fName);
	}
	properties = new Properties();
	properties.load(in);
	init();
    }
    /**
     * Load properties from InputStream.
     *
     * @param stream Stream with Java properties format.
     * @throws IOException If loading of the resource failed.
     */
    public OverridingProperties(InputStream stream) throws IOException {
	this(stream, null);
	init();
    }
    /**
     * Load properties from property instance.
     *
     * @param props Properties instance.
     */
    public OverridingProperties(Properties props) {
	properties = new Properties(props);
	init();
    }
    /**
     * Load properties from InputStreams.
     * The second properties stream takes precedence over the first one. This intended as a persistent default
     * mechanism. The first stream may point to a bundled properties file, while the second can point to a file in the
     * applications config directory, so that the user can alter the config values.
     *
     * @param bundledConf Stream to base properties.
     * @param homeConf Stream to properties overriding the base properties. May be null.
     * @throws IOException If loading of the base resource failed.
     */
    public OverridingProperties(InputStream bundledConf, InputStream homeConf) throws IOException {
	Properties baseProps = new Properties();
	baseProps.load(bundledConf);

	try {
	    if (homeConf != null) {
		Properties homeProps = new Properties(baseProps);
		homeProps.load(homeConf);
		baseProps = homeProps;
	    }
	} catch (IOException ex) {
	}

	properties = baseProps;
	init();
    }

    private void init() {
	overrideFromSystem();
    }

    private void overrideFromSystem() {
	for (Object nextKey : properties.keySet()) {
	    if (nextKey instanceof String) {
		String next = (String) nextKey;
		if (System.getProperties().containsKey(next)) {
		    properties.setProperty(next, System.getProperties().getProperty(next));
		}
	    }
	}
    }


    /**
     * Gets URL to resource reachable by the classloader of this class.
     * This convenience method tries the path with / prepended and without.
     *
     * @param fname Path to the file to load.
     * @return URL instance pointing to the resource. A stream can be opened afterwards.
     */
    public URL getDependentResource(String fname) {
	URL url = getClass().getResource("/" + fname);
	if (url == null) {
	    url = getClass().getResource(fname);
	}
	return url;
    }

    /**
     * Gets the value for the given property key.
     *
     * @see Properties#getProperty(java.lang.String)
     * @param key
     * @return The value, or null if none is found.
     */
    public final String getProperty(String key) {
	return properties.getProperty(key);
    }

    /**
     * Sets the value for a property key.
     *
     * @see Properties#setProperty(java.lang.String, java.lang.String)
     * @param key The key to be placed into the property list.
     * @param value The value corresponding to key.
     * @return The previous value in the properties structure, or null if none was set.
     */
    public final Object setProperty(String key, String value) {
	return properties.setProperty(key, value);
    }

    /**
     * Gets a copy of all defined properties.
     *
     * @return Copy of the properties.
     */
    public final Properties properties() {
	return (Properties) properties.clone();
    }

}
