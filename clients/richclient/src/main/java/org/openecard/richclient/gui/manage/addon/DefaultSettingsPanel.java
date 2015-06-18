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

import org.openecard.richclient.gui.manage.SettingsGroup;
import org.openecard.richclient.gui.manage.SettingsPanel;


/**
 * Settings panel that can be used as default. 
 * This panel hosts the given settings groups.
 *
 * @author Dirk Petrautzki
 */
public class DefaultSettingsPanel extends SettingsPanel {

    private static final long serialVersionUID = 1L;

    public DefaultSettingsPanel(SettingsGroup... groups) {
	for (SettingsGroup group : groups) {
	    addSettingsGroup(group);
	}
    }

}
