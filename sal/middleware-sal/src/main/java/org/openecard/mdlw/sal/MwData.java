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

import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.struct.CkAttribute;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;


/**
 *
 * @author Tobias Wich
 */
public class MwData {

    private final long objectHandle;
    private final MiddleWareWrapper mw;
    private final MwSession session;
    private final String application;
    private final byte[] value;
    private final byte[] objectId;

    /**
     * Creates new Data Object from given Object Handle
     * 
     * @param objectHandle
     * @param mw
     * @param mwSession
     * @throws CryptokiException
     */
    public MwData(long objectHandle, MiddleWareWrapper mw, MwSession mwSession) throws CryptokiException {
        this.objectHandle = objectHandle;
        this.mw = mw;
        this.session = mwSession;
        this.application = loadAttrValApp();
        this.value = loadAttrValValue();
        this.objectId = loadAttrValObjectID();
    }

    /**
     * Loading the Attribute Value for CKA_APPLICATION
     * 
     * @return String
     * @throws CryptokiException
     */
    private String loadAttrValApp() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_APPLICATION);
	return AttributeUtils.getString(raw);
    }

    /**
     * Loading the Attribute Value for CKA_VALUE
     * 
     * @return byte[]
     * @throws CryptokiException
     */
    private byte[] loadAttrValValue() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_VALUE);
	return AttributeUtils.getBytes(raw);
    }

    /**
     * Loading the Attribute Value for CKA_OBJECT_ID
     * 
     * @return byte[]
     * @throws CryptokiException
     */
    private byte[] loadAttrValObjectID() throws CryptokiException {
        CkAttribute raw = mw.getAttributeValue(session.getSessionId(), objectHandle, CryptokiLibrary.CKA_OBJECT_ID);
	return AttributeUtils.getBytes(raw);
    }

    /**
     * Returns the Application Name
     * 
     * @return
     */
    public String getApplicationName() {
        return application;
    }

    /**
     * Returns the Data Value
     * 
     * @return byte[]
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Returns the Object Identifier
     * 
     * @return byte[]
     */
    public byte[] getObjectID() {
        return objectId;
    }
}
