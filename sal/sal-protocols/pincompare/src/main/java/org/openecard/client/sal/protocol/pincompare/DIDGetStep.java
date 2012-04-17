/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.sal.protocol.pincompare;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
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

	DIDStructureType didStructure = null;
	if (didGet.getDIDScope() != null && didGet.getDIDScope().equals(DIDScopeType.GLOBAL)) {
	    didStructure = cardStateEntry.getDIDStructure(didName, cardStateEntry.getImplicitlySelectedApplicationIdentifier());
	} else {
	    didStructure = cardStateEntry.getDIDStructure(didName, connectionHandle.getCardApplication());
	}

	if (!cardStateEntry.checkApplicationSecurityCondition(connectionHandle.getCardApplication(), DifferentialIdentityServiceActionName.DID_GET)) {
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, "cardapplication"));
	}

	DIDGetResponse didGetResponse = new DIDGetResponse();
	didGetResponse.setDIDStructure(didStructure);
	didGetResponse.setResult(WSHelper.makeResultOK());
	// <editor-fold defaultstate="collapsed" desc="log trace">
	_logger.trace(_exit, "{}", didGetResponse);
	// </editor-fold>
	return didGetResponse;
    }

}
