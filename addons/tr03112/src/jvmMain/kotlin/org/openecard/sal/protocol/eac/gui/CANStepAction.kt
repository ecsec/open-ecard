/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse
import org.openecard.addon.Context
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.gui.StepResult
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.i18n.I18N
import org.openecard.sal.protocol.eac.EACData
import org.openecard.sal.protocol.eac.EACProtocol

private val logger = KotlinLogging.logger { }

/**
 * StepAction for capturing the user PIN on the EAC GUI.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class CANStepAction(
	addonCtx: Context,
	eacData: EACData,
	capturePin: Boolean,
	step: PINStep,
) : AbstractPasswordStepAction(addonCtx, eacData, capturePin, step) {
	override fun perform(
		oldResults: Map<String, ExecutionResults>,
		result: StepResult,
	): StepActionResult {
		try {
			var conHandle = this.ctx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType?
			val ph = PaceCardHelper(addonCtx, conHandle!!)
			conHandle = ph.connectCardIfNeeded(setOf(ECardConstants.NPA_CARD_TYPE))
			this.ctx.put(TR03112Keys.CONNECTION_HANDLE, conHandle)

			val establishChannelResponse = performPACEWithPIN(oldResults, conHandle)

			if (establishChannelResponse.getResult().getResultMajor() == ECardConstants.Major.ERROR) {
				if (establishChannelResponse
						.getResult()
						.getResultMinor() == ECardConstants.Minor.IFD.AUTHENTICATION_FAILED
				) {
					// repeat the step
					logger.info { "Wrong CAN entered, trying again." }
					return StepActionResult(StepActionResultStatus.REPEAT)
				} else {
					checkResult<EstablishChannelResponse>(establishChannelResponse)
				}
			}

			eacData.paceResponse = establishChannelResponse
			// PACE completed successfully, proceed with next step
			ctx.put(EACProtocol.Companion.PACE_EXCEPTION, null)
			return StepActionResult(StepActionResultStatus.NEXT)
		} catch (ex: WSHelper.WSException) {
			// This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
			if (ex.resultMinor == ECardConstants.Minor.IFD.CANCELLATION_BY_USER) {
				logger.error(ex) { "User canceled the authentication manually." }
				return StepActionResult(StepActionResultStatus.CANCEL)
			}

			// for people which think they have to remove the card in the process
			if (ex.resultMinor == ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE) {
				logger.error(ex) { "The SlotHandle was invalid so probably the user removed the card or an reset occurred." }
				return StepActionResult(
					StepActionResultStatus.REPEAT,
					ErrorStep(
						I18N.strings.pinplugin_action_error_title.localized(),
						I18N.strings.pinplugin_action_error_card_removed.localized(),
						ex,
					),
				)
			}

			// repeat the step
			logger.error { "An unknown error occurred while trying to verify the PIN." }
			return StepActionResult(
				StepActionResultStatus.REPEAT,
				ErrorStep(
					I18N.strings.pinplugin_action_error_title.localized(),
					I18N.strings.pinplugin_action_error_unknown.localized(),
					ex,
				),
			)
		} catch (ex: InterruptedException) {
			logger.warn(ex) { "CAN step action interrupted." }
			return StepActionResult(StepActionResultStatus.CANCEL)
		} catch (ex: PinOrCanEmptyException) {
			logger.warn(ex) { "CAN was empty" }
			return StepActionResult(StepActionResultStatus.REPEAT)
		}
	}
}
