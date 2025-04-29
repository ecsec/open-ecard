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
package org.openecard.gui.executor

import org.openecard.gui.definition.OutputInfoUnit

/**
 * Class wrapping the results of one step.
 * The result contains the status as well as the elements results.
 *
 * @author Tobias Wich
 */
class ExecutionResults(
    /**
     * Gets the ID of the associated step.
     *
     * @return The step ID.
     */
    val stepID: String,
    /**
     * Gets all element result.
     *
     * @return The results of the step.
     */
    val results: List<OutputInfoUnit>
) {
    /**
     * Gets element result with a specific element ID.
     *
     * @param id ID of the result element.
     * @return Result element or null if none exists for the given ID.
     */
    fun getResult(id: String): OutputInfoUnit? {
        for (next in results) {
            if (next.id == id) {
                return next
            }
        }
        return null
    }
}
