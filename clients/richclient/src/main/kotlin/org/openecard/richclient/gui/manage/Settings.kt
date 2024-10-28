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

import org.openecard.addon.AddonPropertiesException
import java.io.IOException

/**
 * Interface capable of wrapping Properties providing a store function.
 *
 * @author Hans-Martin Haase
 */
interface Settings {
    /**
     * Set a property with the property name `key` and the value `value`.
     *
     * @param key The key name of the property to set.
     * @param value The value of the property to set.
     */
    fun setProperty(key: String?, value: String?)

    /**
     * Get a property by a key.
     *
     * @param key The key to look for in the properties.
     * @return The value of the property which corresponds to the `key`.
     */
    fun getProperty(key: String?): String?

    /**
     * Save the currently set properties to a file.
     *
     * @throws AddonPropertiesException Thrown in case an exception occurred in the saveProperties() function of a
     * wrapped AddonProeprties object.
     * @throws IOException Thrown in case of an error while writing the openecard.properties file.
     */
    @Throws(AddonPropertiesException::class, IOException::class)
    fun store()
}
