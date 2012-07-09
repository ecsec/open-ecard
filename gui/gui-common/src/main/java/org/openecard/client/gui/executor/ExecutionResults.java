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

import java.util.List;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecutionResults {

    private final String stepID;
    private final List<OutputInfoUnit> results;

    public ExecutionResults(String stepID, List<OutputInfoUnit> results) {
	this.stepID = stepID;
	this.results = results;
    }

    /**
     * @return the stepName
     */
    public String getStepID() {
	return stepID;
    }

    /**
     * @return the results
     */
    public List<OutputInfoUnit> getResults() {
	return results;
    }

    /**
     * Get result element with a specific ID.
     *
     * @param id ID of the result element.
     * @return Result element or null if none exists for the given ID.
     */
    public OutputInfoUnit getResult(String id) {
	for (OutputInfoUnit next : getResults()) {
	    if (next.getID().equals(id)) {
		return next;
	    }
	}
	return null;
    }

}
