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

import skid.mob.impl.client.model.UiInfo;
import skid.mob.lib.ProviderInfo;


/**
 *
 * @author Tobias Wich
 */
public class ProviderInfoImpl implements ProviderInfo {

    private final UiInfo uii;

    ProviderInfoImpl(UiInfo uii) {
	this.uii = uii;
    }

    public void load() {
	
    }

    @Override
    public String displayName() {
	return uii.displayName();
    }

    @Override
    public String displayName(String lang) {
	return uii.displayName(lang);
    }

    @Override
    public String informationUrl() {
	return uii.informationUrl();
    }

    @Override
    public String informationUrl(String lang) {
	return uii.informationUrl(lang);
    }

    @Override
    public String privacyStatementUrl() {
	return uii.privacyStatementUrl();
    }

    @Override
    public String privacyStatementUrl(String lang) {
	return uii.privacyStatementUrl(lang);
    }

    @Override
    public String logoUrl() {
	return uii.logoUrl();
    }

    @Override
    public String logoUrl(String lang) {
	return uii.logoUrl(lang);
    }

}
