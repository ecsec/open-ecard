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

package org.openecard.gui.messagebox;


/**
 * Return type of the messagebox.
 * Indicates which button was pressed to exit the message box.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public enum ReturnType {
    /** Return value if YES is chosen. */
    YES,
    /** Return value if NO is chosen. */
    NO,
    /** Return value if CANCEL is chosen. */
    CANCEL,
    /** Return value if OK is chosen. */
    OK,
    /**
     * Return value if user closes window without selecting anything, more than likely this should be treated as either
     * a <code>CANCEL_OPTION</code> or <code>NO_OPTION</code>.
     */
    CLOSED;
}
