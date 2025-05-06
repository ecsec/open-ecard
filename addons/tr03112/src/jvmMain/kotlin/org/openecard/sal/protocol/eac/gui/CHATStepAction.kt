/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
package org.openecard.sal.protocol.eac.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.addon.Context
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.ifd.PacePinStatus
import org.openecard.common.util.SysUtils
import org.openecard.crypto.common.asn1.cvc.CHAT
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Checkbox
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.ifd.protocol.pace.common.PasswordID
import org.openecard.ifd.protocol.pace.common.PasswordID.Companion.parse
import org.openecard.sal.protocol.eac.EACData
import org.openecard.sal.protocol.eac.EACProtocol
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType

private val logger = KotlinLogging.logger { }

private val LANG: I18n = I18n.getTranslation("pace")!!
private val PIN: String? = LANG.translationForKey("pin")
private val PUK: String? = LANG.translationForKey("puk")

/**
 * StepAction for evaluation of CHAT value items on the EAC GUI.
 *
 * @author Tobias Wich
 */
class CHATStepAction(
	private val addonCtx: Context,
	private val eacData: EACData,
	step: Step,
) : StepAction(step) {
	override fun perform(
		oldResults: MutableMap<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		if (result.isOK()) {
			processResult(oldResults)

			try {
				val nextStep = preparePinStep()

				return StepActionResult(StepActionResultStatus.NEXT, nextStep)
			} catch (ex: WSHelper.WSException) {
				logger.error(ex) { "Failed to prepare PIN step." }
				return StepActionResult(StepActionResultStatus.CANCEL)
			} catch (ex: InterruptedException) {
				logger.warn(ex) { "CHAT step action interrupted." }
				return StepActionResult(StepActionResultStatus.CANCEL)
			}
		} else {
			// cancel can not happen, so only back is left to be handled
			return StepActionResult(StepActionResultStatus.BACK)
		}
	}

	@Throws(WSHelper.WSException::class, InterruptedException::class)
	private fun preparePinStep(): Step {
		val ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		initContextVars(ctx)
		val nextStep = buildPinStep(ctx)

		return nextStep
	}

	@Throws(WSHelper.WSException::class, InterruptedException::class)
	private fun initContextVars(ctx: DynamicContext) {
		var status = ctx.get(EACProtocol.Companion.PIN_STATUS) as? PinState

		// only process once
		if (status == null) {
			status = PinState()
			val nativePace: Boolean
			val sessHandle = ctx.get(TR03112Keys.SESSION_CON_HANDLE) as ConnectionHandleType
			val cardHandle: ConnectionHandleType
			val paceMarker: PACEMarkerType

			val passwordType = parse(eacData.pinID)

			val ph = PaceCardHelper(addonCtx, sessHandle)
			if (!SysUtils.isMobileDevice()) {
				cardHandle =
					ph.connectCardIfNeeded(
						setOf(ECardConstants.NPA_CARD_TYPE),
					)
				if (passwordType == PasswordID.PIN) {
					val pinState = ph.pinStatus
					status.update(pinState)
				}
				nativePace = ph.isNativePinEntry

				// get the PACEMarker
				paceMarker = ph.getPaceMarker(passwordType!!.name, ECardConstants.NPA_CARD_TYPE)
			} else {
				// mobile device, pick only available reader and proceed
				status.update(PacePinStatus.UNKNOWN)
				cardHandle = ph.getMobileReader()
				nativePace = false
				paceMarker = ph.getPaceMarker(passwordType!!.name, ECardConstants.NPA_CARD_TYPE)
			}

			// save values in dynctx
			ctx.put(EACProtocol.Companion.PIN_STATUS, status)
			ctx.put(EACProtocol.Companion.IS_NATIVE_PACE, nativePace)
			ctx.put(TR03112Keys.CONNECTION_HANDLE, cardHandle)
			ctx.put(EACProtocol.Companion.PACE_MARKER, paceMarker)
		}
	}

	private fun processResult(results: Map<String, ExecutionResults>) {
		val dataGroupsNames = this.dataGroupNames
		val specialFunctionsNames = this.specialFunctionNames
		val executionResults: ExecutionResults = results[stepID]!!

		// process read access and special functions
		val cbRead = executionResults.getResult(CHATStep.Companion.READ_CHAT_BOXES) as? Checkbox
		if (cbRead != null) {
			val selectedCHAT = eacData.selectedCHAT
			for (item in cbRead.getBoxItems()) {
				if (dataGroupsNames.contains(item.name)) {
					selectedCHAT.setReadAccess(item.name, item.isChecked)
				} else if (specialFunctionsNames.contains(item.name)) {
					selectedCHAT.setSpecialFunction(item.name, item.isChecked)
				}
			}
		}

		// process write access
		val cbWrite = executionResults.getResult(CHATStep.Companion.WRITE_CHAT_BOXES) as? Checkbox
		if (cbWrite != null) {
			val selectedCHAT = eacData.selectedCHAT
			for (item in cbWrite.getBoxItems()) {
				if (dataGroupsNames.contains(item.name)) {
					selectedCHAT.setWriteAccess(item.name, item.isChecked)
				}
			}
		}
	}

	private val specialFunctionNames: List<String>
		/**
		 * Returns a list containing the names of all special functions.
		 * @return list containing the names of all special functions.
		 */
		get() {
			return CHAT.SpecialFunction.entries.map { it.name }
		}

	private val dataGroupNames: List<String>
		/**
		 * Returns a list containing the names of all data groups.
		 * @return list containing the names of all data groups.
		 */
		get() {
			return CHAT.DataGroup.entries.map { it.name }
		}

	private fun buildPinStep(ctx: DynamicContext): Step {
		val status = ctx.get(EACProtocol.Companion.PIN_STATUS) as? PinState
		val nativePace = ctx.get(EACProtocol.Companion.IS_NATIVE_PACE) as Boolean
		val paceMarker = ctx.get(EACProtocol.Companion.PACE_MARKER) as PACEMarkerType
		checkNotNull(status)

		if (status.isBlocked) {
			return ErrorStep(
				LANG.translationForKey("step_error_title_blocked", PIN),
				LANG.translationForKey("step_error_pin_blocked", PIN, PIN, PUK, PIN),
				createException(makeResultError(ECardConstants.Minor.IFD.PASSWORD_BLOCKED, "Password blocked.")),
			)
		} else if (status.isDeactivated) {
			return ErrorStep(
				LANG.translationForKey("step_error_title_deactivated"),
				LANG.translationForKey("step_error_pin_deactivated"),
				createException(makeResultError(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED, "Card deactivated.")),
			)
		} else {
			val pinStep = PINStep(eacData, !nativePace, paceMarker)
			val pinAction =
				if (eacData.pinID == PasswordID.CAN.byte) {
					CANStepAction(addonCtx, eacData, !nativePace, pinStep)
				} else {
					PINStepAction(addonCtx, eacData, !nativePace, pinStep)
				}
			pinStep.setAction(pinAction)
			return pinStep
		}
	}
}
