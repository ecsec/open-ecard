/****************************************************************************
 * Copyright (C) 2019-2024 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.addon.Context
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.ifd.PACECapabilities
import org.openecard.common.ifd.PacePinStatus
import org.openecard.common.sal.util.InsertCardHelper
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType

/**
 *
 * @author Tobias Wich
 */
class PaceCardHelper(ctx: Context, conHandle: ConnectionHandleType) : InsertCardHelper(
    ctx, conHandle
) {
    @Throws(WSHelper.WSException::class)
    fun getPaceMarker(pinType: String, cardType: String): PACEMarkerType {
        return if (isConnected()) {
            getPaceMarkerFromSal(pinType)
        } else {
            getPaceMarkerFromCif(pinType, cardType)
        }
    }

    @Throws(WSHelper.WSException::class)
    private fun getPaceMarkerFromSal(pinType: String): PACEMarkerType {
        val dg = DIDGet()
        dg.connectionHandle = conHandle
        dg.didName = pinType
        val dgr = ctx.dispatcher.safeDeliver(dg) as DIDGetResponse
        checkResult(dgr)

        val didStructure = dgr.didStructure
        val didMarker = didStructure.didMarker as iso.std.iso_iec._24727.tech.schema.PACEMarkerType
        return PACEMarkerType(didMarker)
    }

    private fun getPaceMarkerFromCif(pinType: String, cardType: String): PACEMarkerType {
        val cif = ctx.recognition.getCardInfo(cardType)
        if (cif != null) {
            for (app in cif.applicationCapabilities.cardApplication) {
                for (did in app.didInfo) {
                    if (pinType == did.differentialIdentity.didName) {
                        // convert marker
                        val marker = did.differentialIdentity.didMarker.paceMarker
                        val wrappedMarker = PACEMarkerType(marker)
                        return wrappedMarker
                    }
                }
            }
        }

        // nothing found, this means the code is just wrong
        val msg = String.format("The requested DID=%s is not available in the nPA CIF.", pinType)
        throw IllegalArgumentException(msg)
    }


    @get:Throws(WSHelper.WSException::class)
    val isNativePinEntry: Boolean
        /**
         * Check if the selected card reader supports PACE.
         * In that case, the reader is a standard or comfort reader.
         *
         * @return true when card reader supports genericPACE, false otherwise.
         * @throws WSHelper.WSException
         */
        get() {
            // Request terminal capabilities
            val capabilitiesRequest = GetIFDCapabilities()
            capabilitiesRequest.contextHandle = conHandle.contextHandle
            capabilitiesRequest.ifdName = conHandle.ifdName
            val capabilitiesResponse = ctx.dispatcher.safeDeliver(capabilitiesRequest) as GetIFDCapabilitiesResponse
            checkResult(capabilitiesResponse)

            if (capabilitiesResponse.ifdCapabilities != null) {
                val capabilities = capabilitiesResponse.ifdCapabilities.slotCapability
                // Check all capabilities for generic PACE
                val genericPACE = PACECapabilities.PACECapability.GenericPACE.protocol
                for (capability in capabilities) {
                    if (capability.index == conHandle.slotIndex) {
                        for (protocol in capability.protocol) {
                            if (protocol == genericPACE) {
                                return true
                            }
                        }
                    }
                }
            }

            // No PACE capability found
            return false
        }

    @get:Throws(WSHelper.WSException::class)
    val pinStatus: PacePinStatus
        get() {
            val input = InputAPDUInfoType()
            input.inputAPDU = byteArrayOf(
                0x00.toByte(),
                0x22.toByte(),
                0xC1.toByte(),
                0xA4.toByte(),
                0x0F.toByte(),
                0x80.toByte(),
                0x0A.toByte(),
                0x04.toByte(),
                0x00.toByte(),
                0x7F.toByte(),
                0x00.toByte(),
                0x07.toByte(),
                0x02.toByte(),
                0x02.toByte(),
                0x04.toByte(),
                0x02.toByte(),
                0x02.toByte(),
                0x83.toByte(),
                0x01.toByte(),
                0x03.toByte()
            )
            input.acceptableStatusCode.addAll(PacePinStatus.getCodes())

            val transmit = Transmit()
            transmit.slotHandle = conHandle.slotHandle
            transmit.inputAPDUInfo.add(input)

            val pinCheckResponse = ctx.dispatcher.safeDeliver(transmit) as TransmitResponse
            checkResult(pinCheckResponse)
            val output = pinCheckResponse.outputAPDU[0]
            val outputApdu = CardResponseAPDU(output)
            val status = outputApdu.statusBytes
            return PacePinStatus.fromCode(status)
        }

}