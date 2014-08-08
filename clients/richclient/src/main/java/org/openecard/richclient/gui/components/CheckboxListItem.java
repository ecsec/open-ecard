/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.richclient.gui.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import javax.swing.JCheckBox;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.richclient.gui.manage.SettingsFactory.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class CheckboxListItem extends JCheckBox {

    private static final Logger logger = LoggerFactory.getLogger(CheckboxListItem.class);

    private final String itemLabel;
    private final String propName;
    private final Settings properties;

    public CheckboxListItem(String name, boolean selected, String propertyName, Settings props) {
	setSelected(selected);
	setText(name);
	itemLabel = name;
	propName = propertyName;
	properties = props;
	construct();
    }

    private void construct() {
	addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		String propValue = properties.getProperty(propName);
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    if (propValue == null) {
			properties.setProperty(propName, itemLabel);
		    } else {
			    // property value is not null so some other options are selected so append the now selected
			// option
			properties.setProperty(propName, propValue.concat(";" + itemLabel));
		    }
		} else if (e.getStateChange() == ItemEvent.DESELECTED) {
		    if (propValue.equals(itemLabel)) {
			// just the current was selected so set an empty string
			properties.setProperty(propName, "");
		    } else {
			// element somewhere between all others
			if (propValue.contains(";" + itemLabel + ";")) {
			    propValue = propValue.replace(";" + itemLabel + ";", ";");
			    properties.setProperty(propName, propValue);
			} else if (propValue.contains(";" + itemLabel)) {
			    // last element
			    propValue = propValue.replace(";" + itemLabel, ";");
			    properties.setProperty(propName, propValue);
			} else {
			    // first element
			    propValue = propValue.replace(itemLabel + ";", "");
			    properties.setProperty(propName, propValue);
			}
		    }
		}
		try {
		    properties.store();
		} catch (AddonPropertiesException | IOException ex) {
		    logger.error("Failed to save settings.", ex);
		}
	    }

	});
    }
}
