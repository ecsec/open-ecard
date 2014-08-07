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

package org.openecard.richclient.gui.manage;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import javax.swing.JRadioButton;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.richclient.gui.manage.SettingsFactory.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class RadioButtonItem extends JRadioButton {

    private static final Logger logger = LoggerFactory.getLogger(RadioButtonItem.class);

    private final Settings properties;
    private final String name;
    private final String propertyName;

    public RadioButtonItem(String name, boolean selected, String propertyName, Settings props) {
	properties = props;
	this.name = name;
	this.propertyName = propertyName;
	setText(name);
	setSelected(selected);
	construct();
    }

    private void construct() {
	addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    properties.setProperty(propertyName, name);
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
