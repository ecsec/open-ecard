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
 */
package org.openecard.gui.message


/**
 * Type of the messagebox.
 * Controls the type of the messagebox such as error, or warning messages.
 *
 * @author Tobias Wich
 */
enum class DialogType {
    /** Used for error messages.  */
    ERROR_MESSAGE,

    /** Used for information messages.  */
    INFORMATION_MESSAGE,

    /** Used for warning messages.  */
    WARNING_MESSAGE,

    /** Used for questions.  */
    QUESTION_MESSAGE,

    /** No icon is used.  */
    PLAIN_MESSAGE
}
