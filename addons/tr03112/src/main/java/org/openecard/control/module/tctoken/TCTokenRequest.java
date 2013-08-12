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
import java.net.URL;
import java.util.List;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.util.Pair;
import org.openecard.common.util.StringUtils;


/**
 * This class represents a TC Token request to the client. It contains the {@link TCTokenType} and situational parts
 * like the ifdName or the server certificates received while retrieving the TC Token.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TCTokenRequest {

    private TCTokenType token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;
    private String cardType = "http://bsi.bund.de/cif/npa.xml";
    private boolean tokenFromObject;
    private List<Pair<URL, Certificate>> certificates;
    private URL tcTokenURL;


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
     * Sets the server certificates that have been received when the TCToken was retrieved.
     *
     * @param certificates List of X509 certificates and the requested URLs of the servers passed.
     */
    public void setCertificates(List<Pair<URL, Certificate>> certificates) {
	this.certificates = certificates;
    }

    /**
     * Gets the certificates of the servers that have been passed while the TCToken was retrieved.
     *
     * @return List of the X509 server certificates and the requested URLs. May be null under certain circumstances
     *   (e.g. legacy activation).
     */
    public List<Pair<URL, Certificate>> getCertificates() {
	return certificates;
    }

    /**
     * Sets the TC Token URL.
     *
     * @param tcTokenURL TC Token URL
     */
    public void setTCTokenURL(URL tcTokenURL) {
	this.tcTokenURL = tcTokenURL;
    }

    /**
     * Gets the TC Token URL.
     *
     * @return TC Token URL
     */
    public URL getTCTokenURL() {
	return tcTokenURL;
    }

}
