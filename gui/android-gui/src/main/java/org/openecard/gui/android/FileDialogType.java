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

package org.openecard.gui.android;


/**
 * Type of the file dialog.
 * Controls the type of the messagebox such as error, or warning messages.
 *
 * @author Dirk Petrautzki
 */
public enum FileDialogType {

    /** Used for file dialogs to save a file */
    SAVE,
    /** Used for file dialogs to open a file */
    OPEN,
    /** Used for other file dialogs (neither a standard save or open) */
    OTHER;

}
