/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.ConfigurationEntry;
import org.openecard.addon.manifest.EnumEntry;
import org.openecard.addon.manifest.EnumListEntry;
import org.openecard.addon.manifest.ScalarEntry;
import org.openecard.addon.manifest.ScalarListEntry;
import org.openecard.common.util.FileUtils;
import org.openecard.richclient.gui.manage.SettingsGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SettingsGroup that can be used as default.
 * For every ConfigurationEntry in the given AddonSpecification an according item will be added.
 *
 * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
 */
public class DefaultSettingsGroup extends SettingsGroup {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DefaultSettingsGroup.class);

    private static final String LANGUAGE_CODE = System.getProperty("user.language");

    private AddonSpecification desc;

    public DefaultSettingsGroup(String title, Properties properties, AddonSpecification desc) {
	super(title, properties);
	this.desc = desc;
	for (ConfigurationEntry entry : desc.getConfigDescription().getEntries()) {
	    String name = entry.getLocalizedName(LANGUAGE_CODE);
	    String description = entry.getLocalizedDescription(LANGUAGE_CODE);

	    // match entry types with class, else the type hierarchy is implicit in the if an that is a bad thing
	    if (ScalarEntry.class.equals(entry.getClass())) {
		ScalarEntry scalarEntry = (ScalarEntry) entry;
		if (scalarEntry.getType().equalsIgnoreCase("string")) {
		    addInputItem(name, description, entry.getKey());
		} else if (scalarEntry.getType().equalsIgnoreCase("boolean")) {
		    addBoolItem(name, description, entry.getKey());
		} else {
		    logger.error("Untreated ScalarEntry type: {}", scalarEntry.getType());
		}
	    } else if (ScalarListEntry.class.equals(entry.getClass())) {
		addScalarListItem(name, description, entry.getKey());
	    } else if (EnumEntry.class.equals(entry.getClass())) {
		EnumEntry enumEntry = (EnumEntry) entry;
		List<String> values = enumEntry.getValues();
		// TODO: implement function
		//addMultiSelectionItem(name, description, entry.getKey(), values);
	    } else if (EnumListEntry.class.equals(entry.getClass())) {
		EnumListEntry enumEntry = (EnumListEntry) entry;
		List<String> values = enumEntry.getValues();
		// TODO: use correct function
		// addMultiSelectionItem(name, description, entry.getKey(), values);
		logger.error("Yet unsupported entry type: {}", entry.getClass().getName());
	    } else {
		logger.error("Untreated entry type: {}", entry.getClass().getName());
	    }
	    // TODO: implement missing types EnumEntry, EnumListEntry
	}
    }

    @Override
    protected void saveProperties() throws IOException, SecurityException {
	File home = FileUtils.getHomeConfigDir();
	File path = new File(home, "addons");
	path = new File(path, desc.getId());
	File config = new File(path, "settings" + ".properties");
	FileWriter writer = new FileWriter(config);
	properties.store(writer, null);
    }

}
