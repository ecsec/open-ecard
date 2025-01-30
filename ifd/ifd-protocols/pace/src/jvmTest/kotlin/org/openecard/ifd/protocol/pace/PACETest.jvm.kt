/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.Connect
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import jakarta.activation.UnsupportedDataTypeException
import jakarta.xml.bind.JAXBException
import org.openecard.common.ClientEnv
import org.openecard.common.ECardConstants
import org.openecard.common.util.ByteUtils
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.openecard.ifd.scio.IFD
import org.openecard.transport.dispatcher.MessageDispatcher
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.testng.annotations.Test
import org.xml.sax.SAXException
import java.math.BigInteger

private val logger = KotlinLogging.logger {}

/**
 * @author Moritz Horsch
 */
class PACETest {
    @Test(groups = ["interactive"])
    @Throws(
        UnsupportedDataTypeException::class,
        JAXBException::class,
        SAXException::class,
        WSMarshallerException::class
    )
    fun executePACE_PIN() {
        val env = ClientEnv()
        env.gui = SwingUserConsent(SwingDialogWrapper())
        val dispatcher = MessageDispatcher(env)
        val ifd = IFD()
        ifd.setEnvironment(env)

        env.ifd = ifd
        env.dispatcher = dispatcher
        ifd.addProtocol(ECardConstants.Protocol.PACE, PACEProtocolFactory())

        val eCtx = EstablishContext()
        val ctxHandle = ifd.establishContext(eCtx).contextHandle

        val listIFDs = ListIFDs()
        listIFDs.contextHandle = ctxHandle
        val ifdName = ifd.listIFDs(listIFDs).ifdName[0]

        val connect = Connect()
        connect.contextHandle = ctxHandle
        connect.ifdName = ifdName
        connect.slot = BigInteger.ZERO
        val slotHandle = ifd.connect(connect).slotHandle

        // PinID: 02 = CAN, 03 = PIN
        val xmlCall = """
			<?xml version="1.0" encoding="UTF-8"?>
			<iso:EstablishChannel xmlns:iso="urn:iso:std:iso-iec:24727:tech:schema">
			  <iso:SlotHandle>${ByteUtils.toHexString(slotHandle)}</iso:SlotHandle>
			  <iso:AuthenticationProtocolData Protocol="urn:oid:0.4.0.127.0.7.2.2.4">
				<iso:PinID>02</iso:PinID>
				<iso:CHAT>7f4c12060904007f0007030102025305300301ffb7</iso:CHAT>
				<iso:PIN>142390</iso:PIN>
			  </iso:AuthenticationProtocolData>
			</iso:EstablishChannel>
			""".trimIndent()
        val m = createInstance()
        val eCh = m.unmarshal(m.str2doc(xmlCall)) as EstablishChannel

        val eChR = ifd.establishChannel(eCh)

        logger.info { "PACE result: ${eChR.result.resultMajor}" }
        try {
			logger.info { "${eChR.result.resultMinor}" }
			logger.info { "${eChR.result.resultMessage.value}" }
        } catch (ignore: Exception) {
        }
    }

}
