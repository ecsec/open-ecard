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

import java.nio.charset.StandardCharsets;
import org.openecard.mdlw.sal.cryptoki.CK_TOKEN_INFO;
import org.openecard.mdlw.sal.cryptoki.CK_VERSION;
import org.openecard.mdlw.sal.enums.Flag;


/**
 *
 * @author Jan Mannsbart
 */
public class MwToken {

    private final CK_TOKEN_INFO orig;

    /**
     * Creates a new Token Object from given {@link CK_TOKEN_INFO}.
     * 
     * @param pInfo
     */
    public MwToken(CK_TOKEN_INFO pInfo) {
        this.orig = pInfo;

    }

    /**
     * Returns the UTC-Time.
     * 
     * @return String
     */
    public String getUtcTime() {
        return new String(orig.getUtcTime(), StandardCharsets.UTF_8).trim();
    }

    /**
     * Returns the UI total public memory.
     * 
     * @return long
     */
    public long getUlTotalPublicMemory() {
        return orig.getUlTotalPublicMemory().longValue();
    }

    /**
     * Returns the UI total private memory.
     * 
     * @return
     */
    public long getUlTotalPrivateMemory() {
        return orig.getUlTotalPrivateMemory().longValue();
    }

    /**
     * Returns the UI Session Count.
     * 
     * @return long
     */
    public long getUlSessionCount() {
        return orig.getUlSessionCount().longValue();
    }

    /**
     * Returns the UI RW Session Count.
     * 
     * @return long
     */
    public long getUlRwSessionCount() {
        return orig.getUlRwSessionCount().longValue();
    }

    /**
     * Returns the UI min PIN length.
     * 
     * @return long
     */
    public long getUlMinPinLen() {
        return orig.getUlMinPinLen().longValue();
    }

    /**
     * Returns the UI max Session Count.
     * 
     * @return long
     */
    public long getUlMaxSessionCount() {
        return orig.getUlMaxSessionCount().longValue();
    }

    /**
     * Returns the UI max RW Session Count.
     * 
     * @return long
     */
    public long getUlMaxRwSessionCount() {
        return orig.getUlMaxRwSessionCount().longValue();
    }

    /**
     * Returns the UI max PIN length.
     * 
     * @return
     */
    public long getUlMaxPinLen() {
        return orig.getUlMaxPinLen().longValue();
    }

    /**
     * Returns the UI Free Public Memory.
     * 
     * @return long
     */
    public long getUlFreePublicMemory() {
        return orig.getUlFreePublicMemory().longValue();
    }

    /**
     * Returns the Serial Number.
     * 
     * @return String
     */
    public String getSerialNumber() {
        String serial = new String(orig.getSerialNumber(), StandardCharsets.UTF_8);
	return serial.trim();
    }

    /**
     * Returns the UI Free Private Memory.
     * 
     * @return long
     */
    public long getUlFreePrivateMemory() {
        return orig.getUlFreePrivateMemory().longValue();
    }

    /**
     * Returns the Firmware Version.
     * 
     * @return {@link CK_VERSION}
     */
    public CK_VERSION getFirmwareVerion() {
        return orig.getFirmwareVersion();
    }

    /**
     * Returns if the Token can Auto Read.
     * 
     * @return boolean
     */
    public boolean getAutoRead() {
        return orig.getAutoRead();
    }

    /**
     * Returns if the Token can Auto Write.
     * 
     * @return boolean
     */
    public boolean getAutoWrite() {
        return orig.getAutoWrite();
    }

    /**
     * Returns the Hardware Version.
     * 
     * @return {@link CK_VERSION}
     */
    public CK_VERSION getHardwareVersion() {
        return orig.getHardwareVersion();
    }

    /**
     * Returns true if a Token contains the given Flag.
     * 
     * Example: <br>
     * <pre>
     * if (list.get(0).getTokenInfo().containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH)) {
     *   System.out.println("Terminal bereit!");
     *   session.loginWithoutPin(UserType.User);
     * } else {
     *   System.out.println("Kein Terminal bereit!");
     *   session.login(UserType.User, "123123");
     * }
     * </pre>
     *
     * @param flag
     * @return
     */
    public boolean containsFlag(Flag flag) {
        return (orig.getFlags().longValue() & flag.getValue()) > 0;
    }

    /**
     * Returns the Token Label.
     * 
     * @return String
     */
    public String getLabel() {
        String label = new String(orig.getLabel(), StandardCharsets.UTF_8);
	return label.trim();
    }

    /**
     * Gets the tokens manufacturer ID.
     *
     * @return String
     */
    public String getManufacturerID() {
        String manId = new String(orig.getManufacturerID(), StandardCharsets.UTF_8);
	return manId.trim();
    }

    /**
     * Returns the Token Model.
     * 
     * @return String
     */
    public String getModel() {
        String model = new String(orig.getModel(), StandardCharsets.UTF_8);
	return model.trim();
    }

}
