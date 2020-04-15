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

import skid.mob.lib.NativeHttpClient;
import skid.mob.lib.NativeHttpClientFactory;


/**
 *
 * @author Tobias Wich
 */
public class JavaHttpClientFactory implements NativeHttpClientFactory {

    @Override
    public NativeHttpClient forUrl(String url) {
	return new JavaHttpClient(url);
    }

}
