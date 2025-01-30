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

package org.openecard.richclient.gui.manage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonProperties
import org.openecard.addon.AddonPropertiesException
import org.openecard.common.OpenecardProperties
import java.io.IOException
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Factory implementation which provides a various Setting objects.
 *
 * @author Hans-Martin Haase
 */
object SettingsFactory {

	/**
	 * Get a Settings object from a fresh Properties object obtained from OpenecardProperties.
	 *
	 * @return A Settings object which wraps the `pops` object.
	 */
    fun getInstance(): Settings {
		return OpenecardPropertiesWrapper()
	}

    fun getInstance(props: Properties): Settings {
        return NonSavingProperties(props)
    }

    /**
     * Get a Settings object from the given AddonProperties object.
     *
     * @param props The AddonProperties to wrap in the Settings object.
     * @return A Settings object which wraps the `props` object.
     */
    fun getInstance(props: AddonProperties): Settings {
        return AddonPropertiesWrapper(props)
    }


    /**
     * The class extends the Settings class wrapping an AddonProperties object.
     *
     * @author Hans-Martin Haase
     */
    class AddonPropertiesWrapper(private val props: AddonProperties) : Settings {
        init {
            try {
                props.loadProperties()
            } catch (ex: AddonPropertiesException) {
                logger.error(ex) { "Failed to load AddonProperties." }
            }
        }

        override fun setProperty(key: String?, value: String?) {
            props.setProperty(key, value)
        }

        override fun getProperty(key: String?): String? {
            return props.getProperty(key)
        }

        @Throws(AddonPropertiesException::class)
        override fun store() {
            props.saveProperties()
        }
    }

    /**
     * The class extends the NonSavingProperties class and wraps OpenecardProperties.
     *
     * @author Hans-Martin Haase
     */
    class OpenecardPropertiesWrapper : NonSavingProperties(OpenecardProperties.properties()) {
        @Throws(IOException::class)
        override fun store() {
            OpenecardProperties.writeChanges(props)
        }
    }

    /**
     * This class wraps a properties object but is not able to save it.
     *
     * @author Tobias Wich
     */
    open class NonSavingProperties(protected val props: Properties) : Settings {
        override fun setProperty(key: String?, value: String?) {
            props.setProperty(key, value)
        }

        override fun getProperty(key: String?): String? {
            return props.getProperty(key)
        }

        @Throws(IOException::class)
        override fun store() {
            OpenecardProperties.writeChanges(props)
        }
    }
}
