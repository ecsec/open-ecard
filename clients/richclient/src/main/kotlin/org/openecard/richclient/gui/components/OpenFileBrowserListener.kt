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
package org.openecard.richclient.gui.components

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JFileChooser
import javax.swing.JTextField

/**
 * ActionListener implementation which opens a FileChooser.
 *
 * @author Hans-Martin Haase
 */
class OpenFileBrowserListener
/**
 * Creates a new OpenFileBrowserListener object.
 *
 * @param fileTypes A semicolon separated list of accepted file types.
 * @param currentvalue A [JTextField] which registers this listener.
 */(
    /**
     * A semicolon separated list of accepted file types.
     */
    private val fileType: String,
    /**
     * The JTextField which is modified when a file was selected.
     */
    private val value: JTextField
) : ActionListener {
    /**
     * Creates a new FileChooserItem and make it appear on the screen.
     *
     * The method creates a [FileChooserItem] and makes it visible on the screen. If the user has selected a valid
     * file according to the restrictions stored in the `fileType` field.
     *
     * @param e An [ActionEvent].
     */
    override fun actionPerformed(e: ActionEvent) {
        val fChooser: FileChooserItem = FileChooserItem(fileType)
        fChooser.setVisible(true)
        val chooserresult: Int = fChooser.showOpenDialog(null)

        if (chooserresult == JFileChooser.APPROVE_OPTION && fChooser.getSelectedFile().isFile()) {
            value.setText(fChooser.getSelectedFile().getPath())
        }
    }
}
