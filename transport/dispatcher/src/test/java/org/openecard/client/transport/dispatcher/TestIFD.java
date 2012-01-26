/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.transport.dispatcher;

import iso.std.iso_iec._24727.tech.schema.*;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestIFD implements IFD {

    @Override
    public EstablishChannelResponse establishChannel(EstablishChannel parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DestroyChannelResponse destroyChannel(DestroyChannel parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EstablishContextResponse establishContext(EstablishContext parameters) {
	return new EstablishContextResponse();
    }

    @Override
    public ReleaseContextResponse releaseContext(ReleaseContext parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIFDsResponse listIFDs(ListIFDs parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GetIFDCapabilitiesResponse getIFDCapabilities(GetIFDCapabilities parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GetStatusResponse getStatus(GetStatus parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WaitResponse wait(Wait parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CancelResponse cancel(Cancel parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ControlIFDResponse controlIFD(ControlIFD parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConnectResponse connect(Connect parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DisconnectResponse disconnect(Disconnect parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BeginTransactionResponse beginTransaction(BeginTransaction beginTransaction) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EndTransactionResponse endTransaction(EndTransaction parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TransmitResponse transmit(Transmit parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VerifyUserResponse verifyUser(VerifyUser parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ModifyVerificationDataResponse modifyVerificationData(ModifyVerificationData parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputResponse output(Output parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
