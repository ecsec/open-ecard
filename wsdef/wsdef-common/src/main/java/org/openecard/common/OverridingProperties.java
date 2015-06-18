/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic Properties class which overrides default values from System properties on its creation.
 * When looking up a key, it is first searched in the system properties, then in the properties structure instantiated
 * in the constructor.
 *
 * @author Tobias Wich
 */
public class OverridingProperties {

    private static final Logger logger = LoggerFactory.getLogger(OverridingProperties.class);

    private final Properties properties;

    /**
     * Loads properties from named resource.
     * The resource must be located in the classpath.
     *
     * @param fName Name of the properties file.
     * @throws IOException If loading of the resource failed.
     */
    public OverridingProperties(String fName) throws IOException {
	this(getfileStream(OverridingProperties.class, fName));
    }

    /**
     * Loads properties from named resource.
     * The resource must be located in the classpath.
     *
     * @param clazz Class used as reference to load the resource.
     * @param fName Name of the properties file.
     * @throws IOException If loading of the resource failed.
     */
    public OverridingProperties(Class clazz, String fName) throws IOException {
	this(getfileStream(clazz, fName));
    }

    /**
     * Load properties from InputStream.
     *
     * @param stream Stream with Java properties format.
     * @throws IOException If loading of the resource failed.
     */
    public OverridingProperties(InputStream stream) throws IOException {
	this(stream, null);
    }

    /**
     * Load properties from property instance.
     *
     * @param props Properties instance.
     * @throws IOException If the merge of the properties with the system defaults failed.
     */
    public OverridingProperties(Properties props) throws IOException {
	properties = mergeWithOverrides(props);
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
	bundledConf.close();

	try {
	    if (homeConf != null) {
		Properties homeProps = new Properties(baseProps);
		homeProps.load(homeConf);
		homeConf.close();
		baseProps = homeProps;
	    }
	} catch (IOException ex) {
	    logger.error("Failed to load given properties stream.", ex);
	}

	properties = mergeWithOverrides(baseProps);
    }


    private static InputStream getfileStream(Class clazz, String fName) {
	InputStream in = clazz.getResourceAsStream("/" + fName);
	if (in == null) {
	    in = clazz.getResourceAsStream(fName);
	}
	return in;
    }

    private static Properties mergeWithOverrides(Properties reference) throws IOException {
	Properties result = new Properties(reference);
	result.load(propsToStream(getOverrides(reference)));
	return result;
    }

    private static Reader propsToStream(Properties properties) throws IOException {
	StringWriter w = new StringWriter();
	properties.store(w, null);
	String propsStr = w.toString();
	return new StringReader(propsStr);
    }

    private static Properties getOverrides(Properties reference) {
	Properties overrides = new Properties();
	for (String nextKey : reference.stringPropertyNames()) {
	    if (System.getProperties().containsKey(nextKey)) {
		overrides.setProperty(nextKey, System.getProperties().getProperty(nextKey));
	    }
	}
	return overrides;
    }


    /**
     * Gets the value for the given property key.
     *
     * @see Properties#getProperty(java.lang.String)
     * @param key Key of the property.
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
     * The values in the copy are all handled as defaults.
     *
     * @return Copy of the properties.
     */
    public final Properties properties() {
	return new Properties(properties);
    }

}
