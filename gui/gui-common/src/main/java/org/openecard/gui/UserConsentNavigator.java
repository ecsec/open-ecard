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

import java.util.concurrent.Future;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionEngine;


/**
 * Navigator interface for use in the {@link ExecutionEngine}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface UserConsentNavigator {

    boolean hasNext();

    StepResult current();
    StepResult next();
    StepResult previous();

    StepResult replaceCurrent(Step step);
    StepResult replaceNext(Step step);
    StepResult replacePrevious(Step step);

    /**
     * Sets the action in the navigator which is executed after calling this method.
     * The action can be canceled from within the navigator if needed.
     *
     * @param action Future of the StepAction that is executed next.
     */
    void setRunningAction(Future action);

    /**
     * Closes the open dialog.
     */
    void close();

}
