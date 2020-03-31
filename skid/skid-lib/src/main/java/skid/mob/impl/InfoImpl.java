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

import skid.mob.impl.opt.OptionImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import skid.mob.impl.client.InvalidServerData;
import skid.mob.impl.client.model.AuthOption;
import skid.mob.impl.client.model.SpMetadata;
import skid.mob.lib.Info;
import skid.mob.lib.Option;
import skid.mob.lib.ProviderInfo;


/**
 *
 * @author Tobias Wich
 */
public class InfoImpl implements Info {

    private final SpMetadata spMeta;
    private boolean loaded = false;
    private ProviderInfoImpl pi;
    private List<Option> aos;

    public InfoImpl(SpMetadata spMeta) {
	this.spMeta = spMeta;
    }

    public synchronized void load() throws InvalidServerData {
	if (! loaded) {
	    // load objects
	    pi = new ProviderInfoImpl(spMeta.getUiInfo());
	    pi.load();

	    aos = new ArrayList<>();
	    for (AuthOption next : spMeta.getAuthOptions().list()) {
		OptionImpl opt = new OptionImpl(next);
		opt.load();
		aos.add(opt);
	    }

	    loaded = true;
	}
    }

    @Override
    public ProviderInfo getProviderInfo() {
	return pi;
    }

    @Override
    public List<Option> getOptions() {
	return Collections.unmodifiableList(aos);
    }

}
