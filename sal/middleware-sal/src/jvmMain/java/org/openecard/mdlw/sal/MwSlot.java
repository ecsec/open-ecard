/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import java.util.ArrayList;
import java.util.List;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;

import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.struct.CkSlot;

/**
 *
 * @author Tobias Wich
 */
public class MwSlot {

    private final MiddleWareWrapper mw;
    private final MwModule module;
    private final CkSlot slotInfo;

    /**
     * Creates a new Slot
     * 
     * @param mw
     * @param module
     * @param slotInfo
     */
    MwSlot(MiddleWareWrapper mw, MwModule module, CkSlot slotInfo) {
        this.mw = mw;
        this.module = module;
        this.slotInfo = slotInfo;
    }

    /**
     * Return the Slotinformations from a {@link MwSlot}.
     *
     * @return CkSlot
     */
    public CkSlot getSlotInfo() {
        return slotInfo;
    }

    /**
     * Obtains information about a particular {@link MwToken}.
     * in the system.
     *
     * @return
     * @throws CryptokiException
     */
    public MwToken getTokenInfo() throws CryptokiException {
        return mw.getTokenInfo(slotInfo.getSlotID());
    }

    /**
     * Opens a {@link MwSession} between an application and a {@link MwToken}.
     *
     * @return MwSession
     * @throws CryptokiException
     */
    public MwSession openSession() throws CryptokiException {
        long handle = mw.openSession(slotInfo.getSlotID(), CryptokiLibrary.CKF_RW_SESSION | CryptokiLibrary.CKF_SERIAL_SESSION);
        MwSession s = new MwSession(mw, this, handle);
        return s;
    }

    /**
     * Returns a List of {@link MwMechanism}, available on the Slot.
     * 
     * @return List
     * @throws CryptokiException
     */
    public List<MwMechanism> getMechanismList() throws CryptokiException {
        long id = slotInfo.getSlotID();
        List<MwMechanism> list = new ArrayList<>();
        long[] arr = mw.getMechanismList(id);
        for (int i = 0; i < arr.length; i++) {
            list.add(mw.getMechanismInfo(id, arr[i]));
        }

        return list;
    }

}
