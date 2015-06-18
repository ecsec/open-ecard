/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.crypto.common.sal;


/**
 *
 * @author Hans-Martin Haase
 */
public class DIDCertificate {

    /**
     * Constant for TLS version 1.0.
     */
    public static final int TLSv10 = 0;

    /**
     * Constant for TLS version 1.1.
     */
    public static final int TLSv11 = 1;

    /**
     * Constant for TLS version 1.2.
     */
    public static final int TLSv12 = 2;

    /**
     * The name of the DataSet which contains the certificate.
     */
    private String dataSetName;

    /**
     * The name of the DID which references the certificate.
     */
    private String didName;

    /**
     * Variable which indicates whether the certificate DataSet is always readable or not.
     */
    private boolean alwaysReadable = false;

    /**
     * The application identifier of the application which contains the DID.
     */
    private byte[] applicationIdentifier;

    /**
     * A byte array containing the certificate.
     */
    private byte[] rawCertificate;

    /**
     * A integer variable which indicates the minimal TLS version which can be used with the DID.
     */
    private int minTlsVerion = TLSv10;

    public DIDCertificate() {
    }

    /**
     * Get the name of the DataSet which contains the certificate.
     *
     * @return The name of the certificate DataSet.
     */
    public String getDataSetName() {
	return dataSetName;
    }

    /**
     * Get the name of the DID which references the certificate.
     *
     * @return Name of DID which references the certificate.
     */
    public String getDIDName() {
	return didName;
    }

    /**
     * Get the application identifier of the application which contains the DID.
     *
     * @return Application identifier of the DID.
     */
    public byte[] getApplicationIdentifier() {
	return applicationIdentifier;
    }

    /**
     * Get the card certificate as raw byte array.
     *
     * @return A byte array containing the certificate from the card or null if the certificate is not always readable.
     */
    public byte[] getRawCertificate() {
	return rawCertificate;
    }

    /**
     * Get the minimal TLS version which is supported by the DID.
     *
     * @return The method returns 0 for TLS 1.0, 1 for TLS 1.1 and 2 for TLS 1.2.
     */
    public int getMinTLSVersion() {
	return minTlsVerion;
    }

    /**
     * Get the information whether the certificate is always readable or not.
     *
     * @return The method returns TRUE if the certificate DataSet is always readable else FALSE is returned.
     */
    public boolean isAlwaysReadable() {
	return alwaysReadable;
    }

    /**
     * Set the raw certificate.
     *
     * @param rawCert A byte array containing a certificate.
     */
    public void setRawCertificate(byte[] rawCert) {
	this.rawCertificate = rawCert;
    }

    /**
     * Sets the minimal TLS version which is supported by the DID.
     * <br>
     * <br>
     * The class provides the following public constants: <br>
     * <br>
     * - TLS10 for TLS in version 1.0
     * - TLS11 for TLS in version 1.1
     * - TLS12 for TLS in version 1.2
     *
     * @param version One of the constants mentioned above in the description.
     * @throws IllegalArgumentException Thrown if the input parameter is not one of the above mentioned constants.
     */
    public void setMinTLSVersion(int version) throws IllegalArgumentException {
	if (version != 0 && version != 1 && version != 2) {
	    throw new IllegalArgumentException("There are no TLS versions defined for the argument " + version);
	} else {
	    minTlsVerion = version;
	}
    }

    /**
     * Sets the certificate DataSet to always readable.
     * Note: This method changes nothing at the card. The method changes just a boolean variable in this object to
     * represent the behavior of the file in the card.
     */
    public void setAlwaysReadable() {
	alwaysReadable = true;
    }

    /**
     * Sets the name of the DID.
     *
     * @param didName The name of the DID to set.
     */
    public void setDIDName(String didName) {
	this.didName = didName;
    }

    /**
     * Sets the name of the DataSet which contains the certificate.
     *
     * @param dSetName The DataSet name of the certificate.
     */
    public void setDataSetName(String dSetName) {
	dataSetName = dSetName;
    }

    /**+
     * Sets the application identifier of the application which contains the DID.
     *
     * @param appID A byte array containing the application identifier of the apllication which contains the DID.
     */
    public void setApplicationID(byte[] appID) {
	applicationIdentifier = appID;
    }

}
