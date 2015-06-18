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

package org.openecard.richclient.gui.manage.addon;

import java.io.IOException;
import java.util.List;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.addon.manifest.ConfigurationEntry;
import org.openecard.addon.manifest.EnumEntry;
import org.openecard.addon.manifest.EnumListEntry;
import org.openecard.addon.manifest.FileEntry;
import org.openecard.addon.manifest.FileListEntry;
import org.openecard.addon.manifest.ScalarEntry;
import org.openecard.addon.manifest.ScalarEntryType;
import org.openecard.addon.manifest.ScalarListEntry;
import org.openecard.addon.manifest.ScalarListEntryType;
import org.openecard.richclient.gui.manage.Settings;
import org.openecard.richclient.gui.manage.SettingsGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SettingsGroup that can be used as default.
 * For every ConfigurationEntry in the given AddonSpecification an according item will be added.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class DefaultSettingsGroup extends SettingsGroup {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DefaultSettingsGroup.class);
    private static final String LANGUAGE_CODE = System.getProperty("user.language");

    public DefaultSettingsGroup(String title, Settings settings, List<ConfigurationEntry> configEntries) {
	super(title, settings);
	for (ConfigurationEntry entry : configEntries) {
	    String name = entry.getLocalizedName(LANGUAGE_CODE);
	    String description = entry.getLocalizedDescription(LANGUAGE_CODE);

	    // match entry types with class, else the type hierarchy is implicit in the if an that is a bad thing
	    if (ScalarEntry.class.equals(entry.getClass())) {
		ScalarEntry scalarEntry = (ScalarEntry) entry;
		if (scalarEntry.getType().equals(ScalarEntryType.STRING.name())) {
		    addInputItem(name, description, entry.getKey());
		} else if (scalarEntry.getType().equals(ScalarEntryType.BOOLEAN.name())) {
		    addBoolItem(name, description, entry.getKey());
		} else if (scalarEntry.getType().equals(ScalarEntryType.BIGDECIMAL.name())) {
		    addScalarEntryTypNumber(name, description, scalarEntry.getKey(), scalarEntry.getType());
		} else if (scalarEntry.getType().equals(ScalarEntryType.BIGINTEGER.name())) {
		    addScalarEntryTypNumber(name, description, scalarEntry.getKey(), scalarEntry.getType());
		} else {
		    logger.error("Untreated ScalarEntry type: {}", scalarEntry.getType());
		}
	    } else if (ScalarListEntry.class.equals(entry.getClass())) {
		addScalarListItem(name, description, entry.getKey(),
			ScalarListEntryType.valueOf(((ScalarListEntry) entry).getType()));
	    } else if (EnumEntry.class.equals(entry.getClass())) {
		EnumEntry enumEntry = (EnumEntry) entry;
		List<String> values = enumEntry.getValues();
		addSelectionItem(name, description, enumEntry.getKey(), values.toArray(new String[values.size()]));
	    } else if (EnumListEntry.class.equals(entry.getClass())) {
		EnumListEntry enumEntry = (EnumListEntry) entry;
		List<String> values = enumEntry.getValues();
		addMultiSelectionItem(name, description, entry.getKey(), values);
	    } else if (FileEntry.class.equals(entry.getClass())) {
		FileEntry fEntry = (FileEntry) entry;
		addFileEntry(name, description, entry.getKey(), fEntry.getFileType(), fEntry.isRequiredBeforeAction());
	    } else if (FileListEntry.class.equals(entry.getClass())) {
		FileListEntry fEntry = (FileListEntry) entry;
		addFileListEntry(name, description, entry.getKey(), fEntry.getFileType(), fEntry.isRequiredBeforeAction());
	    } else {
		logger.error("Untreated entry type: {}", entry.getClass().getName());
	    }
	}
    }

    @Override
    protected void saveProperties() throws IOException, SecurityException, AddonPropertiesException {
	super.saveProperties();
    }

}
