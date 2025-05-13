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
 * @author Hans-Martin Haase
 */
@XmlRootElement(name = "AppPluginSpecification")
@XmlType(
	propOrder = [
		"className", "isLoadOnStartup", "localizedName", "localizedDescription",
		"resourceName", "configDescription", "parameters", "body", "attachments",
	],
)
@XmlAccessorType(XmlAccessType.FIELD)
class AppPluginSpecification {
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

	@XmlElement(name = "LocalizedName")
	val localizedName: MutableList<LocalizedString> = ArrayList()

	@XmlElement(name = "LocalizedDescription")
	val localizedDescription: MutableList<LocalizedString> = ArrayList()

	@XmlElement(name = "ConfigDescription")
	var configDescription: Configuration? = null

	@JvmField
	@XmlElement(name = "Parameter")
	val parameters: MutableList<ParameterType?> = ArrayList<ParameterType?>()

	@JvmField
	@XmlElement(name = "ResourceName")
	var resourceName: String? = null

	@JvmField
	@XmlElement(name = "Body")
	var body: BodyType? = null

	@JvmField
	@XmlElement(name = "Attachment")
	val attachments: MutableList<AttachmentType?> = ArrayList<AttachmentType?>()

	fun getLocalizedName(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(localizedName, languageCode)

	fun getLocalizedDescription(languageCode: String): String =
		LocalizedStringExtractor.getLocalizedString(localizedDescription, languageCode)
}
