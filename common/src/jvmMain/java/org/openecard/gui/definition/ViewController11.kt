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
 */
package org.openecard.gui.definition


/**
 * Interface which provides the possibility to display the Settings dialog and the default view of a client implementation.
 *
 * @author Hans-Martin Haase
 */
interface ViewController {
    /**
     * Display the Settings GUI of the used client implementation.
     */
    fun showSettingsUI()

    /**
     * Display the default view of the client implementation.
     */
    fun showDefaultViewUI()
}
