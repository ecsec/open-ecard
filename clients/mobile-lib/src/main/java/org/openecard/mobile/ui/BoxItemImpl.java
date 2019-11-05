/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mobile.ui;

import org.openecard.mobile.activation.SelectableItem;


/**
 *
 * @author Tobias Wich
 */
public class BoxItemImpl implements SelectableItem {

    private final String name;
    private final boolean required;
    private final String text;
    private boolean checked;

    public BoxItemImpl(String name, boolean checked, boolean required, String text) {
	this.name = name;
	this.checked = checked;
	this.required = required;
	this.text = text;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getText() {
	return text;
    }

    @Override
    public boolean isChecked() {
	return checked;
    }

    @Override
    public void setChecked(boolean checked) {
	this.checked = checked;
    }

    @Override
    public boolean isRequired() {
	return required;
    }

}
