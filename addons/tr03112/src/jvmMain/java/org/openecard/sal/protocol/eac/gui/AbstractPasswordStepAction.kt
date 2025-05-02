/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.sal.protocol.eac.gui

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.addon.Context
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.ifd.anytype.PACEInputType
import org.openecard.common.util.ByteUtils
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.ifd.protocol.pace.common.PasswordID.Companion.parse
import org.openecard.sal.protocol.eac.EACData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.xml.parsers.ParserConfigurationException

/**
 *
 * @author Tobias Wich
 */
abstract class AbstractPasswordStepAction(
    protected val addonCtx: Context,
    protected val eacData: EACData,
    protected val capturePin: Boolean,
    protected val step: PINStep
) : StepAction(
    step
) {
    protected val ctx: DynamicContext

    init {
        this.ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
        // indicate that the card stays connected
    }

    @Throws(WSHelper.WSException::class, InterruptedException::class, PinOrCanEmptyException::class)
    protected fun performPACEWithPIN(
        oldResults: MutableMap<String?, ExecutionResults>,
        conHandle: ConnectionHandleType
    ): EstablishChannelResponse? {
        val protoData = eacData.didRequest.getAuthenticationProtocolData()
        val paceAuthMap: AuthDataMap?
        try {
            paceAuthMap = AuthDataMap(protoData)
        } catch (ex: ParserConfigurationException) {
            LOG.error("Failed to read EAC Protocol data.", ex)
            return null
        }
        val paceInputMap: AuthDataResponse<*> = paceAuthMap.createResponse<DIDAuthenticationDataType?>(protoData)

        if (capturePin) {
            val executionResults: ExecutionResults = oldResults.get(getStepID())!!
            val p = executionResults.getResult(PINStep.Companion.PIN_FIELD) as PasswordField
            if (p == null) {
                throw PinOrCanEmptyException("The PIN field is missing")
            }
            val pinIn = p.getValue()
            // let the user enter the pin again, when there is none entered
            // TODO: check pin length and possibly allowed charset with CardInfo file
            if (pinIn.size == 0) {
                throw PinOrCanEmptyException("PIN must not be empty")
            } else {
                // NOTE: saving pin as string prevents later removal of the value from memory !!!
                paceInputMap.addElement(PACEInputType.PIN, String(pinIn))
            }
        }

        // perform PACE
        paceInputMap.addElement(PACEInputType.PIN_ID, parse(eacData.pinID)!!.byteAsString)
        paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")
        paceInputMap.addElement(PACEInputType.CHAT, eacData.selectedCHAT.toString())
        val certDesc = ByteUtils.toHexString(eacData.rawCertificateDescription)
        paceInputMap.addElement(PACEInputType.CERTIFICATE_DESCRIPTION, certDesc)
        val eChannel = createEstablishChannelStructure(conHandle, paceInputMap)
        val res = addonCtx.getDispatcher().safeDeliver(eChannel) as EstablishChannelResponse

        return res
    }

    @Throws(WSHelper.WSException::class, InterruptedException::class, CanLengthInvalidException::class)
    protected fun performPACEWithCAN(
        oldResults: MutableMap<String?, ExecutionResults>,
        conHandle: ConnectionHandleType
    ): EstablishChannelResponse? {
        val paceInput = DIDAuthenticationDataType()
        paceInput.setProtocol(ECardConstants.Protocol.PACE)
        val tmp: AuthDataMap?
        try {
            tmp = AuthDataMap(paceInput)
        } catch (ex: ParserConfigurationException) {
            LOG.error("Failed to read empty Protocol data.", ex)
            return null
        }

        val paceInputMap: AuthDataResponse<*> = tmp.createResponse<DIDAuthenticationDataType?>(paceInput)
        if (capturePin) {
            val executionResults: ExecutionResults = oldResults.get(getStepID())!!
            val canField = executionResults.getResult(PINStep.Companion.CAN_FIELD) as PasswordField
            if (canField == null) {
                throw CanLengthInvalidException("The CAN field is missing")
            }
            val canValue = String(canField.getValue())

            if (canValue.length != 6) {
                // let the user enter the can again, when input verification failed
                throw CanLengthInvalidException("Can does not contain 6 digits.")
            } else {
                paceInputMap.addElement(PACEInputType.PIN, canValue)
            }
        }
        paceInputMap.addElement(PACEInputType.PIN_ID, PIN_ID_CAN)
        paceInputMap.addAttribute(AuthDataResponse.OEC_NS, PACEInputType.USE_SHORT_EF, "false")

        // perform PACE by EstablishChannelCommand
        val eChannel = createEstablishChannelStructure(conHandle, paceInputMap)
        val res = addonCtx.getDispatcher().safeDeliver(eChannel) as EstablishChannelResponse

        return res
    }

    private fun createEstablishChannelStructure(
        conHandle: ConnectionHandleType,
        paceInputMap: AuthDataResponse<*>
    ): EstablishChannel {
        // EstablishChannel
        val establishChannel = EstablishChannel()
        establishChannel.setSlotHandle(conHandle.getSlotHandle())
        establishChannel.setAuthenticationProtocolData(paceInputMap.getResponse())
        establishChannel.getAuthenticationProtocolData().setProtocol(ECardConstants.Protocol.PACE)
        return establishChannel
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AbstractPasswordStepAction::class.java)

        private const val PIN_ID_CAN = "2"
    }
}
