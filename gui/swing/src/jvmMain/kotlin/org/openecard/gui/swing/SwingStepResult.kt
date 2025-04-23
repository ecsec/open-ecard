/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
package org.openecard.gui.swing

import org.openecard.gui.ResultStatus
import org.openecard.gui.StepResult
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.Step
import java.util.concurrent.Exchanger

/**
 * Blocking StepResult implementation for the Swing GUI.
 * After the values are set, the [.synchronize] method can be called, so that any listeners can proceed and
 * fetch the values.
 *
 * @author Tobias Wich
 */
class SwingStepResult
	@JvmOverloads
	constructor(
		private val step: Step?,
		private var status: ResultStatus? = null,
	) : StepResult {
		var syncPoint = Exchanger<Void>()
		private var replacement: Step? = null
		private var results: List<OutputInfoUnit?> = listOf()

		fun setResultStatus(status: ResultStatus) {
			this.status = status
		}

		fun setResult(results: List<OutputInfoUnit?>) {
			this.results = results
		}

		override fun getStep(): Step? = step

		override fun getStepID(): String? = step?.id

		override fun getStatus(): ResultStatus? {
			synchronize()
			return status
		}

		override fun isOK(): Boolean {
			// wait until values are present (blocks until triggered
			synchronize()
			synchronized(this) {
				return getStatus() == ResultStatus.OK
			}
		}

		override fun isBack(): Boolean {
			// wait until values are present
			synchronize()
			synchronized(this) {
				return getStatus() == ResultStatus.BACK
			}
		}

		override fun isCancelled(): Boolean {
			// wait until values are present
			synchronize()
			synchronized(this) {
				return getStatus() == ResultStatus.CANCEL
			}
		}

		override fun isReload(): Boolean {
			// wait until values are present
			synchronize()
			synchronized(this) {
				return getStatus() == ResultStatus.RELOAD
			}
		}

		override fun getResults(): List<OutputInfoUnit?> {
			// wait until values are present
			synchronize()
			synchronized(this) {
				return results
			}
		}

		fun setReplacement(replacement: Step?) {
			this.replacement = replacement
		}

		override fun getReplacement(): Step? = replacement

		private fun synchronize() {
			if (status == null) {
				try {
					syncPoint.exchange(null)
				} catch (ex: InterruptedException) {
					status = ResultStatus.INTERRUPTED
				}
			}
		}
	}
