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
import java.util.List;
import skid.mob.impl.AttributeImpl;
import skid.mob.impl.client.InvalidServerData;
import skid.mob.impl.client.model.AuthOption;
import skid.mob.impl.client.model.RequestedAttribute;
import skid.mob.impl.client.model.RequestedAttributes;
import skid.mob.lib.ActivationType;
import skid.mob.lib.Attribute;
import skid.mob.lib.Option;
import skid.mob.lib.SelectedOption;


/**
 *
 * @author Tobias Wich
 */
public class OptionImpl implements Option {

    private final AuthOption ao;
    private boolean loaded = false;
    private ActivationType actType;

    public OptionImpl(AuthOption ao) {
	this.ao = ao;
    }

    public synchronized void load() throws InvalidServerData {
	if (! loaded) {
	    this.actType = actTypeFromString(ao.activationType());

	    if (ao.requestedAttributes() == null) {
		throw new InvalidServerData("No RequestedAttributes defined for the AuthOption.");
	    }

	    this.loaded = true;
	}
    }

    @Override
    public SelectedOption createSelection() {
	return new SelectedOptionImpl(this);
    }

    @Override
    public String optionId() {
	return ao.optionId();
    }

    @Override
    public String type() {
	return ao.type();
    }

    @Override
    public String protocol() {
	return ao.protocol();
    }

    @Override
    public String issuer() {
	return ao.issuer();
    }

    @Override
    public ActivationType activationType() {
	return actType;
    }

    @Override
    public List<Attribute> attributes() {
	ArrayList<Attribute> result = new ArrayList<>();

	RequestedAttributes ras = ao.requestedAttributes();
	for (RequestedAttribute ra : ras.getReqAttrs()) {
	    Attribute a = AttributeImpl.fromRequestedAttribute(ra);
	    result.add(a);
	}

	return result;
    }

    private static ActivationType actTypeFromString(String actType) throws InvalidServerData {
	// prevent NPE
	actType = actType == null ? "" : actType;

	switch (actType) {
	    case "eID-Client":  return ActivationType.EID_CLIENT;
	    case "Browser":     return ActivationType.BROWSER;
	    case "Browser-XHR": return ActivationType.BROWSER_XHR;
	    default: throw new InvalidServerData("Unknown activation type received from server.");
	}
    }

}
