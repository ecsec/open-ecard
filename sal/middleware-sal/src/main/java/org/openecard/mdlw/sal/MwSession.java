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
import java.util.ArrayList;
import java.util.List;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.cryptoki.CK_ATTRIBUTE;
import org.openecard.mdlw.sal.enums.UserType;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.ptr.NativeLongByReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @author Jan Mannsbart
 */
public class MwSession {

    private static final Logger LOG = LoggerFactory.getLogger(MwSession.class);

    private static final NativeLong CKA_CLASS = new NativeLong(0x00);

    private final MiddleWareWrapper mw;
    private final MwSlot slot;
    private final long sessionHandle;

    /**
     * Creates a new MwSession
     * 
     * @param mw
     * @param slot
     * @param sessionHandle
     * @throws CryptokiException
     */
    MwSession(MiddleWareWrapper mw, MwSlot slot, long sessionHandle) throws CryptokiException {
        this.mw = mw;
        this.slot = slot;
        this.sessionHandle = sessionHandle;
    }

    /**
     * Gets the slot for which this session has been established.
     *
     * @return Slot of this session.
     */
    public MwSlot getSlot() {
	return slot;
    }


    /**
     * Closes a session between an application and a token.
     *
     * @throws CryptokiException
     */
    public void closeSession() throws CryptokiException {
        mw.closeSession(sessionHandle);
    }

    /**
     * Returns all Private Keys from the Token of the selected Session
     * 
     * @return List of private keys.
     * @throws CryptokiException
     */
    public List<MwPrivateKey> getPrivateKeys() throws CryptokiException {
	LOG.debug("Trying to get private key objects from middleware.");

	NativeLong privkey = new NativeLong(CryptokiLibrary.CKO_PRIVATE_KEY, true);
        NativeLongByReference temp = new NativeLongByReference(privkey);

        CK_ATTRIBUTE pTemplate = new CK_ATTRIBUTE();

        pTemplate.setType(CKA_CLASS);
        pTemplate.setPValue(temp.getPointer());
        pTemplate.setUlValueLen(new NativeLong(NativeLong.SIZE));

	List<Long> res = findObjects(pTemplate);

	List<MwPrivateKey> keyList = new ArrayList<>();
        for (long l : res) {
	    MwPrivateKey key = new MwPrivateKey(l, mw, this);
	    LOG.debug("Found private key {} (handle={}).", key, l);
            keyList.add(key);
        }

        return keyList;
    }

    /**
     * Returns all Data Objects from the Token of the selected Session
     * 
     * @return List of data objects.
     * @throws CryptokiException
     */
    public List<MwData> getData() throws CryptokiException {
        NativeLongByReference temp = new NativeLongByReference(new NativeLong(CryptokiLibrary.CKO_DATA, true));

        CK_ATTRIBUTE pTemplate = new CK_ATTRIBUTE();

        pTemplate.setType(CKA_CLASS);
        pTemplate.setPValue(temp.getPointer());
        pTemplate.setUlValueLen(new NativeLong(NativeLong.SIZE));

	List<Long> res = findObjects(pTemplate);

        List<MwData> dataList = new ArrayList<>();
        for (long l : res) {
            dataList.add(new MwData(l, mw, this));
        }

        return dataList;
    }

    /**
     * Returns all Certificates from the Token of the selected Session
     * 
     * @return List of certificates.
     * @throws CryptokiException
     */
    public List<MwCertificate> getCertificates() throws CryptokiException {
        NativeLongByReference temp = new NativeLongByReference(new NativeLong(CryptokiLibrary.CKO_CERTIFICATE, true));

        CK_ATTRIBUTE pTemplate = new CK_ATTRIBUTE();

        pTemplate.setType(CKA_CLASS);
        pTemplate.setPValue(temp.getPointer());
        pTemplate.setUlValueLen(new NativeLong(NativeLong.SIZE));

        List<Long> res = findObjects(pTemplate);

        List<MwCertificate> cerList = new ArrayList<>();
        for (long l : res) {
            cerList.add(new MwCertificate(l, mw, this));
        }

        return cerList;
    }

    /**
     * Returns all Public Keys from the Token of the selected Session
     * 
     * @return List public keys.
     * @throws CryptokiException
     */
    public List<MwPublicKey> getPublicKeys() throws CryptokiException {
        NativeLongByReference temp = new NativeLongByReference(new NativeLong(CryptokiLibrary.CKO_PUBLIC_KEY, true));

        CK_ATTRIBUTE pTemplate = new CK_ATTRIBUTE();

        pTemplate.setType(CKA_CLASS);
        pTemplate.setPValue(temp.getPointer());
        pTemplate.setUlValueLen(new NativeLong(NativeLong.SIZE));

        List<Long> res = findObjects(pTemplate);

        List<MwPublicKey> keyList = new ArrayList<>();
        for (long l : res) {
            keyList.add(new MwPublicKey(l, mw, this));
        }

        return keyList;
    }

    /**
     * Return the Session Identifier
     *
     * @return long
     */
    public long getSessionId() {
        return sessionHandle;
    }

    public void initPin(@Nullable char[] newPin) throws CryptokiException {
        byte[] pin = null;
	if (newPin != null) {
	    pin = convertPin(newPin);
	}

	try {
	    mw.initPin(sessionHandle, pin);
	} finally {
	    if (pin != null) {
		Arrays.fill(pin, (byte) 0);
	    }
	}
    }

    public void initPinExternal() throws CryptokiException {
	initPin(null);
    }

    private byte[] convertPin(char[] pin) {
	ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(pin));
	byte[] pinBytes = new byte[bb.remaining()];
	bb.get(pinBytes);
	// blank out buffer array
	if (bb.hasArray()) {
	    Arrays.fill(bb.array(), (byte) 0);
	}
	return pinBytes;
    }

    /**
     * Login into a token. User Type: {@link UserType}. PIN as String
     *
     * @param userType
     * @param pPin The pin of the user. Supplying {@code null} is the same as calling {@link #loginExternal(UserType)}.
     * @throws CryptokiException
     */
    public void login(@Nonnull UserType userType, @Nullable char[] pPin) throws CryptokiException {
        byte[] pin = null;
	if (pPin != null) {
	    pin = convertPin(pPin);
	}

	try {
	    mw.login(sessionHandle, userType.getValue(), pin);
	} finally {
	    if (pin != null) {
		Arrays.fill(pin, (byte) 0);
	    }
	}
    }

    /**
     * Login into a Token without a PIN.
     * Only {@link UserType} required. Opens dialog to enter pin.
     *
     * @param userType
     * @throws CryptokiException
     */
    public void loginExternal(UserType userType) throws CryptokiException {
	login(userType, null);
    }

    public void changePin(@Nullable char[] oldPin, @Nullable char[] newPin) throws CryptokiException {
	byte[] oldPinBytes = null;
	byte[] newPinBytes = null;
	if (oldPin != null) {
	    oldPinBytes = convertPin(oldPin);
	}
	if (newPin != null) {
	    newPinBytes = convertPin(newPin);
	}

	try {
	    mw.setPin(sessionHandle, oldPinBytes, newPinBytes);
	} finally {
	    if (oldPinBytes != null) {
		Arrays.fill(oldPinBytes, (byte) 0);
	    }
	    if (newPinBytes != null) {
		Arrays.fill(newPinBytes, (byte) 0);
	    }
	}
    }

    public void changePinExternal() throws CryptokiException {
	changePin(null, null);
    }

    /**
     * Logout an existing session. Only works if Login is performed before.
     *
     * @throws CryptokiException
     */
    public void logout() throws CryptokiException {
        mw.logout(sessionHandle);
    }

    private List<Long> findObjects(CK_ATTRIBUTE pTemplate) throws CryptokiException {
	try (MiddleWareWrapper.LockedMiddlewareWrapper lmw = mw.lock()) {
	    lmw.findObjectsInit(sessionHandle, pTemplate, 1);
	    List<Long> res = lmw.findObjects(sessionHandle);
	    lmw.findObjectsFinalize(sessionHandle);
	    return res;
	} catch (InterruptedException ex) {
	    throw new RuntimeException("Thread interrupted while waiting for Middleware lock.", ex);
	}
    }

}
