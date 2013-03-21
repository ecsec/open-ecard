/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.control.module.tctoken;

import generated.TCTokenType;
import java.math.BigInteger;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.util.StringUtils;
import org.openecard.control.client.ClientRequest;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenRequest extends ClientRequest {

    private TCTokenType token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;
    private String cardType = "http://bsi.bund.de/cif/npa.xml";
    private boolean tokenFromObject;
    private Certificate certificate;


    /**
     * Returns the TCToken.
     *
     * @return TCToken
     */
    public TCTokenType getTCToken() {
	return token;
    }

    /**
     * Sets the TCToken.
     *
     * @param token TCToken
     */
    public void setTCToken(TCTokenType token) {
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

    /**
     * Returns the card type selected for this authentication process.
     * Defaults to the nPA identifier to provide a fallback.
     *
     * @return Card type
     */
    public String getCardType() {
	return cardType;
    }

    /**
     * Sets the card type.
     *
     * @param cardType Card type
     */
    public void setCardType(String cardType) {
	this.cardType = cardType;
    }

    /**
     * Gets whether the token was created from an object tag or fetched from a URL.
     *
     * @return {@code true} when the token was created from an object tag, {@code false} otherwise.
     */
    public boolean isTokenFromObject() {
	return tokenFromObject;
    }

    /**
     * Sets whether the token was created from an object tag or fetched from a URL.
     *
     * @param tokenFromObject {@code true} when the token was created from an object tag, {@code false} otherwise.
     */
    public void setTokenFromObject(boolean tokenFromObject) {
	this.tokenFromObject = tokenFromObject;
    }

    /**
     * Sets the certificate of the service where the TCToken has been received.
     *
     * @param certificate X509 certificate of the server.
     */
    public void setCertificate(Certificate certificate) {
	this.certificate = certificate;
    }

    /**
     * Gets the certificate of the service where the TCToken has been received.
     *
     * @return X509 certificate of the server. May be null, when no certificate is available, e.g. legacy activation.
     */
    public Certificate getCertificate() {
	return certificate;
    }

}
