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

package org.openecard.gui.definition;


/**
 * Base interface every info unit must implement.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface InfoUnit {

    /**
     * Get type of info unit.
     */
    InfoUnitElementType type();

    /**
     * Get ID of the info unit.
     * The id must be unique per step.
     */
    String getID();
    /**
     * Set ID of the info unit.
     * The id must be unique per step.
     */
    void setID(String id);

    /**
     * Copy the content of the given info unit to this instance.
     * Both instances must have the same type. If the types differ, a warning is logged and
     * the copy is not performed.
     *
     * @param origin InfoUnit to copy from.
     */
    void copyContentFrom(final InfoUnit origin);

}
