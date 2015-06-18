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

/**
 * Main interfaces of the user consent (or GUI).
 * The user consent interfaces are an abstraction, so that user consent implementations can be implemented without
 * exposing platform specific portions of the code.<br>
 * The user consent definition defines two kinds of dialogs:
 * <ul>
 * <li>Navigation based user consents (see {@link UserConsentNavigator})</li>
 * <li>File dialogs (see {@link FileDialog})</li>
 * </ul>
 * These types can be created by a user consent implementation which is represented by the {@link UserConsent}
 * interface.
 */
package org.openecard.gui;
