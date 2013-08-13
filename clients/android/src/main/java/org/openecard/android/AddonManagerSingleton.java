/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of SkIDentity.
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.android;

import org.openecard.addon.AddonManager;


/**
 * TODO: revise if we can live without a singleton here.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AddonManagerSingleton {

    private static AddonManager instance;

    public static void setInstance(AddonManager instance) {
	AddonManagerSingleton.instance = instance;
    }

    public static AddonManager getInstance() {
	return instance;
    }

}
