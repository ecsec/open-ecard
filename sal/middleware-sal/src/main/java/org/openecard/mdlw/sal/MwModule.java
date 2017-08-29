/****************************************************************************
 * Copyright (C) 2015-2016 ecsec GmbH.
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

import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import org.openecard.mdlw.sal.struct.CkInfo;
import org.openecard.mdlw.sal.struct.CkSlot;
import java.util.ArrayList;
import java.util.List;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.FinalizationException;
import org.openecard.mdlw.sal.exceptions.InitializationException;
import org.openecard.mdlw.sal.enums.TokenState;
import java.util.Collections;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;


/**
 * 
 * @author Tobias Wich
 */
public class MwModule {

    private final MiddlewareSALConfig mwSALConfig;

    private MiddleWareWrapper mw;

    public MwModule(MiddlewareSALConfig mwSALConfig) {
        this.mwSALConfig = mwSALConfig;
    }

    /**
     * Initializes the Cryptoki library.
     *
     * @throws UnsatisfiedLinkError Thrown in case the library could not be found.
     * @throws InitializationException Thrown in case the Middleware could not be initialized.
     */
    public void initialize() throws UnsatisfiedLinkError, InitializationException {
	try {
	    mw = new MiddleWareWrapper(mwSALConfig);
	    mw.initialize();
	} catch (CryptokiException ex) {
	    throw new InitializationException("Failed to initalize PKCS#11 middleware.", ex.getErrorCode());
	}
    }

    /**
     * Indicates that an application is done with the Cryptoki library.
     *
     * @throws FinalizationException
     */
    public void destroy() throws FinalizationException {
	try {
	    mw.destroy();
	} catch (CryptokiException ex) {
	    throw new FinalizationException("Failed to shutdown PKCS#11 middleware.", ex.getErrorCode());
	}
    }

    /**
     * Returns general information about Cryptoki.
     *
     * @return CkInfo
     * @throws CryptokiException
     */
    public CkInfo getInfo() throws CryptokiException {
        return mw.getInfo();
    }

    /**
     * Obtains a list of slots in the system.
     *
     * @param tokenState When {@code true}, only return slots with a present token.
     * @return
     * @throws CryptokiException
     */
    public List<MwSlot> getSlotList(TokenState tokenState) throws CryptokiException {
        return getSlotList(tokenState.getValue());
    }

    /**
     * Obtains a list of slots in the system.
     *
     * @param tokenPresent When {@code true}, only return slots with a present token.
     * @return
     * @throws CryptokiException
     */
    public List<MwSlot> getSlotList(boolean tokenPresent) throws CryptokiException {
        ArrayList<MwSlot> slots = new ArrayList<>();
        for (long slotId : mw.getSlotList(tokenPresent)) {
            CkSlot slotInfo = mw.getSlotInfo(slotId);
            MwSlot slot = new MwSlot(mw, this, slotInfo);
            slots.add(slot);
        }

        return Collections.unmodifiableList(slots);
    }

    /**
     * Obtains the MiddlewareSALConfig which is used for the PKCS#11 middleware and specified cards.
     * 
     * @return MiddlewareSALConfig
     */
    public MiddlewareSALConfig getMiddlewareSALConfig() {
        return mwSALConfig;
    }

    /**
     * Waits for a slot event (token insertion, removal, etc.) to occur.
     *
     * @param flag 0 = blocking, 1 = nonblocking
     * @return changed slotID or -1 in case mode is non-blocking and nothing happened.
     * @throws CryptokiException Thrown in case an error happened.
     */
    // TODO return slot
    public long waitForSlotEvent(int flag) throws CryptokiException {
	try {
	    return mw.waitForSlotEvent(flag);
	} catch (CryptokiException ex) {
	    if (flag == 1 && ex.getErrorCode() == CryptokiLibrary.CKR_NO_EVENT) {
		return -1;
	    } else {
		throw ex;
	    }
	}
    }

}
