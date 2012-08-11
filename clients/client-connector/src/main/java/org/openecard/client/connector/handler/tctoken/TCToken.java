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

import java.net.URL;


/**
 * Implements a TCToken.
 * See BSI-TR-03112, version 1.1.2, section 3.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCToken {

    // Defines the elementes of the TCToken
    protected static final String TC_TOKEN_TYPE = "TCTokenType";
    protected static final String SERVER_ADDRESS = "ServerAddress";
    protected static final String SESSION_IDENTIFIER = "SessionIdentifier";
    protected static final String REFRESH_ADDRESS = "RefreshAddress";
    protected static final String PATH_SECURITY_PROTOCOL = "PathSecurity-Protocol";
    protected static final String BINDING = "Binding";
    private URL serverAddress;
    private String sessionIdentifier;
    private URL refreshAddress;
    private String pathSecurityProtocol;
    private String binding;
    private TCToken.PathSecurityParameters pathSecurityParameters;

    /**
     * Returns the ServerAddress.
     *
     * @return ServerAddress
     */
    public URL getServerAddress() {
	return serverAddress;
    }

    /**
     * Sets the ServerAddress.
     *
     * @param serverAddress ServerAddress
     */
    protected void setServerAddress(URL serverAddress) {
	this.serverAddress = serverAddress;
    }

    /**
     * Returns the SessionIdentifier.
     *
     * @return SessionIdentifier
     */
    public String getSessionIdentifier() {
	return sessionIdentifier;
    }

    /**
     * Sets the SessionIdentifier.
     *
     * @param sessionIdentifier SessionIdentifier
     */
    protected void setSessionIdentifier(String sessionIdentifier) {
	this.sessionIdentifier = sessionIdentifier;
    }

    /**
     * Returns the RefreshAddress.
     *
     * @return RefreshAddress
     */
    public URL getRefreshAddress() {
	return refreshAddress;
    }

    /**
     * Sets the RefreshAddress
     *
     * @param refreshAddress RefreshAddress
     */
    protected void setRefreshAddress(URL refreshAddress) {
	this.refreshAddress = refreshAddress;
    }

    /**
     *
     * @return
     */
    public String getBinding() {
	return binding;
    }

    /**
     * Sets the Binding.
     *
     * @param binding Binding
     */
    protected void setBinding(String binding) {
	this.binding = binding;
    }

    /**
     * Returns the PathSecurity-Protocol.
     *
     * @return PathSecurity-Protocol
     */
    public String getPathSecurityProtocol() {
	return pathSecurityProtocol;
    }

    /**
     * Sets the PathSecurity-Protocol.
     *
     * @param pathSecurityProtocol PathSecurity-Protocol
     */
    public void setPathSecurityProtocol(String pathSecurityProtocol) {
	this.pathSecurityProtocol = pathSecurityProtocol;
    }

    /**
     * Returns the PathSecurity-Parameters.
     *
     * @return PathSecurity-Parameters
     */
    public TCToken.PathSecurityParameters getPathSecurityParameters() {
	return pathSecurityParameters;
    }

    /**
     * Sets the PathSecurity-Parameters.
     *
     * @param pathSecurityParameters PathSecurity-Parameters
     */
    public void setPathSecurityParameters(TCToken.PathSecurityParameters pathSecurityParameters) {
	this.pathSecurityParameters = pathSecurityParameters;
    }

    /**
     * PathSecurityParameters element of the TCToken.
     */
    public static class PathSecurityParameters {

	// Defines the elemente of the PathSecurityParameter
	protected static final String PATH_SECURITY_PARAMETERS = "PathSecurity-Parameters";
	protected static final String PSK = "PSK";
	private byte[] psk;

	/**
	 * Returns the pre-shared key (PSK).
	 *
	 * @return PSK
	 */
	public byte[] getPSK() {
	    return psk;
	}

	/**
	 * Sets the pre-shared key (PSK).
	 *
	 * @param psk PSK
	 */
	public void setPSK(byte[] psk) {
	    this.psk = psk;
	}

    }

}
