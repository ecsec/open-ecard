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
 */
package org.openecard.gui.executor

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ThreadTerminateException
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked
import org.openecard.gui.ResultStatus
import org.openecard.gui.UserConsentNavigator
import org.openecard.gui.definition.InputInfoUnit
import org.openecard.gui.replacement
import org.openecard.gui.results
import org.openecard.gui.status
import org.openecard.gui.step
import org.openecard.gui.stepID
import java.util.Collections
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger { }

/**
 * Class capable of displaying and executing a user consent. <br></br>
 * This class is a helper to display the steps of a user consent. It displays one after the other and reacts differently
 * depending of the outcome of a step. It also executes actions associated with the steps after they are finished.
 *
 * @author Tobias Wich
 */
class ExecutionEngine(
	private val navigator: UserConsentNavigator,
) {
	private val _results = mutableMapOf<String, ExecutionResults>()
	val results: Map<String, ExecutionResults>
		/**
		 * Get all step results of the execution.
		 *
		 * @return Mapping of the step results with step ID as key.
		 */
		get() {
			return Collections.unmodifiableMap(results)
		}

	/**
	 * Processes the user consent associated with this instance. <br></br>
	 * The following algorithm is used to process the dialog.
	 *
	 *  1. Display the first step.
	 *  1. Evaluate step result. Break execution on CANCEL.
	 *  1. Execute step action. Break execution on CANCEL.
	 *  1. Display either next previous or current step, or a replacement according to result.
	 *  1. Proceed with point 2.
	 *
	 *
	 * @return Overall result of the execution.
	 * @throws ThreadTerminateException Thrown in case the GUI has been closed externally (interrupted).
	 */
	fun process(): ResultStatus {
		try {
			var next = navigator.next() // get first step
			// loop over steps. break inside loop
			while (true) {
				val result = next?.status
				logger.debug { "${"Step {} finished with result {}."} ${next?.stepID} $result" }
				// close dialog on cancel and interrupt
				if (result == ResultStatus.INTERRUPTED || Thread.currentThread().isInterrupted) {
					throw ThreadTerminateException("GUI has been interrupted.")
				} else if (result == ResultStatus.CANCEL) {
					return result
				}

				// get result and put it in resultmap
				val stepResults = next!!.results
				val oldResults = Collections.unmodifiableMap(_results)
				_results[next.stepID!!] = ExecutionResults(next.stepID!!, stepResults)

				// replace InfoInputUnit values in live list
				if (!next.step?.isResetOnLoad!!) {
					val s = next.step
					val inputInfo = s?.inputInfoUnits
					val infoMap: MutableMap<String?, InputInfoUnit> = HashMap()
					// create index over infos
					for (nextInfo in inputInfo!!) {
						infoMap[nextInfo.id] = nextInfo
					}
					for (nextOut in stepResults) {
						val matchingInfo = infoMap[nextOut.id]
						// an entry must exist, otherwise this is an error in the GUI implementation
						// this type of error should be found in tests
						matchingInfo!!.copyContentFrom(nextOut)
					}
				}

				// replace step if told by result value
				val replaceStep = next.replacement
				if (replaceStep != null) {
					logger.debug { "${replaceStep.id} ${{ "Replacing with step.id={}." }}" }
					when (next.status) {
						ResultStatus.BACK -> next = navigator.replacePrevious(replaceStep)
						ResultStatus.OK ->
							if (navigator.hasNext()) {
								next = navigator.replaceNext(replaceStep)
							} else {
								return convertStatus(StepActionResultStatus.NEXT)
							}

						ResultStatus.RELOAD -> next = navigator.replaceCurrent(replaceStep)
						else -> {}
					}
				} else {
					// step replacement did not happen, so we can execute the action
					val action = next.step!!.action
					val actionCallable = StepActionCallable(action, oldResults, next)
					// use separate thread or tasks running outside the JVM context, like PCSC calls, won't stop on cancellation
					val execService = Executors.newSingleThreadExecutor()
					val actionFuture = execService.submit(actionCallable)
					navigator.setRunningAction(actionFuture)
					val actionResult: StepActionResult
					try {
						actionResult = actionFuture.get()!!
						logger.debug { "${"Step Action {} finished with result {}."} ${action.stepID} ${actionResult.status}" }
					} catch (ex: CancellationException) {
						logger.info(ex) { "StepAction was canceled." }
						return ResultStatus.CANCEL
					} catch (ex: InterruptedException) {
						logger.info(ex) { "StepAction was interrupted." }
						navigator.close()
						throw ThreadTerminateException("GUI has been interrupted.")
					} catch (ex: ExecutionException) {
						// there are some special kinds we need to handle here
						if (ex.cause is InvocationTargetExceptionUnchecked) {
							val iex = ex.cause as InvocationTargetExceptionUnchecked?
							if (iex!!.cause is ThreadTerminateException) {
								logger.info(ex) { "StepAction was interrupted." }
								throw ThreadTerminateException("GUI has been interrupted.")
							}
						}
						// all other types
						logger.error(ex.cause) { "StepAction failed with error." }
						return ResultStatus.CANCEL
					}

					// break out if cancel was returned
					if (actionResult.status == StepActionResultStatus.CANCEL) {
						logger.info { "StepAction was canceled." }
						return ResultStatus.CANCEL
					}

					// replace step if told by result value
					val actionReplace = actionResult.replacement
					if (actionReplace != null) {
						logger.debug { "${actionReplace.id} ${{ "Replacing after action with step.id={}." }}" }
						when (actionResult.status) {
							StepActionResultStatus.BACK -> next = navigator.replacePrevious(actionReplace)
							StepActionResultStatus.NEXT ->
								if (navigator.hasNext()) {
									next = navigator.replaceNext(actionReplace)
								} else {
									return convertStatus(StepActionResultStatus.NEXT)
								}

							StepActionResultStatus.REPEAT -> next = navigator.replaceCurrent(actionReplace)
							else -> {}
						}
					} else {
						// no replacement just proceed
						when (actionResult.status) {
							StepActionResultStatus.BACK -> next = navigator.previous()
							StepActionResultStatus.NEXT ->
								if (navigator.hasNext()) {
									next = navigator.next()
								} else {
									return convertStatus(StepActionResultStatus.NEXT)
								}

							StepActionResultStatus.REPEAT -> next = navigator.current()
							else -> {}
						}
					}
				}
			}
		} finally {
			logger.debug { "Closing UserConsentNavigator." }
			navigator.close()
		}
	}

	private fun convertStatus(`in`: StepActionResultStatus): ResultStatus =
		when (`in`) {
			StepActionResultStatus.BACK -> ResultStatus.BACK
			StepActionResultStatus.NEXT -> ResultStatus.OK
			else -> ResultStatus.OK // repeat undefined for this kind of status
		}
}
