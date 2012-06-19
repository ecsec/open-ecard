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

package org.openecard.client.gui.executor;

import org.openecard.client.gui.definition.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepActionResult {

    private final StepActionResultStatus status;
    private final Step replacement;

    public StepActionResult(StepActionResultStatus status) {
	this(status, null);
    }

    public StepActionResult(StepActionResultStatus status, Step replacement) {
	this.status = status;
	this.replacement = replacement;
    }

    /**
     * @return the status
     */
    public StepActionResultStatus getStatus() {
	return status;
    }

    /**
     * @return the replacement
     */
    public Step getReplacement() {
	return replacement;
    }

}
