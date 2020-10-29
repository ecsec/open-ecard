/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.Step;


/**
 * Action returning a CANCEL result.
 * This action is helpful in error screens, where a cancel shall be returned after the error has been displayed to the
 * user.
 *
 * @author Tobias Wich
 */
public class CancelAction extends StepAction {

    public CancelAction(Step step) {
	super(step);
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	return new StepActionResult(StepActionResultStatus.CANCEL);
    }

}
