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

package org.openecard.gui;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;


/**
 * Interface for a step result with status and result values.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface StepResult {

    /**
     * Get step definition of the matching step.
     *
     * @return The step instance which belongs to this result.
     */
    Step getStep();
    /**
     * Get ID value of the matching step.
     *
     * @return ID of the step.
     */
    String getStepID();

    /**
     * Get result status of the step this result belongs to.
     * The invocation of this function may block, if the result is not ready at the time this function is called. In the
     * {@link org.openecard.gui.executor.ExecutionEngine}, the StepResult is returned right after calling the
     * {@link UserConsentNavigator#next()} function. After that, the results status is checked, but the user might not
     * have produced a result yet.
     *
     * @return Result status of the step.
     */
    ResultStatus getStatus();

    /**
     * Convenience method for {@link #getStatus()} with check if the status is {@link ResultStatus#OK}.
     *
     * @return {@code true} if result status is OK, {@code false} otherwise.
     */
    boolean isOK();
    /**
     * Convenience method for {@link #getStatus()} with check if the status is {@link ResultStatus#BACK}.
     *
     * @return {@code true} if result status is BACK, {@code false} otherwise.
     */
    boolean isBack();
    /**
     * Convenience method for {@link #getStatus()} with check if the status is {@link ResultStatus#CANCEL}.
     *
     * @return {@code true} if result status is CANCEL, {@code false} otherwise.
     */
    boolean isCancelled();
    /**
     * Convenience method for {@link #getStatus()} with check if the status is {@link ResultStatus#RELOAD}.
     *
     * @return {@code true} if result status is RELOAD, {@code false} otherwise.
     */
    boolean isReload();

    /**
     * Return result values of all OutputInfoUnits of the step this result belongs to.
     * This method blocks if the dialog is still displayed. The blocking behaviour is defined in the documentation for
     * {@code getStatus}.
     *
     * @see #getStatus()
     * @return List containing all results of the OutputInfoUnits of this step.
     */
    @Nonnull
    List<OutputInfoUnit> getResults();

    /**
     * Returns the replacement step for the next display.
     * This method blocks if the dialog is still displayed. The blocking behaviour is defined in the documentation for
     * {@code getStatus}.
     *
     * @return The replacement step, or {@code null} if no replacement should be performed.
     */
    @Nullable
    Step getReplacement();

}
