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

package org.openecard.client.connector.handler.tctoken;

import java.math.BigInteger;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.connector.client.ClientRequest;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenRequest extends ClientRequest {

    private TCToken token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;

    /**
     * Returns the TCToken.
     *
     * @return TCToken
     */
    public TCToken getTCToken() {
	return token;
    }

    /**
     * Sets the TCToken.
     *
     * @param token TCToken
     */
    public void setTCToken(TCToken token) {
	this.token = token;
    }

    /**
     * Returns the IFD name.
     *
     * @return IFD name
     */
    public String getIFDName() {
	return ifdName;
    }

    /**
     * Sets the IFD name.
     *
     * @param ifdName IFD name
     */
    public void setIFDName(String ifdName) {
	this.ifdName = ifdName;
    }

    /**
     * Returns the context handle.
     *
     * @return Context handle
     */
    public byte[] getContextHandle() {
	return contextHandle;
    }

    /**
     * Sets the context handle.
     *
     * @param contextHandle Context handle
     */
    public void setContextHandle(String contextHandle) {
	this.contextHandle = StringUtils.toByteArray(contextHandle);
    }

    /**
     * Returns the slot index.
     *
     * @return Slot index
     */
    public BigInteger getSlotIndex() {
	return slotIndex;
    }

    /**
     * Sets the slot index.
     *
     * @param slotIndex Slot index
     */
    public void setSlotIndex(String slotIndex) {
	this.slotIndex = new BigInteger(slotIndex);
    }

}
