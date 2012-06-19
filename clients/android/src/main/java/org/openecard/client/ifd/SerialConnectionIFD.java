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

package org.openecard.client.ifd;

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
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import org.openecard.client.common.WSHelper;
import org.openecard.client.ws.android.AndroidMarshaller;
import org.w3c.dom.Document;


/**
 * Small IFD for communication with a full IFD-Instance over a serial connection
 * (e.g. Bluetooth)
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SerialConnectionIFD implements org.openecard.ws.IFD {

    private SerialConnection transport;
    AndroidMarshaller m = new AndroidMarshaller();

    public SerialConnectionIFD(SerialConnection t) {
	this.transport = t;
    }

    private <C extends Class<T>, T extends ResponseType> T send(C c, /* RequestType */Object requestType) {
	try {

	    Document d = m.marshal(requestType);
	    byte[] response = transport.transmit(m.doc2str(d).getBytes("UTF-8"));

	    return (T) m.unmarshal(m.str2doc(new String(response, "UTF-8")));
	} catch (Exception e) {
	    e.printStackTrace();
	    return WSHelper.makeResponse(c, WSHelper.makeResult(e));
	}
    }

    public TransmitResponse transmit(Transmit t) {
	return this.send(TransmitResponse.class, t);
    }

    public ConnectResponse connect(Connect parameters) {
	return this.send(ConnectResponse.class, parameters);
    }

    public ControlIFDResponse controlIFD(ControlIFD arg0) {
	return this.send(ControlIFDResponse.class, arg0);
    }

    public EstablishContextResponse establishContext(EstablishContext arg0) {
	return this.send(EstablishContextResponse.class, arg0);
    }

    public GetStatusResponse getStatus(GetStatus parameters) {
	return this.send(GetStatusResponse.class, parameters);
    }

    public ListIFDsResponse listIFDs(ListIFDs parameters) {
	return this.send(ListIFDsResponse.class, parameters);
    }

    public ModifyVerificationDataResponse modifyVerificationData(ModifyVerificationData arg0) {
	return this.send(ModifyVerificationDataResponse.class, arg0);
    }

    public VerifyUserResponse verifyUser(VerifyUser arg0) {
	return this.send(VerifyUserResponse.class, arg0);
    }

    public GetIFDCapabilitiesResponse getIFDCapabilities(GetIFDCapabilities parameters) {
	return this.send(GetIFDCapabilitiesResponse.class, parameters);
    }

    public BeginTransactionResponse beginTransaction(BeginTransaction arg0) {
	return this.send(BeginTransactionResponse.class, arg0);
    }

    public CancelResponse cancel(Cancel arg0) {
	return this.send(CancelResponse.class, arg0);
    }

    public DisconnectResponse disconnect(Disconnect arg0) {
	return this.send(DisconnectResponse.class, arg0);
    }

    public EndTransactionResponse endTransaction(EndTransaction arg0) {
	return this.send(EndTransactionResponse.class, arg0);
    }

    public ReleaseContextResponse releaseContext(ReleaseContext arg0) {
	return this.send(ReleaseContextResponse.class, arg0);
    }

    public WaitResponse wait(Wait arg0) {
	return this.send(WaitResponse.class, arg0);
    }

    public OutputResponse output(Output arg0) {
	return this.send(OutputResponse.class, arg0);
    }

    public DestroyChannelResponse destroyChannel(DestroyChannel arg0) {
	return this.send(DestroyChannelResponse.class, arg0);
    }

    public EstablishChannelResponse establishChannel(EstablishChannel arg0) {
	return this.send(EstablishChannelResponse.class, arg0);
    }

}
