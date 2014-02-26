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

package org.openecard.common.interfaces;

import org.openecard.common.enums.EventType;


/**
 * Interface for event callback handlers.
 * This interface must be implemented by a callback registering itself in the event system.
 *
 * @see EventManager
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface EventCallback {

    /**
     * Callback funtion for IFD events.
     * This function gets called for each registered event in the {@link EventManager}.
     *
     * @param eventType Type of the event.
     * @param eventData Data describing the event further.
     */
    void signalEvent(EventType eventType, Object eventData);

}
