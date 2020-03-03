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

import skid.mob.client.InvalidServerData;
import skid.mob.client.model.AuthOption;
import skid.mob.lib.ActivationType;
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

    OptionImpl(AuthOption ao) {
	this.ao = ao;
    }

    public synchronized void load() throws InvalidServerData {
	if (! loaded) {
	    this.actType = actTypeFromString(ao.activationType());

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
