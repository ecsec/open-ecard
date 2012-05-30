/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.connector.tctoken;

import java.net.URL;



/**
 * Implements a TCToken.
 * See BSI-TR-03112, version 1.1.2, section 3.3.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
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
    private TCToken.PathSecurityParameter pathSecurityParameter;

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
     * Returns the PathSecurity-Parameter.
     *
     * @return PathSecurity-Parameter
     */
    public TCToken.PathSecurityParameter getPathSecurityParameter() {
	return pathSecurityParameter;
    }

    /**
     * Sets the PathSecurity-Parameter.
     *
     * @param pathSecurityParameter PathSecurity-Parameter
     */
    public void setPathSecurityParameter(TCToken.PathSecurityParameter pathSecurityParameter) {
	this.pathSecurityParameter = pathSecurityParameter;
    }

    /**
     * PathSecurityParameter element of the TCToken.
     */
    public static class PathSecurityParameter {

	// Defines the elemente of the PathSecurityParameter
	protected static final String PATH_SECURITY_PARAMETER = "PathSecurity-Parameters";
	//FIXME
//        protected static final String PATH_SECURITY_PARAMETER = "PathSecurity-Parameter";
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
