/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl;

import skid.mob.lib.Attribute;
import skid.mob.lib.AttributeSelection;


/**
 *
 * @author Tobias Wich
 */
public class AttributeSelectionImpl extends AttributeImpl implements AttributeSelection {

    private boolean isSelected;

    public AttributeSelectionImpl(Attribute origin) {
	super(origin);
	this.isSelected = true;
    }

    @Override
    public boolean isSelected() {
	return isSelected;
    }

    @Override
    public void select(boolean selectValue) {
	if (! isRequired()) {
	    isSelected = selectValue;
	}
    }

}
