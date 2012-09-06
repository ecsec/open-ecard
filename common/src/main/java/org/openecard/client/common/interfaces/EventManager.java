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
package org.openecard.client.common.interfaces;

import java.util.List;
import org.openecard.client.common.enums.EventType;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface EventManager {
    
    public Object initialize();
    public void terminate();
    
    public void register(EventCallback callback, EventFilter filter);
    public void register(EventCallback callback, EventType type);
    public void register(EventCallback callback, List<EventType> types);
    
    public void registerAllEvents(EventCallback callback);
    
    public void unregister(EventCallback callback);

}
