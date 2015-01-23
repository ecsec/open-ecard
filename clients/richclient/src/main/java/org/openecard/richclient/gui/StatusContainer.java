/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.richclient.gui;

import java.awt.Container;
import javax.swing.RootPaneContainer;


/**
 * Interface a container class holding a pane from the Status class must implement.
 * The purpose of this interface is that the Status class can add the status pane to the container and that it can
 * trigger UI updates on it.
 *
 * @author Tobias Wich
 */
public interface StatusContainer extends RootPaneContainer {

    /**
     * Perform a UI update of the status elemnt in the container represented by this class.+
     *
     * @param status The status object which needs to be redrawn.
     */
    void updateContent(Container status);

}
