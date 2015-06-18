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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JTextField;


/**
 * ActionListener implementation which opens a FileChooser.
 *
 * @author Hans-Martin Haase
 */
public class OpenFileBrowserListener implements ActionListener {

    /**
     * A semicolon separated list of accepted file types.
     */
    private final String fileType;

    /**
     * The JTextField which is modified when a file was selected.
     */
    private final JTextField value;

    /**
     * Creates a new OpenFileBrowserListener object.
     *
     * @param fileTypes A semicolon separated list of accepted file types.
     * @param currentvalue A {@link JTextField} which registers this listener.
     */
    public OpenFileBrowserListener(String fileTypes, JTextField currentvalue) {
	fileType = fileTypes;
	value = currentvalue;
    }

    /**
     * Creates a new FileChooserItem and make it appear on the screen.
     *
     * The method creates a {@link FileChooserItem} and makes it visible on the screen. If the user has selected a valid
     * file according to the restrictions stored in the {@code fileType} field.
     *
     * @param e An {@link ActionEvent}.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	FileChooserItem fChooser = new FileChooserItem(fileType);
	fChooser.setVisible(true);
	int chooserresult = fChooser.showOpenDialog(null);

	if (chooserresult == JFileChooser.APPROVE_OPTION && fChooser.getSelectedFile().isFile()) {
	    value.setText(fChooser.getSelectedFile().getPath());
	}
    }

}
