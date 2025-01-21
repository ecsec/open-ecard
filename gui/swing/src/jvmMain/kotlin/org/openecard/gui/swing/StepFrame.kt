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
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.Step
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.gui.swing.common.NavigationEvent
import org.openecard.gui.swing.components.Focusable
import org.openecard.gui.swing.components.StepComponent
import org.openecard.gui.swing.steplayout.StepLayouter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import javax.swing.JPanel

 private val LOG = KotlinLogging.logger {  }

/**
 * The StepFrame class represents a single step.
 * The actual layouting is however deferred to a layouting component.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Florian Feldmann
 */
class StepFrame(val step: Step, private val dialogType: String) {

    private val panel: JPanel = JPanel()
	private var stepResult: SwingStepResult
    private var components: MutableList<StepComponent> = mutableListOf()

    private var bgThread: Thread? = null

    init {
		this.stepResult = SwingStepResult(step)
        initLayout()
    }

    private fun initLayout() {
        panel.setLayout(BorderLayout())
    }

    private fun initComponents() {
        val stepLayouter: StepLayouter = StepLayouter.Companion.create(step.getInputInfoUnits(), dialogType, step.title)
        val contentPanel = stepLayouter.panel
        panel.add(contentPanel, BorderLayout.CENTER)

        components = stepLayouter.components
    }

    fun resetResult() {
        stepResult = SwingStepResult(step)
    }

    val isInstantReturn: Boolean
        get() = step.isInstantReturn

    fun getPanel(): Container {
        revalidate(panel)
        return panel
    }

    /**
     * Check if all components on the frame are valid. This can be used to see
     * if a jump to the next frame can be made.
     *
     * @return True if all components are valid, false otherwise.
     */
    fun validateComponents(): Boolean {
        for (next in components) {
            val component = next.component
            if (next.isValueType && !next.validate()) {
                component.setBackground(Color.RED)
                return false
            }
            component.setBackground(null)
        }
        return true
    }

    val resultContent: MutableList<OutputInfoUnit?>
        /**
         * Get result for all components on the frame that support result values.
         *
         * @return List containing all result values. As a matter of fact this list can be empty.
         */
        get() {
            val result = mutableListOf<OutputInfoUnit?>()
            for (next in components) {
                if (next.isValueType) {
                    result.add(next.value)
                }
            }
            return result
        }

    fun updateFrame() {
        panel.removeAll()
        initComponents()
        revalidate(panel)
        setFocus()
        runBackgroundTask()
    }

    fun getStepResult(): StepResult {
        return stepResult
    }


    private fun revalidate(c: Container) {
        for (i in 0..<c.componentCount) {
            val next = c.getComponent(i)
            if (next is Container) {
                this.revalidate(next)
            } else {
                this.revalidate(next)
            }
        }
        c.revalidate()
        c.repaint()
    }

    private fun revalidate(c: Component) {
        c.revalidate()
        c.repaint()
    }

    private fun setFocus() {
        for (next in components) {
            if (next is Focusable) {
                (next as Focusable).setFocus()
                return
            }
        }
    }


    /**
     * Locks elements on the frame, so they can not be modified anymore.
     * This is needed when executing an action. That is the time between a button click and the update of the frame
     * panel.
     */
    fun lockControls() {
        // TODO: lock all elements
    }

    /**
     * Unlocks elements on this frame, so that they can be modified.
     * This is needed to unlock the frame when it is displayed after it has been locked by an action.
     */
    fun unlockControls() {
        // TODO: unlock elements of this frame
    }

    /**
     * Updates the StepResult when a button is clicked.
     * Before a button is clicked by the user, the [org.openecard.gui.executor.ExecutionEngine] waits for the
     * result content by calling [StepResult.getStatus]. This method sets the portions of the result relevant
     * for the respective button event and unlocks the getStatus method.
     *
     * @param event Event describing which button has been clicked.
     */
    fun updateResult(event: NavigationEvent) {
        killBackgroundTask()

        // update issued result
        when (event) {
            NavigationEvent.BACK -> {
                stepResult.setResult(this.resultContent)
                stepResult.setResultStatus(ResultStatus.BACK)
            }

            NavigationEvent.NEXT -> {
                stepResult.setResult(this.resultContent)
                stepResult.setResultStatus(ResultStatus.OK)
            }

            NavigationEvent.CANCEL -> {
				stepResult.setResultStatus(ResultStatus.CANCEL)
			}
        }

        try {
			LOG.debug { "Exchange result for step '${step.title}'." }
            stepResult.syncPoint.exchange(null)
        } catch (ignore: InterruptedException) {
        }
    }

    private fun forceResult(result: StepActionResult) {
        // update issued result
        stepResult.setReplacement(result.replacement)
        when (result.status) {
            StepActionResultStatus.BACK -> {
                stepResult.setResult(this.resultContent)
                stepResult.setResultStatus(ResultStatus.BACK)
            }

            StepActionResultStatus.NEXT -> {
                stepResult.setResult(this.resultContent)
                stepResult.setResultStatus(ResultStatus.OK)
            }

            StepActionResultStatus.REPEAT -> {
                stepResult.setResult(this.resultContent)
                stepResult.setResultStatus(ResultStatus.RELOAD)
            }

            StepActionResultStatus.CANCEL -> {
				stepResult.setResultStatus(ResultStatus.CANCEL)
			}

            else -> {
				stepResult.setResultStatus(ResultStatus.CANCEL)
			}
        }

        try {
			LOG.debug { "Exchange result from background task for step '${step.title}'." }
            stepResult.syncPoint.exchange(null)
        } catch (ignore: InterruptedException) {
        }
    }

    fun killBackgroundTask() {
		LOG.debug { "Trying to kill background task if it exists." }
        // kill running thread
        bgThread?.let {
			if (it.isAlive) {
				LOG.debug { "Killing background task." }
				it.interrupt()
			}
		}
    }

    private fun runBackgroundTask() {
        val task = step.backgroundTask
        if (task != null) {
            bgThread = Thread(object : Runnable {
                override fun run() {
                    try {
                        val result = task.call()
						LOG.debug { "Background thread terminated before the GUI." }
                        forceResult(result)
                    } catch (ex: InterruptedException) {
						LOG.debug(ex) { "Background task has been terminated from the Swing GUI." }
                    } catch (ex: Exception) {
						LOG.error(ex) { "Background task terminated with an exception." }
                    }
                }
            }, "Swing-GUI-BG-Task").apply {
				isDaemon = true
				start()
			}
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
