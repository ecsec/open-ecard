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
 */
package org.openecard.gui

import org.openecard.gui.definition.Step
import java.util.concurrent.Future

/**
 * Navigator interface to control the execution of the user consent.
 * This interface is used in the [org.openecard.gui.executor.ExecutionEngine].
 *
 * @author Tobias Wich
 */
interface UserConsentNavigator {
    /**
     * Checks whether there is a successor step to display.
     *
     * @return `true` if a successor step can be displayed, `false` otherwise.
     */
    fun hasNext(): Boolean

    /**
     * Redisplays current dialog step.
     *
     * @return Result of the step. The content of the result is available after the step is finished.
     */
    fun current(): StepResult?

    /**
     * Displays next dialog step.
     *
     * @return Result of the step. The content of the result is available after the step is finished.
     */
    fun next(): StepResult?

    /**
     * Displays previous dialog step.
     *
     * @return Result of the step. The content of the result is available after the step is finished.
     */
    fun previous(): StepResult?

    /**
     * Replaces the current step in the step list and displays the new step.
     *
     * @param step Replacement for the current step.
     * @return Result of the step. The content of the result is available after the step is finished.
     */
    fun replaceCurrent(step: Step?): StepResult?

    /**
     * Replaces the next step in the step list and displays the new step.
     *
     * @param step Replacement for the next step.
     * @return Result of the step. The content of the result is available after the step is finished.
     */
    fun replaceNext(step: Step?): StepResult?

    /**
     * Replaces the previous step in the step list and displays the new step.
     *
     * @param step Replacement for the previous step.
     * @return Result of the step. The content of the result is available after the step is finished.
     */
    fun replacePrevious(step: Step?): StepResult?

    /**
     * Sets the action in the navigator which is executed after calling this method.
     * The action can be canceled from within the navigator if needed.
     *
     * @param action Future of the StepAction that is executed next.
     */
    fun setRunningAction(action: Future<*>?)

    /**
     * Closes the open dialog.
     */
    fun close()
}
