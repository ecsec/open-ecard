/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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
 * Marker interface for Android GUI implementations.
 * The ecard protocol identifier of the GUI can be requested as text.
 *
 * @author Tobias Wich
 */
public interface AndroidGui {

    /**
     * Gets the ecard protocol identifier handled by this GUI.
     * This may be the identifier for EAC, Pin Compare, etc.
     *
     * @return Protocol identifier.
     */
    String getProtocolType();

}
