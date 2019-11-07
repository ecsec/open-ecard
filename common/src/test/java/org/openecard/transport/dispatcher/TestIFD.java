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

package org.openecard.transport.dispatcher;

import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.Cancel;
import iso.std.iso_iec._24727.tech.schema.CancelResponse;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ControlIFD;
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationData;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationDataResponse;
import iso.std.iso_iec._24727.tech.schema.Output;
import iso.std.iso_iec._24727.tech.schema.OutputResponse;
import iso.std.iso_iec._24727.tech.schema.PrepareDevices;
import iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import org.openecard.ws.IFD;


/**
 * IFD implementation class to test the dispatcher.
 * Most methods just return an UnsupportedOperationException. At the moment only EstablishContext returns a result
 * which itself is not a valid response object.
 *
 * @author Tobias Wich
 */
public class TestIFD implements IFD {

    @Override
    public PrepareDevicesResponse prepareDevices(PrepareDevices parameters) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

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
