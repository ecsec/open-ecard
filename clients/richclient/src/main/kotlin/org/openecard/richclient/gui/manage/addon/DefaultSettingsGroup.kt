/****************************************************************************
 * Copyright (C) 2013-2014 HS Coburg.
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

package org.openecard.richclient.gui.manage.addon

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonPropertiesException
import org.openecard.addon.manifest.ConfigurationEntry
import org.openecard.addon.manifest.EnumEntry
import org.openecard.addon.manifest.EnumListEntry
import org.openecard.addon.manifest.FileEntry
import org.openecard.addon.manifest.FileListEntry
import org.openecard.addon.manifest.ScalarEntry
import org.openecard.addon.manifest.ScalarEntryType
import org.openecard.addon.manifest.ScalarListEntry
import org.openecard.addon.manifest.ScalarListEntryType
import org.openecard.richclient.gui.manage.Settings
import org.openecard.richclient.gui.manage.SettingsGroup
import java.io.IOException

private val logger = KotlinLogging.logger { }

/**
 * SettingsGroup that can be used as default.
 * For every ConfigurationEntry in the given AddonSpecification an according item will be added.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class DefaultSettingsGroup(
	title: String?,
	settings: Settings,
	configEntries: List<ConfigurationEntry>,
) : SettingsGroup(title, settings) {
	init {
		for (entry: ConfigurationEntry in configEntries) {
			val name: String = entry.getLocalizedName(LANGUAGE_CODE)
			val description: String = entry.getLocalizedDescription(LANGUAGE_CODE)

			// match entry types with class, else the type hierarchy is implicit in the if an that is a bad thing
			if (ScalarEntry::class.java == entry.javaClass) {
				val scalarEntry: ScalarEntry = entry as ScalarEntry
				when (scalarEntry.getType()) {
					ScalarEntryType.STRING.name -> {
						addInputItem(name, description, entry.key!!)
					}
					ScalarEntryType.BOOLEAN.name -> {
						addBoolItem(name, description, entry.key!!)
					}
					ScalarEntryType.BIGDECIMAL.name -> {
						addScalarEntryTypNumber(name, description, scalarEntry.key!!, scalarEntry.getType())
					}
					ScalarEntryType.BIGINTEGER.name -> {
						addScalarEntryTypNumber(name, description, scalarEntry.key!!, scalarEntry.getType())
					}
					else -> {
						error { "Untreated ScalarEntry type: ${scalarEntry.getType()}" }
					}
				}
			} else if (ScalarListEntry::class.java == entry.javaClass) {
				addScalarListItem(
					name,
					description,
					entry.key!!,
					ScalarListEntryType.valueOf((entry as ScalarListEntry).getType()),
				)
			} else if (EnumEntry::class.java == entry.javaClass) {
				val enumEntry: EnumEntry = entry as EnumEntry
				val values = enumEntry.values
				addSelectionItem(name, description, enumEntry.key!!, *values.toTypedArray())
			} else if (EnumListEntry::class.java == entry.javaClass) {
				val enumEntry: EnumListEntry = entry as EnumListEntry
				val values = enumEntry.values
				addMultiSelectionItem(name, description, entry.key!!, values)
			} else if (FileEntry::class.java == entry.javaClass) {
				val fEntry: FileEntry = entry as FileEntry
				addFileEntry(name, description, entry.key!!, fEntry.fileType!!, fEntry.isRequiredBeforeAction!!)
			} else if (FileListEntry::class.java == entry.javaClass) {
				val fEntry: FileListEntry = entry as FileListEntry
				addFileListEntry(
					name,
					description,
					entry.key!!,
					fEntry.fileType!!,
					fEntry.isRequiredBeforeAction!!,
				)
			} else {
				logger.error { "Untreated entry type: ${entry.javaClass.getName()}" }
			}
		}
	}

	@Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
	override fun saveProperties() {
		super.saveProperties()
	}

	companion object {
		private val LANGUAGE_CODE: String = System.getProperty("user.language")
	}
}
