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
