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
 */
package org.openecard.transport.dispatcher

import iso.std.iso_iec._24727.tech.schema.BeginTransaction
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse
import iso.std.iso_iec._24727.tech.schema.Cancel
import iso.std.iso_iec._24727.tech.schema.CancelResponse
import iso.std.iso_iec._24727.tech.schema.Connect
import iso.std.iso_iec._24727.tech.schema.ConnectResponse
import iso.std.iso_iec._24727.tech.schema.ControlIFD
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse
import iso.std.iso_iec._24727.tech.schema.DestroyChannel
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse
import iso.std.iso_iec._24727.tech.schema.Disconnect
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse
import iso.std.iso_iec._24727.tech.schema.EndTransaction
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse
import iso.std.iso_iec._24727.tech.schema.GetStatus
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationData
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationDataResponse
import iso.std.iso_iec._24727.tech.schema.Output
import iso.std.iso_iec._24727.tech.schema.OutputResponse
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices
import iso.std.iso_iec._24727.tech.schema.PowerDownDevicesResponse
import iso.std.iso_iec._24727.tech.schema.PrepareDevices
import iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse
import iso.std.iso_iec._24727.tech.schema.ReleaseContext
import iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import iso.std.iso_iec._24727.tech.schema.VerifyUser
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse
import iso.std.iso_iec._24727.tech.schema.Wait
import iso.std.iso_iec._24727.tech.schema.WaitResponse
import org.openecard.ws.IFD

/**
 * IFD implementation class to test the dispatcher.
 * Most methods just return an UnsupportedOperationException. At the moment only EstablishContext returns a result
 * which itself is not a valid response object.
 *
 * @author Tobias Wich
 */
class TestIFD : IFD {
	override fun prepareDevices(parameters: PrepareDevices): PrepareDevicesResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun powerDownDevices(parameters: PowerDownDevices): PowerDownDevicesResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun establishChannel(parameters: EstablishChannel): EstablishChannelResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun destroyChannel(parameters: DestroyChannel): DestroyChannelResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun establishContext(parameters: EstablishContext): EstablishContextResponse = EstablishContextResponse()

	override fun releaseContext(parameters: ReleaseContext): ReleaseContextResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun listIFDs(parameters: ListIFDs): ListIFDsResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun getIFDCapabilities(parameters: GetIFDCapabilities): GetIFDCapabilitiesResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun getStatus(parameters: GetStatus): GetStatusResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun wait(parameters: Wait): WaitResponse = throw UnsupportedOperationException("Not supported yet.")

	override fun cancel(parameters: Cancel): CancelResponse = throw UnsupportedOperationException("Not supported yet.")

	override fun controlIFD(parameters: ControlIFD): ControlIFDResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun connect(parameters: Connect): ConnectResponse = throw UnsupportedOperationException("Not supported yet.")

	override fun disconnect(parameters: Disconnect): DisconnectResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun beginTransaction(beginTransaction: BeginTransaction): BeginTransactionResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun endTransaction(parameters: EndTransaction): EndTransactionResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun transmit(parameters: Transmit): TransmitResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun verifyUser(parameters: VerifyUser): VerifyUserResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun modifyVerificationData(parameters: ModifyVerificationData): ModifyVerificationDataResponse =
		throw UnsupportedOperationException("Not supported yet.")

	override fun output(parameters: Output): OutputResponse = throw UnsupportedOperationException("Not supported yet.")
}
