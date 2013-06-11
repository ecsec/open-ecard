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

package org.openecard.richclient.gui.manage.core;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.openecard.common.I18n;
import org.openecard.common.util.FileUtils;
import org.openecard.richclient.gui.manage.AddonPanel;


/**
 * Convenience class to create a PIIN management add-on panel.
 * The panel only hosts a PIN management related action page.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PINAddon extends AddonPanel {

    private static final long serialVersionUID = 1L;
    private static final I18n lang = I18n.getTranslation("addon");
    private static final String NAME = "addon.list.core.pin_management";
    private static final String DESC = "addon.list.core.pin_management.desc";

    public PINAddon() {
	super(createAction(), lang.translationForKey(NAME), lang.translationForKey(DESC), loadLogo());
    }

    private static JPanel createAction() {
	return new PINActionPanel();
    }

    private static Image loadLogo() {
	try {
	    String fName = "images/pin-management.png";
	    InputStream in = FileUtils.resolveResourceAsStream(PINAddon.class, fName);
	    ImageIcon icon = new ImageIcon(FileUtils.toByteArray(in));
	    return icon.getImage();
	} catch (IOException ex) {
	    // ignore and let the default decide
	    return null;
	}
    }

}
