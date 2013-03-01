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
 * Radiobox element for user consents.
 * Any radio box item can be checked or unchecked, but only one at a time can be checked.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class Radiobox extends AbstractBox {

    /**
     * Creates a new Radiobox instance and initializes it with the given ID.
     *
     * @param id The ID to initialize the instance with.
     */
    public Radiobox(String id) {
	super(id);
    }

    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.RADIO_BOX;
    }

}
