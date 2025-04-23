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
package org.openecard.ifd.scio

import iso.std.iso_iec._24727.tech.schema.Connect
import iso.std.iso_iec._24727.tech.schema.ControlIFD
import iso.std.iso_iec._24727.tech.schema.EstablishChannel
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.InputUnitType
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType.ASCII_NUMERIC
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType.BCD
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType.HALF_NIBBLE_BCD
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType.ISO_9564_1
import iso.std.iso_iec._24727.tech.schema.PinInputType
import iso.std.iso_iec._24727.tech.schema.VerifyUser
import jakarta.activation.UnsupportedDataTypeException
import jakarta.xml.bind.JAXBException
import org.openecard.common.ClientEnv
import org.openecard.common.ECardConstants
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.PINUtils
import org.openecard.common.util.StringUtils
import org.openecard.common.util.UtilException
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.openecard.ifd.scio.reader.PCSCFeatures
import org.openecard.ifd.scio.reader.PCSCPinModify
import org.openecard.ifd.scio.reader.PCSCPinVerify
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory
import org.testng.Assert
import org.testng.annotations.Test
import org.xml.sax.SAXException
import java.math.BigInteger
import java.util.*
import kotlin.test.assertEquals

/**
 *
 * @author Tobias Wich
 */
class PINTest {
	@Test
	@Throws(UtilException::class)
	fun testISO() {
		val pwdAttr = create(true, ISO_9564_1, 4, 8, 12)

		val pinMask = PINUtils.createPinMask(pwdAttr)
		Assert.assertEquals(
			byteArrayOf(
				0x20,
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
				0xFF.toByte(),
			),
			pinMask,
		)

		val pinResult: ByteArray? = PINUtils.encodePin("123456789".toCharArray(), pwdAttr)
		Assert.assertEquals(
			byteArrayOf(0x29, 0x12, 0x34, 0x56, 0x78, 0x9F.toByte(), 0xFF.toByte(), 0xFF.toByte()),
			pinResult,
		)
	}

	@Test
	@Throws(UtilException::class)
	fun testBCD() {
		val pwdAttr = create(true, BCD, 4, 3, 6)
		pwdAttr.setPadChar(byteArrayOf(0xFF.toByte()))

		val pinMask: ByteArray = PINUtils.createPinMask(pwdAttr)
		Assert.assertEquals(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()), pinMask)

		val pinResult: ByteArray? = PINUtils.encodePin("12345".toCharArray(), pwdAttr)
		Assert.assertEquals(byteArrayOf(0x12.toByte(), 0x34.toByte(), 0x5F.toByte()), pinResult)
	}

	@Test
	@Throws(UtilException::class)
	fun testASCII() {
		var pwdAttr = create(false, ASCII_NUMERIC, 6, 6)

		val pinResult: ByteArray? = PINUtils.encodePin("123456".toCharArray(), pwdAttr)
		Assert.assertEquals(byteArrayOf(0x31, 0x32, 0x33, 0x34, 0x35, 0x36), pinResult)

		try {
			pwdAttr = create(true, ASCII_NUMERIC, 6, 6)
			PINUtils.encodePin("123456".toCharArray(), pwdAttr)
			Assert.fail() // padding needed, but no char given
		} catch (ex: UtilException) {
		}
		// 	try {
// 	    pwdAttr = create(false, ASCII_NUMERIC, 6, 7);
// 	    PINUtils.encodePin("123456", pwdAttr);
// 	    fail(); // padding inferred, but no char given
// 	} catch (UtilException ex) {
// 	}
	}

	@Test
	@Throws(UtilException::class)
	fun testHalfNibble() {
		var pwdAttr = create(false, HALF_NIBBLE_BCD, 6, 6)

		var pinResult: ByteArray? = PINUtils.encodePin("123456".toCharArray(), pwdAttr)
		Assert.assertEquals(
			byteArrayOf(
				0xF1.toByte(),
				0xF2.toByte(),
				0xF3.toByte(),
				0xF4.toByte(),
				0xF5.toByte(),
				0xF6.toByte(),
			),
			pinResult,
		)

		pwdAttr = create(true, HALF_NIBBLE_BCD, 6, 7)
		pwdAttr.setPadChar(byteArrayOf(0xFF.toByte()))

		pinResult = PINUtils.encodePin("123456".toCharArray(), pwdAttr)
		Assert.assertEquals(
			byteArrayOf(
				0xF1.toByte(),
				0xF2.toByte(),
				0xF3.toByte(),
				0xF4.toByte(),
				0xF5.toByte(),
				0xF6.toByte(),
				0xFF.toByte(),
			),
			pinResult,
		)
	}

	@Test
	@Throws(IFDException::class)
	fun verifyISO() {
		val pwdAttr = create(true, ISO_9564_1, 4, 8)
		val ctrlStruct = PCSCPinVerify(pwdAttr, StringUtils.toByteArray("00200001"))
		ctrlStruct.setLang(Locale.GERMANY)
		val structData: ByteArray = ctrlStruct.toBytes()
		val pinStr = "00 20 00 01 08 20 FF FF FF FF FF FF FF" // length=13
		val ctrlStr = "3C 00 89 47 04 0E04 02 01 0704 00 000000 0D000000"
		val referenceData = StringUtils.toByteArray(ctrlStr + pinStr, true)
		Assert.assertEquals(referenceData, structData)
	}

	@Test
	@Throws(IFDException::class)
	fun verifyASCII() {
		val pwdAttr = create(false, ASCII_NUMERIC, 4, 4)
		val ctrlStruct = PCSCPinVerify(pwdAttr, StringUtils.toByteArray("00200001"))
		ctrlStruct.setLang(Locale.GERMANY)
		val structData: ByteArray = ctrlStruct.toBytes()
		val pinStr = "00 20 00 01" // length=5
		val ctrlStr = "3C 00 82 04 00 0404 02 01 0704 00 000000 04000000"
		val referenceData = StringUtils.toByteArray(ctrlStr + pinStr, true)
		Assert.assertEquals(referenceData, structData)
	}

	@Test(enabled = false)
	@Throws(IFDException::class, WSMarshallerException::class, SAXException::class)
	fun testModifyPin() {
		val ifd = IFD()
		val env = ClientEnv()
		env.gui = SwingUserConsent(SwingDialogWrapper())
		ifd.setEnvironment(env)
		val eCtx = EstablishContext()
		val ctxHandle: ByteArray? = ifd.establishContext(eCtx).getContextHandle()

		val listIFDs = ListIFDs()
		listIFDs.setContextHandle(ctxHandle)
		val ifdName: String? = ifd.listIFDs(listIFDs).getIFDName().get(0)

		val connect: Connect = Connect()
		connect.setContextHandle(ctxHandle)
		connect.setIFDName(ifdName)
		connect.setSlot(BigInteger.ZERO)
		val slotHandle: ByteArray? = ifd.connect(connect).getSlotHandle()

		// prepare pace call
		val xmlCall =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<iso:EstablishChannel xmlns:iso=\"urn:iso:std:iso-iec:24727:tech:schema\">\n" +
				"  <iso:SlotHandle>" + ByteUtils.toHexString(slotHandle) + "</iso:SlotHandle>\n" +
				"  <iso:AuthenticationProtocolData Protocol=\"urn:oid:0.4.0.127.0.7.2.2.4\">\n" +
				"    <iso:PinID>03</iso:PinID>\n" +
				"  </iso:AuthenticationProtocolData>\n" +
				"</iso:EstablishChannel>"
		val m = WSMarshallerFactory.createInstance()
		val eCh: EstablishChannel = m.unmarshal(m.str2doc(xmlCall)) as EstablishChannel

		// send pace call
		val eChR = ifd.establishChannel(eCh)
		assertEquals(ECardConstants.Major.OK, eChR.getResult().getResultMajor())

		val pwdAttr = create(true, ASCII_NUMERIC, 6, 6, 6)
		pwdAttr.setPadChar(byteArrayOf(0x3F.toByte()))
		val ctrlStruct = PCSCPinModify(pwdAttr, StringUtils.toByteArray("002C0203"))
		val structData: ByteArray = ctrlStruct.toBytes()
		val pinStr = "00 2C 02 03 06 3F3F3F3F3F3F"
		val ctrlStr = "15 05 82 06 00 00 00 0606 01 02 02 0407 00 01 02 000000 0B000000"

		// This is the command the 'AusweisApp' sends
		// String ausweisApp = "150582080000000606010202090400010200000005000000002C020300";
		val referenceData = StringUtils.toByteArray(ctrlStr + pinStr, true)
		Assert.assertEquals(referenceData, structData)

		val controlIFD = ControlIFD()
		controlIFD.setCommand(ByteUtils.concatenate(PCSCFeatures.MODIFY_PIN_DIRECT.toByte(), structData))
		controlIFD.setSlotHandle(slotHandle)
		val response = ifd.controlIFD(controlIFD)
	}

	@Test(enabled = false)
	fun verifyeGK() {
		val ifd = IFD()
		val env: ClientEnv = ClientEnv()
		env.gui = SwingUserConsent(SwingDialogWrapper())
		ifd.setEnvironment(env)
		val eCtx: EstablishContext = EstablishContext()
		val ctxHandle: ByteArray? = ifd.establishContext(eCtx).getContextHandle()

		val listIFDs: ListIFDs = ListIFDs()
		listIFDs.setContextHandle(ctxHandle)
		val ifdName: String? = ifd.listIFDs(listIFDs).getIFDName().get(0)

		val connect: Connect = Connect()
		connect.setContextHandle(ctxHandle)
		connect.setIFDName(ifdName)
		connect.setSlot(BigInteger.ZERO)
		val slotHandle: ByteArray? = ifd.connect(connect).getSlotHandle()

		val verify = VerifyUser()
		verify.setSlotHandle(slotHandle)
		val inputUnit = InputUnitType()
		verify.setInputUnit(inputUnit)
		val pinInput = PinInputType()
		inputUnit.setPinInput(pinInput)
		pinInput.setIndex(BigInteger.ZERO)
		pinInput.setPasswordAttributes(create(true, ISO_9564_1, 6, 8, 8))
		verify.setTemplate(StringUtils.toByteArray("00 20 00 01", true))
		val verifyR = ifd.verifyUser(verify)
		val responseCode: ByteArray? = verifyR.getResponse()
	}

	@Test(enabled = false)
	@Throws(
		UnsupportedDataTypeException::class,
		JAXBException::class,
		SAXException::class,
		WSMarshallerException::class,
	)
	fun executePACE_PIN() {
		val ifd = IFD()
		val env: ClientEnv = ClientEnv()
		env.gui = SwingUserConsent(SwingDialogWrapper())
		ifd.setEnvironment(env)
		val eCtx: EstablishContext = EstablishContext()
		val ctxHandle: ByteArray? = ifd.establishContext(eCtx).getContextHandle()

		val listIFDs: ListIFDs = ListIFDs()
		listIFDs.setContextHandle(ctxHandle)
		val ifdName: String? = ifd.listIFDs(listIFDs).getIFDName().get(0)

		val connect: Connect = Connect()
		connect.setContextHandle(ctxHandle)
		connect.setIFDName(ifdName)
		connect.setSlot(BigInteger.ZERO)
		val slotHandle: ByteArray? = ifd.connect(connect).getSlotHandle()

		// prepare pace call
		val xmlCall =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<iso:EstablishChannel xmlns:iso=\"urn:iso:std:iso-iec:24727:tech:schema\">\n" +
				"  <iso:SlotHandle>" + ByteUtils.toHexString(slotHandle) + "</iso:SlotHandle>\n" +
				"  <iso:AuthenticationProtocolData Protocol=\"urn:oid:0.4.0.127.0.7.2.2.4\">\n" +
				"    <iso:PinID>03</iso:PinID>\n" +
				"  </iso:AuthenticationProtocolData>\n" +
				"</iso:EstablishChannel>"
		val m = WSMarshallerFactory.createInstance()
		val eCh: EstablishChannel = m.unmarshal(m.str2doc(xmlCall)) as EstablishChannel

		// send pace call
		val eChR = ifd.establishChannel(eCh)
	}

	companion object {
		private fun create(
			needsPadding: Boolean,
			pwdType: PasswordTypeType?,
			minLen: Int,
			storedLen: Int,
			maxLen: Int,
		): PasswordAttributesType {
			val r: PasswordAttributesType = create(needsPadding, pwdType, minLen, storedLen)
			r.setMaxLength(BigInteger.valueOf(maxLen.toLong()))
			return r
		}

		private fun create(
			needsPadding: Boolean,
			pwdType: PasswordTypeType?,
			minLen: Int,
			storedLen: Int,
		): PasswordAttributesType {
			val r: PasswordAttributesType = PasswordAttributesType()
			r.setMinLength(BigInteger.valueOf(minLen.toLong()))
			r.setStoredLength(BigInteger.valueOf(storedLen.toLong()))
			r.setPwdType(pwdType)
			if (needsPadding) {
				r.getPwdFlags().add("needs-padding")
			}
			return r
		}
	}
}
