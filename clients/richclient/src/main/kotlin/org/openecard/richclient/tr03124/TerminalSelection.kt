/*
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.tr03124

import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.sc.WaitForCardType.waitForCard
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.withTerminalOnlyConnect

object TerminalSelection {
	fun EacProcessState.trySelectPinPadTerminal() {
		val state = this
		// see if there is an npa connected
		val npaTerminal =
			state.cardWatcher.cardState.recognizedCards.find { it.cardType == NpaCif.metadata.id }?.let { npaEntry ->
				state.terminalName = npaEntry.terminal
				state.terminals.getTerminal(npaEntry.terminal)
			}

		if (npaTerminal != null) {
			// card is already inserted
			this.uiStep.getPaceDid(npaTerminal.name).also { pace ->
				this.nativePace = pace.capturePasswordInHardware()
				this.terminalName = npaTerminal.name
				this.paceDid = pace
				this.status = pace.passwordStatus()
			}
		} else {
			// check terminal(s) for native pace
			state.terminals
				.list()
				.find { t ->
					runCatching {
						t.withTerminalOnlyConnect { con ->
							con.getFeatures().any { it is PaceFeature }
						}
					}.getOrDefault(false)
				}?.let { t ->
					// use this reader for the eac process
					state.terminalName = t.name
					state.nativePace = true
				}
		}
	}

	suspend fun EacProcessState.waitForNpa(): PaceDid {
		paceDid?.let {
			// we already have a connected terminal
			return it
		}

		val terminal =
			this.terminalName?.let {
				// we already have selected a terminal
				val hasNpa =
					this.cardWatcher.cardState.recognizedCards.any {
						it.terminal == terminalName &&
							it.cardType == NpaDefinitions.cardType
					}
				if (hasNpa) {
					// npa is already present
					terminalName
				} else {
					// wait for npa to show up
					cardWatcher.waitForCard(NpaDefinitions.cardType).first()
				}
			} ?: run {
				// we don't have a terminal, so wait for one
				cardWatcher.waitForCard(NpaDefinitions.cardType).first()
			}

		// terminal has been configured, obtain pace did
		return this.uiStep.getPaceDid(terminal).also { pace ->
			this.nativePace = pace.capturePasswordInHardware()
			this.terminalName = terminal
			this.paceDid = pace
			this.status = pace.passwordStatus()
		}
	}
}
