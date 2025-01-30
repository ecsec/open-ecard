/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.gui.executor;

import java.util.concurrent.Callable;
import org.openecard.gui.UserConsentNavigator;


/**
 * Background task for execution in steps.
 * A background task can get executed in parallel to the display of a step. In case the background task is finished
 * before the user closes the step with any of the buttons, the user consent must translate the result of the task
 * ({@link StepActionResult}) to a result of the step ({@link org.openecard.gui.StepResult}) and return it to the caller
 * of the navigators function (e.g. {@link UserConsentNavigator#next()}). In case the step is closed by the user, then
 * the background task is aborted and the usual procedure is followed.
 *
 * @author Tobias Wich
 */
@FunctionalInterface
public interface BackgroundTask extends Callable<StepActionResult> {

}
