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

import skid.mob.lib.Option;
import skid.mob.lib.SelectedOption;


/**
 *
 * @author Tobias Wich
 */
public class SelectedOptionImpl implements SelectedOption {

    private final OptionImpl option;

    SelectedOptionImpl(OptionImpl option) {
	this.option = option;
    }

    @Override
    public Option getOption() {
	return option;
    }

}
