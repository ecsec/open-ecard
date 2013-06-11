/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.BoxLayout;


/**
 * Panel aggregating several setting group entries.
 * The entries are of type {@link SettingsGroup}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class SettingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private ArrayList<SettingsGroup> groups;

    /**
     * Creates a panel instance.
     */
    public SettingsPanel() {
	this.groups = new ArrayList<SettingsGroup>();
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Adds a settings group to this panel.
     *
     * @param item The group to add to the panel.
     */
    protected void addSettingsGroup(SettingsGroup item) {
	add(item);
	groups.add(item);
    }

    /**
     * Saves all settings groups of this panel.
     *
     * @throws IOException Thrown in case the properties could not be written to the output device.
     * @throws SecurityException Thrown in case the permission to save the properties is missing.
     */
    public void saveProperties() throws IOException, SecurityException {
	for (SettingsGroup next : groups) {
	    next.saveProperties();
	}
    }

}
