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

import org.openecard.ws.IFD;
import org.openecard.ws.Management;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes Schmoelz
 */
public interface Environment {

    void setIFD(IFD ifd);
    IFD getIFD();

    void setSAL(SAL sal);
    SAL getSAL();

    void setEventManager(EventManager manager);
    EventManager getEventManager();

    void setDispatcher(Dispatcher dispatcher);
    Dispatcher getDispatcher();

    void setGenericComponent(String id, Object component);
    Object getGenericComponent(String id);

    void setManagement(Management management);
    Management getManagement();

}
