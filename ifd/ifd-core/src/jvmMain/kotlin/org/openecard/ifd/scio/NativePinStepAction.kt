/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.PinInputType
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.gui.StepResult
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.ifd.scio.reader.PCSCFeatures
import org.openecard.ifd.scio.reader.PCSCPinVerify
import org.openecard.ifd.scio.wrapper.SingleThreadChannel
import org.openecard.ifd.scio.wrapper.TerminalInfo

/**
 * Action to perform a native pin verify in the GUI executor.
 *
 * @author Tobias Wich
 */
class NativePinStepAction(
	stepName: String,
	private val pinInput: PinInputType,
	private val ch: SingleThreadChannel,
	private val termInfo: TerminalInfo,
	private val template: ByteArray,
) : StepAction(stepName) {
	var exception: IFDException? = null
	var response: ByteArray? = null

	override fun perform(
		oldResults: MutableMap<String, ExecutionResults>?,
		result: StepResult?,
	): StepActionResult {
		try {
			response = nativePinVerify()
		} catch (ex: SCIOException) {
			exception = IFDException(ex)
		} catch (ex: IFDException) {
			exception = ex
		} catch (ex: InterruptedException) {
			exception = IFDException(ex)
		}
		return StepActionResult(StepActionResultStatus.NEXT)
	}

	@Throws(IFDException::class, SCIOException::class, InterruptedException::class)
	private fun nativePinVerify(): ByteArray {
		// get data for verify command and perform it
		val verifyStruct = PCSCPinVerify(pinInput.getPasswordAttributes(), template)
		val verifyStructData = verifyStruct.toBytes()
		// only called when this terminal has the capability
		val features = termInfo.featureCodes
		val result = ch.transmitControlCommand(features[PCSCFeatures.VERIFY_PIN_DIRECT]!!, verifyStructData)
		return result
	}
}
