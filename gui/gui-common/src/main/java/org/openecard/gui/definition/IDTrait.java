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

import org.openecard.common.util.ValueGenerators;


/**
 * Class implementing the ID portion common to all {@code InfoUnit)s.
 * This class creates IDs if none are given and provides an implementation of the ID getter and setter.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class IDTrait implements InfoUnit {

    private String id;

    /**
     * Creates an IDTrait instance and initializes its ID to a generated value.
     */
    public IDTrait() {
	this(ValueGenerators.genBase64Session(16));
    }

    /**
     * Creates an IDTrait instance and initializes its ID to the given value.
     *
     * @param id The ID with which this instance will be initialized.
     */
    public IDTrait(String id) {
	this.id = id;
    }


    @Override
    public final String getID() {
	return id;
    }
    @Override
    public final void setID(String id) {
	this.id = id;
    }

}
