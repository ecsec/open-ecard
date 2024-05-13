/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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
package org.openecard.ws.common

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.*
import java.util.*

private val LOG = KotlinLogging.logger {}

/**
 * Basic Properties class which overrides default values from System properties on its creation.
 * When looking up a key, it is first searched in the system properties, then in the properties structure instantiated
 * in the constructor.
 *
 * @author Tobias Wich
 */
open class OverridingProperties {
    private val properties: Properties

    /**
     * Loads properties from named resource.
     * The resource must be located in the classpath.
     *
     * @param fName Name of the properties file.
     * @throws IOException If loading of the resource failed.
     */
    constructor(fName: String) : this(getfileStream(OverridingProperties::class.java, fName))

    /**
     * Loads properties from named resource.
     * The resource must be located in the classpath.
     *
     * @param clazz Class used as reference to load the resource.
     * @param fName Name of the properties file.
     * @throws IOException If loading of the resource failed.
     */
    constructor(clazz: Class<*>, fName: String) : this(getfileStream(clazz, fName))

    /**
     * Load properties from InputStream.
     *
     * @param stream Stream with Java properties format.
     * @throws IOException If loading of the resource failed.
     */
    constructor(stream: InputStream) : this(stream, null)

    /**
     * Load properties from property instance.
     *
     * @param props Properties instance.
     * @throws IOException If the merge of the properties with the system defaults failed.
     */
    constructor(props: Properties) {
        properties = mergeWithOverrides(props)
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
	@kotlin.jvm.Throws(IOException::class)
    constructor(bundledConf: InputStream, homeConf: InputStream?) {
        var baseProps = Properties()
        baseProps.load(bundledConf)
        bundledConf.close()

        try {
            if (homeConf != null) {
                val homeProps = Properties(baseProps)
                homeProps.load(homeConf)
                homeConf.close()
                baseProps = homeProps
            }
        } catch (ex: IOException) {
            LOG.error(ex) { "Failed to load given properties stream." }
        }

        properties = mergeWithOverrides(baseProps)
    }


    /**
     * Gets the value for the given property key.
     *
     * @see Properties.getProperty
     * @param key Key of the property.
     * @return The value, or null if none is found.
     */
    fun getProperty(key: String): String? {
        return properties.getProperty(key)
    }

    /**
     * Sets the value for a property key.
     *
     * @see Properties.setProperty
     * @param key The key to be placed into the property list.
     * @param value The value corresponding to key.
     * @return The previous value in the properties structure, or null if none was set.
     */
    fun setProperty(key: String, value: String): Any? {
        return properties.setProperty(key, value)
    }

    /**
     * Gets a copy of all defined properties.
     * The values in the copy are all handled as defaults.
     *
     * @return Copy of the properties.
     */
    fun properties(): Properties {
        return Properties(properties)
    }
}


@Throws(IOException::class)
private fun getfileStream(clazz: Class<*>, fName: String): InputStream {
	return clazz.getResourceAsStream("/$fName")
		?: clazz.getResourceAsStream(fName)
		?: throw IOException("Resource $fName not found.")
}

@Throws(IOException::class)
private fun mergeWithOverrides(reference: Properties): Properties {
	val result = Properties(reference)
	result.load(propsToStream(getOverrides(reference)))
	return result
}

@Throws(IOException::class)
private fun propsToStream(properties: Properties): Reader {
	val w = StringWriter()
	properties.store(w, null)
	val propsStr = w.toString()
	return StringReader(propsStr)
}

private fun getOverrides(reference: Properties): Properties {
	val overrides = Properties()
	for (nextKey in reference.stringPropertyNames()) {
		if (System.getProperties().containsKey(nextKey)) {
			overrides.setProperty(nextKey, System.getProperties().getProperty(nextKey))
		}
	}
	return overrides
}
