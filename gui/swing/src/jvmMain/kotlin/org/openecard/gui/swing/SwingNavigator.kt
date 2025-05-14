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
package org.openecard.gui.swing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.gui.ResultStatus
import org.openecard.gui.StepResult
import org.openecard.gui.UserConsentNavigator
import org.openecard.gui.definition.Step
import org.openecard.gui.swing.common.GUIConstants
import org.openecard.gui.swing.common.NavigationEvent
import java.awt.Container
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

private val LOG = KotlinLogging.logger { }

/**
 * Implementation of the UserConsentNavigator interface for the Swing GUI.
 * This class receives button clicks and orchestrates the update of the steps and progress indication components.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 */
class SwingNavigator(
	private val dialogWrapper: SwingDialogWrapper,
	private val dialogType: String,
	steps: MutableList<Step>,
	private val stepContainer: Container,
	navPanel: NavigationBar,
	stepBar: StepBar,
) : UserConsentNavigator,
	ActionListener {
	private val stepFrames: MutableList<StepFrame>
	private val navBar: NavigationBar
	private val stepBar: StepBar

	private var stepPointer: Int
	private var action: Future<*>? = null

	init {
		this.stepPointer = -1
		this.stepFrames = createStepFrames(steps, dialogType)
		this.navBar = navPanel
		this.stepBar = stepBar

		this.dialogWrapper.show()
	}

	override fun hasNext(): Boolean = stepPointer < (stepFrames.size - 1)

	override fun current(): StepResult {
		stepBar.disableLoaderImage()
		selectIdx(stepPointer)
		val frame = stepFrames[stepPointer]

		// click next button without giving the user the possibility to interfere
		clickIfInstantReturn(frame)

		return frame.getStepResult()
	}

	override fun next(): StepResult {
		stepBar.disableLoaderImage()
		if (hasNext()) {
			selectIdx(stepPointer + 1)
			val frame = stepFrames[stepPointer]

			// click next button without giving the user the possibility to interfere
			clickIfInstantReturn(frame)

			return frame.getStepResult()
		}
		return SwingStepResult(null, ResultStatus.CANCEL)
	}

	override fun previous(): StepResult? {
		stepBar.disableLoaderImage()
		if (stepPointer > 0) {
			selectIdx(stepPointer - 1)
			val frame = stepFrames[stepPointer]

			// click next button without giving the user the possibility to interfere
			clickIfInstantReturn(frame)

			return frame.getStepResult()
		}
		return SwingStepResult(null, ResultStatus.CANCEL)
	}

	override fun replaceCurrent(step: Step): StepResult {
		stepBar.disableLoaderImage()
		stepFrames.removeAt(stepPointer)
		val sf = StepFrame(step, dialogType)
		stepFrames.add(stepPointer, sf)
		selectIdx(stepPointer)

		// click next button without giving the user the possibility to interfere
		clickIfInstantReturn(sf)

		return sf.getStepResult()
	}

	override fun replaceNext(step: Step): StepResult {
		stepBar.disableLoaderImage()
		stepPointer = stepPointer + 1
		if (stepPointer < stepFrames.size) {
			stepFrames.removeAt(stepPointer)
		}

		val sf = StepFrame(step, dialogType)
		stepFrames.add(stepPointer, sf)
		selectIdx(stepPointer)

		// click next button without giving the user the possibility to interfere
		clickIfInstantReturn(sf)

		return sf.getStepResult()
	}

	override fun replacePrevious(step: Step): StepResult {
		stepBar.disableLoaderImage()
		if (stepPointer > 0) {
			stepPointer = stepPointer - 1
			stepFrames.removeAt(stepPointer)
		}

		val sf = StepFrame(step, dialogType)
		stepFrames.add(stepPointer, sf)
		selectIdx(stepPointer)

		// click next button without giving the user the possibility to interfere
		clickIfInstantReturn(sf)

		return sf.getStepResult()
	}

	override fun setRunningAction(action: Future<*>) {
		this.action = action
	}

	override fun close() {
		dialogWrapper.hide()
	}

	private fun createStepFrames(
		steps: List<Step>,
		dialogType: String,
	): ArrayList<StepFrame> {
		val frames = ArrayList<StepFrame>(steps.size)
		for (i in steps.indices) {
			if (i == 0) {
				steps[0].isReversible = false
			}
			val s = steps[i]
			val sf = StepFrame(s, dialogType)
			frames.add(sf)
		}
		return frames
	}

	private fun selectIdx(idx: Int) {
		// Content replacement
		val nextStep = stepFrames[idx]
		stepBar.selectIdx(idx)
		navBar.selectIdx(idx, nextStep.step)
		val nextPanel = nextStep.getPanel()
		nextStep.resetResult()

		stepContainer.removeAll()
		stepContainer.add(nextPanel)
		stepContainer.validate()
		stepContainer.repaint()

		stepPointer = idx

		nextStep.updateFrame()
		nextStep.unlockControls()
		navBar.unlockControls()

		val reversible = nextStep.step.isReversible
		navBar.setReversible(reversible)
	}

	private fun clickIfInstantReturn(frame: StepFrame) {
		if (frame.isInstantReturn) {
			val command = GUIConstants.BUTTON_NEXT
			val e = ActionEvent(frame.step, ActionEvent.ACTION_PERFORMED, command)
			// create async invocation of the action
			object : Thread("Instant-Return-Thread-" + THREAD_NUM.getAndIncrement()) {
				override fun run() {
					actionPerformed(e)
				}
			}.start()
		}
	}

	override fun actionPerformed(e: ActionEvent) {
		LOG.debug { "Received event: ${e.getActionCommand()}" }

		val event: NavigationEvent? = NavigationEvent.Companion.fromEvent(e)
		if (event == null) {
			error { "Unknown event received: ${e.getActionCommand()}" }
			return
		}

		// in case the user wants to proceed check if all components are valid
		val curStep = stepFrames[stepPointer]
		if (event == NavigationEvent.NEXT && !curStep.validateComponents()) {
			LOG.debug { "Validation of components failed." }
			return
		}

		// in case there is a running action, kill it and bail out
		if (action != null && !action!!.isDone) {
			LOG.debug { "Canceling execution of the currently running StepAction." }
			action!!.cancel(true)
			return
		}

		// lock controls and update current step result
		stepBar.enableLoaderImage()
		navBar.lockControls()
		curStep.lockControls()
		curStep.updateResult(event)
	}
}

private val THREAD_NUM = AtomicInteger(1)
