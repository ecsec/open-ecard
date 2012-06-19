/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.sal.protocol.genericryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import java.util.Map;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DIDGetStep implements ProtocolStep<DIDGet, DIDGetResponse> {

    private static final Logger _logger = LoggerFactory.getLogger(DIDGetStep.class);
    private static final Marker _enter = MarkerFactory.getMarker("ENTERING");
    private static final Marker _exit = MarkerFactory.getMarker("EXITING");

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDGet;
    }

    @Override
    public DIDGetResponse perform(DIDGet didGet, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	_logger.trace(_enter, "> {}, {}", didGet, internalData);
	// </editor-fold>

	String didName = didGet.getDIDName();
	ConnectionHandleType connectionHandle = didGet.getConnectionHandle();
	CardStateEntry cardStateEntry = (CardStateEntry) internalData.get("cardState");
	DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, connectionHandle.getCardApplication());

	if (!cardStateEntry.checkApplicationSecurityCondition(connectionHandle.getCardApplication(), DifferentialIdentityServiceActionName.DID_GET)) {
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, "cardapplication"));
	}

	DIDGetResponse didGetResponse = new DIDGetResponse();
	didGetResponse.setDIDStructure(didStructure);
	didGetResponse.setResult(WSHelper.makeResultOK());
	// <editor-fold defaultstate="collapsed" desc="log trace">
	_logger.trace(_exit, "< {}", didGetResponse);
	// </editor-fold>
	return didGetResponse;
    }

}
