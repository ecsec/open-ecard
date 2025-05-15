/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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
package org.openecard.addon.manifest

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType
import org.openecard.addon.utils.LocalizedStringExtractor

/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
@XmlRootElement(name = "ProtocolPluginSpecification")
@XmlType(
	propOrder = ["uri", "className", "isLoadOnStartup", "localizedName", "localizedDescription", "configDescription"],
)
@XmlAccessorType(
	XmlAccessType.FIELD,
)
class ProtocolPluginSpecification {
	@JvmField
	@XmlElement(name = "ClassName")
	var className: String? = null

	@XmlElement(name = "LoadOnStartup", required = false, defaultValue = "false")
	var isLoadOnStartup: Boolean? = null
		get() {
			if (field == null) {
				return false
			}
			return field
		}

	@JvmField
	@XmlElement(name = "URI")
	var uri: String? = null

	@XmlElement(name = "LocalizedName")
	val localizedName: MutableList<LocalizedString> = ArrayList()

	@XmlElement(name = "LocalizedDescription")
	val localizedDescription: MutableList<LocalizedString> = ArrayList()

	@XmlElement(name = "ConfigDescription")
	var configDescription: Configuration? = null

	fun getLocalizedName(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(localizedName, languageCode)

	fun getLocalizedDescription(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(localizedDescription, languageCode)
}
