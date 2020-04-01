/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.opt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import skid.mob.impl.AttributeSelectionImpl;
import skid.mob.lib.Attribute;
import skid.mob.lib.AttributeSelection;
import skid.mob.lib.Option;
import skid.mob.lib.SelectedOption;


/**
 *
 * @author Tobias Wich
 */
public class SelectedOptionImpl implements SelectedOption {

    private final OptionImpl option;
    private final ArrayList<AttributeSelection> selection;

    SelectedOptionImpl(OptionImpl option) {
	this.option = option;

	this.selection = new ArrayList<>();
	for (Attribute a : option.attributes()) {
	    this.selection.add(new AttributeSelectionImpl(a));
	}
    }

    @Override
    public Option getOption() {
	return option;
    }

    @Override
    public List<AttributeSelection> attributeSelection() {
	return Collections.unmodifiableList(selection);
    }

    @Override
    public void selectAttributes(List<Attribute> userSelection) {
	for (AttributeSelection as : this.selection) {
	    as.select(false);
	}

	for (Attribute a : userSelection) {
	    AttributeSelection s = findMatch(a);
	    if (s != null) {
		s.select(true);
	    }
	}
    }

    private AttributeSelection findMatch(Attribute a) {
	for (AttributeSelection s : selection) {
	    if (s.getName().equals(a.getName())) {
		return s;
	    }
	}

	return null;
    }

}
