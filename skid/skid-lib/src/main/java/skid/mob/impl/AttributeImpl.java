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

import skid.mob.impl.client.model.RequestedAttribute;
import skid.mob.lib.Attribute;


/**
 *
 * @author Tobias Wich
 */
public class AttributeImpl implements Attribute {

    private final String name;
    private final boolean isRequired;

    public AttributeImpl(Attribute origin) {
	this(origin.getName(), origin.isRequired());
    }

    public AttributeImpl(String name, boolean isRequired) {
	this.name = name;
	this.isRequired = isRequired;
    }

    public static Attribute fromRequestedAttribute(RequestedAttribute ra) {
	return new AttributeImpl(ra.getName(), ra.isRequired());
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public boolean isRequired() {
	return isRequired;
    }

}
