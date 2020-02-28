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

import skid.mob.client.model.SpMetadata;
import skid.mob.lib.Info;
import skid.mob.lib.ProviderInfo;

/**
 *
 * @author Tobias Wich
 */
public class InfoImpl implements Info {

    private final SpMetadata spMeta;

    InfoImpl(SpMetadata spMeta) {
	this.spMeta = spMeta;
    }

    void load() {
	spMeta.getUiInfo();
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ProviderInfo getProviderInfo() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getOptions() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
