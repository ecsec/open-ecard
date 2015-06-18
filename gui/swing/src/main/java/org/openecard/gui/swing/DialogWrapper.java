/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.gui.swing;

import java.awt.Container;


/**
 * Interface to give the swing GUI the ability to set a title, show and hide the dialog and get a drawing pane.
 *
 * @author Tobias Wich
 */
public interface DialogWrapper {

    /**
     * Set title of the user consent dialog.
     *
     * @param title Title to set in the dialog.
     */
    void setTitle(String title);

    /**
     * A content panel is needed so the user consent can be embedded in the actual application.
     *
     * @return Container the GUI can draw its content on.
     */
    Container getContentPane();

    /**
     * This function is executed after the root panel has been set up with the contents of the user consent.
     */
    void show();

    /**
     * This function is executed after the user consent is finished or canceled.
     */
    void hide();

}
