/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;
import org.openecard.common.ECardConstants;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.crypto.common.sal.GenericCryptoSignerFinder;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertPSKTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.auth.DynamicAuthentication;
import org.openecard.crypto.tls.TlsPSKIdentityImpl;
import org.openecard.crypto.tls.auth.CredentialFactory;
import org.openecard.crypto.tls.auth.SimpleSmartCardCredentialFactory;
import org.openecard.crypto.tls.auth.SmartCardCredentialFactory;
import org.openecard.transport.paos.PAOS;
import org.openecard.transport.paos.PAOSException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PAOSTask implements Callable<StartPAOSResponse> {

    private final Dispatcher dispatcher;
    private final ConnectionHandleType connectionHandle;
    private final TCTokenRequest tokenRequest;

    public PAOSTask(Dispatcher dispatcher, ConnectionHandleType connectionHandle, TCTokenRequest tokenRequest) {
	this.dispatcher = dispatcher;
	this.connectionHandle = connectionHandle;
	this.tokenRequest = tokenRequest;
    }


    @Override
    public StartPAOSResponse call()
	    throws MalformedURLException, PAOSException, DispatcherException, InvocationTargetException {
	TCTokenType token = tokenRequest.getTCToken();
	try {
	    String cardType = null;
	    if (connectionHandle.getRecognitionInfo() != null) {
		cardType = connectionHandle.getRecognitionInfo().getCardType();
	    }
	    if (cardType == null) {
		cardType = tokenRequest.getCardType();
	    }
	    // eID servers usually have problems with sni, so disable it for them
	    boolean noSni = "http://bsi.bund.de/cif/npa.xml".equals(cardType);

	    String sessionIdentifier = token.getSessionIdentifier();
	    URL serverAddress = new URL(token.getServerAddress());
	    String serverHost = serverAddress.getHost();
	    String secProto = token.getPathSecurityProtocol();

	    // FIXME: remove this hilariously stupid bull*#@%&/ code which satisfies a mistake introduced by the AA
	    String queryPart = serverAddress.getQuery();
	    if (queryPart == null || ! (queryPart.contains("?sessionid=") || queryPart.contains("&sessionid="))) {
		String sAddr = serverAddress.toString();
		// fix path of url
		if (serverAddress.getPath().isEmpty()) {
		    sAddr += "/";
		}
		// add parameter
		if (sAddr.endsWith("?")) {
		    sAddr += "sessionid=" + sessionIdentifier;
		} else if (sAddr.contains("?")) {
		    sAddr += "&sessionid=" + sessionIdentifier;
		} else {
		    sAddr += "?sessionid=" + sessionIdentifier;
		}
		serverAddress = new URL(sAddr);
	    }
	    // END: ugly fix


	    // Set up TLS connection
	    ClientCertTlsClient tlsClient;
	    if (secProto.equals("urn:ietf:rfc:4279") || secProto.equals("urn:ietf:rfc:5487")) {
		DynamicAuthentication tlsAuth = new DynamicAuthentication();
		tlsAuth.setHostname(serverHost);
		// FIXME: verify certificate chain as soon as a usable solution exists fpr the trust problem
		//tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		byte[] psk = token.getPathSecurityParameters().getPSK();
		TlsPSKIdentity pskId = new TlsPSKIdentityImpl(sessionIdentifier.getBytes(), psk);
		tlsClient = new ClientCertPSKTlsClient(pskId, noSni ? null : serverHost);
		tlsClient.setAuthentication(tlsAuth);
		tlsClient.setClientVersion(ProtocolVersion.TLSv11);
	    } else if (secProto.equals("urn:ietf:rfc:4346")) {
		DynamicAuthentication tlsAuth = new DynamicAuthentication();
		tlsAuth.setHostname(serverHost);
		// use a smartcard for client authentication if needed
		tlsAuth.setCredentialFactory(makeSmartCardCredential());
		// FIXME: verify certificate chain as soon as a usable solution exists fpr the trust problem
		//tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		tlsClient = new ClientCertDefaultTlsClient(noSni ? null : serverHost);
		tlsClient.setAuthentication(tlsAuth);
		tlsClient.setClientVersion(ProtocolVersion.TLSv11);
	    } else {
		throw new PAOSException("Unknow security protocol '" + secProto + "' requested.");
	    }

	    // TODO: remove this workaround as soon as eGK server uses HTTPS
	    if (serverAddress.getProtocol().equals("http")) {
		tlsClient = null;
	    }

	    // Set up PAOS connection
	    PAOS p = new PAOS(serverAddress, dispatcher, tlsClient);

	    // Create StartPAOS message
	    StartPAOS sp = new StartPAOS();
	    sp.setProfile(ECardConstants.Profile.ECARD_1_1);
	    sp.getConnectionHandle().add(connectionHandle);
	    sp.setSessionIdentifier(sessionIdentifier);

	    return p.sendStartPAOS(sp);
	} finally {
	    // disconnect card after authentication
	    CardApplicationDisconnect appDis = new CardApplicationDisconnect();
	    appDis.setConnectionHandle(connectionHandle);
	    dispatcher.deliver(appDis);
	}
    }

    private CredentialFactory makeSmartCardCredential() {
	GenericCryptoSignerFinder finder = new GenericCryptoSignerFinder(dispatcher, connectionHandle, false);
	SmartCardCredentialFactory scFac = new SmartCardCredentialFactory(finder);
	return scFac;
    }

}
