/****************************************************************************
 * Copyright (C) 2015-2016 ecsec GmbH.
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
package org.openecard.addons.tr03124.gui

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.addon.Context
import org.openecard.gui.StepResult
import org.openecard.gui.definition.Radiobox
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * StepAction implementation which processes the results of a previous executed CardSelectionStep.
 *
 * @author Hans-Martin Haase
 */
class CardSelectionAction
/**
 * Creates a new CardSelectionAction from the given step and the card represented by a list of Connection Handles.
 *
 * @param step Step used to initialize the action.
 * @param avCard List of [ConnectionHandleType] objects which represents the available credentials.
 * @param types Types which may be used for the authentication.
 * @param ctx Addon context used to communicate with the core.
 */(
    private val step: CardSelectionStep,
    private val avCard: MutableList<ConnectionHandleType>,
    private val types: MutableList<String?>,
    private val ctx: Context
) : StepAction(step) {
    private var resultCardTypetName: String? = null

    override fun perform(oldResults: MutableMap<String?, ExecutionResults>, result: StepResult): StepActionResult {
        if (result.isOK()) {
            val results: ExecutionResults = oldResults.get(getStepID())!!
            val out = results.getResult("credentialSelectionBox")
            val rBox = out as Radiobox

            for (item in rBox.getBoxItems()) {
                if (item.isChecked()) {
                    this.resultCardTypetName = item.getName()
                    break
                }
            }

            if (resultCardTypetName != null) {
                return StepActionResult(StepActionResultStatus.NEXT)
            } else {
                return StepActionResult(StepActionResultStatus.REPEAT)
            }
        } else {
            // user has added or removed a card
            if (result.isReload()) {
                updateCards()
                step.update(avCard)
                if (avCard.isEmpty()) {
                    return StepActionResult(StepActionResultStatus.CANCEL)
                }
                return StepActionResult(StepActionResultStatus.REPEAT)
            }
            // user has canceled the dialog so return that
            return StepActionResult(StepActionResultStatus.CANCEL)
        }
    }

    val result: ConnectionHandleType?
        /**
         * Get the ConnectionHandleTyp object of the chosen credential.
         *
         * @return The [ConnectionHandleType] corresponding to the selected credential or `NULL` if the user
         * canceled the dialog.
         */
        get() {
            for (handle in avCard) {
                if (handle.getRecognitionInfo().getCardType() == resultCardTypetName) {
                    return handle
                }
            }

            return null
        }

    /**
     * Update the list of available and fitting cards.
     */
    private fun updateCards() {
        avCard.clear()
        val cap = CardApplicationPath()
        cap.setCardAppPathRequest(CardApplicationPathType())
        val resp = ctx.getDispatcher().safeDeliver(cap) as CardApplicationPathResponse
        val cards = resp.getCardAppPathResultSet().getCardApplicationPathResult()
        for (path in cards) {
            val connect = CardApplicationConnect()
            connect.setCardApplicationPath(path)
            val conResp = ctx.getDispatcher().safeDeliver(connect) as CardApplicationConnectResponse
            if (types.contains(conResp.getConnectionHandle().getRecognitionInfo().getCardType())) {
                avCard.add(conResp.getConnectionHandle())
            } else {
                val disconnect = CardApplicationDisconnect()
                disconnect.setConnectionHandle(conResp.getConnectionHandle())
                ctx.getDispatcher().safeDeliver(disconnect)
            }
        }
    }

    companion object {
        private val LOG: Logger? = LoggerFactory.getLogger(CardSelectionAction::class.java)
    }
}
