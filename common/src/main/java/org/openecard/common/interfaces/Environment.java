/****************************************************************************
 * Copyright (C) 2012-2020 ecsec GmbH.
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

import java.util.List;
import org.openecard.gui.UserConsent;
import org.openecard.ws.IFD;
import org.openecard.ws.Management;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes Schmoelz
 */
public interface Environment {

    void setGUI(UserConsent gui);
    UserConsent getGUI();

    void setIFD(IFD ifd);
    IFD getIFD();

    void addIFDCtx(byte[] ctx);
    void removeIFDCtx(byte[] ctx);
    List<byte[]> getIFDCtx();

    void setSAL(SAL sal);
    SAL getSAL();

    void setEventDispatcher(EventDispatcher manager);
    EventDispatcher getEventDispatcher();

    void setDispatcher(Dispatcher dispatcher);
    Dispatcher getDispatcher();

    void setRecognition(CardRecognition recognition);
    CardRecognition getRecognition();

    void setCIFProvider(CIFProvider provider);
    CIFProvider getCIFProvider();

    void setSalSelector(SalSelector salSelect);
    SalSelector getSalSelector();

    void setGenericComponent(String id, Object component);
    Object getGenericComponent(String id);

    void setManagement(Management management);
    Management getManagement();

}
