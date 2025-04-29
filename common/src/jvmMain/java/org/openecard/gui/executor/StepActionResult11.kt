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

import org.openecard.gui.definition.Step

/**
 * Result of a step action.
 * The result contains a status and if desired a replacement step for the next step that would be displayed.
 *
 * @author Tobias Wich
 */
class StepActionResult @JvmOverloads constructor(
    /**
     * Gets the status of the action result.
     *
     * @return The status of the action result.
     */
    @JvmField val status: StepActionResultStatus,
    /**
     * Gets the replacement step for the step referenced by the status.
     *
     * @return The replacement for the referenced step, or null if none is set.
     */
    @JvmField val replacement: Step? = null
) {
    /**
     * Creates a new StepActionResult instance, initializes it with the given status and sets a replacement for the
     * step referenced in the status.
     *
     * @param status The result status of the step action.
     * @param replacement The replacement step for the step which is next according to the action status..
     */
}
