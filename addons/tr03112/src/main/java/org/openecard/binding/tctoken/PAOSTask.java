/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.Callable;
import org.openecard.binding.tctoken.ex.ErrorTranslations;
import org.openecard.common.ECardConstants;
import org.openecard.common.AppVersion;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.interfaces.DocumentSchemaValidator;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.util.Promise;
import org.openecard.transport.paos.PAOS;
import org.openecard.transport.paos.PAOSConnectionException;
import org.openecard.transport.paos.PAOSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class PAOSTask implements Callable<StartPAOSResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(PAOSTask.class);

    private final Dispatcher dispatcher;
    private final ConnectionHandleType connectionHandle;
    private final List<String> supportedDIDs;
    private final TCTokenRequest tokenRequest;
    private final Promise<DocumentSchemaValidator> schemaValidator;

    public PAOSTask(Dispatcher dispatcher, ConnectionHandleType connectionHandle, List<String> supportedDIDs,
	    TCTokenRequest tokenRequest, Promise<DocumentSchemaValidator> schemaValidator) {
	this.dispatcher = dispatcher;
	this.connectionHandle = connectionHandle;
	this.supportedDIDs = supportedDIDs;
	this.tokenRequest = tokenRequest;
	this.schemaValidator = schemaValidator;
    }

    @Override
    public StartPAOSResponse call()
	    throws MalformedURLException, PAOSException, DispatcherException, InvocationTargetException,
	    ConnectionError, PAOSConnectionException {
	try {
	    TlsConnectionHandler tlsHandler = new TlsConnectionHandler(dispatcher, tokenRequest, connectionHandle);
	    tlsHandler.setUpClient();

	    DocumentSchemaValidator v;
	    try {
		v = schemaValidator.deref();
	    } catch (InterruptedException ex) {
		// TODO: add i18n
		throw new PAOSException(ErrorTranslations.PAOS_INTERRUPTED);
	    }

	    // Set up PAOS connection
	    PAOS p = new PAOS(dispatcher, tlsHandler, v);

	    // Create StartPAOS message
	    StartPAOS sp = new StartPAOS();
	    sp.setProfile(ECardConstants.Profile.ECARD_1_1);
	    sp.getConnectionHandle().add(getHandleForServer());
	    sp.setSessionIdentifier(tlsHandler.getSessionId());

	    StartPAOS.UserAgent ua = new StartPAOS.UserAgent();
	    ua.setName(AppVersion.getName());
	    ua.setVersionMajor(BigInteger.valueOf(AppVersion.getMajor()));
	    ua.setVersionMinor(BigInteger.valueOf(AppVersion.getMinor()));
	    ua.setVersionSubminor(BigInteger.valueOf(AppVersion.getPatch()));
	    sp.setUserAgent(ua);

	    StartPAOS.SupportedAPIVersions sv = new StartPAOS.SupportedAPIVersions();
	    sv.setMajor(ECardConstants.ECARD_API_VERSION_MAJOR);
	    sv.setMinor(ECardConstants.ECARD_API_VERSION_MINOR);
	    sv.setSubminor(ECardConstants.ECARD_API_VERSION_SUBMINOR);
	    sp.getSupportedAPIVersions().add(sv);

	    sp.getSupportedDIDProtocols().addAll(supportedDIDs);
	    return p.sendStartPAOS(sp);
	} finally {

	    try {
		TCTokenHandler.disconnectHandle(dispatcher, connectionHandle);
	    } catch (Exception ex) {
		LOG.warn("Error disconnecting finished handle.", ex);
	    }
	}
    }

    private ConnectionHandleType getHandleForServer() {
	ConnectionHandleType result = HandlerUtils.copyHandle(connectionHandle);
	// this is our own extension and servers might not understand it
	result.setSlotInfo(null);
	return result;
    }

}
