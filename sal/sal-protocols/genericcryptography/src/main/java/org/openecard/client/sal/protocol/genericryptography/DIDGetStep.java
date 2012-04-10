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

package org.openecard.client.sal.protocol.genericryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.sal.TinySAL;
import org.openecard.ws.SAL;


/**
 *
 * @author Dirk Petrautzki <petrautzki at hs-coburg.de>
 */
public class DIDGetStep implements ProtocolStep<DIDGet, DIDGetResponse> {

    private TinySAL sal;
    private static final Logger _logger = LogManager.getLogger(DIDGetStep.class.getName());

    public DIDGetStep(SAL sal){
	this.sal = (TinySAL) sal;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.DIDGet;
    }

    @Override
    public DIDGetResponse perform(DIDGet didGet, Map<String, Object> internalData) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "perform(DIDGet didGet, Map<String, Object> internalData)");
	} // </editor-fold>

	String didName = didGet.getDIDName();
	DIDScopeType didScope = didGet.getDIDScope();
	ConnectionHandleType connectionHandle = didGet.getConnectionHandle();
	CardStateEntry cardStateEntry = sal.getStates().getEntry(connectionHandle);
	CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	DIDStructureType didStructure = cardInfoWrapper.getDIDStructure(didName, didScope);

	if(!cardInfoWrapper.checkSecurityCondition(cardInfoWrapper.getCardApplication(connectionHandle.getCardApplication()), DifferentialIdentityServiceActionName.DID_GET)){
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, "cardapplication"));
	}

	DIDGetResponse didGetResponse = new DIDGetResponse();
	didGetResponse.setDIDStructure(didStructure);
	didGetResponse.setResult(WSHelper.makeResultOK());
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "perform(DIDGet didGet, Map<String, Object> internalData)", didGetResponse);
	} // </editor-fold>
	return didGetResponse;
    }

}
