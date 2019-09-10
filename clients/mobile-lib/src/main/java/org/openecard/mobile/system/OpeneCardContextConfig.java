/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.system;

/**
 *
 * @author Neil Crossley
 */
public class OpeneCardContextConfig {

    private final String ifdFactoryClass;
    private final String wsdefMarshallerClass;

    public OpeneCardContextConfig(String ifdFactoryClass, String wsdefMarshallerClass) {
	this.ifdFactoryClass = ifdFactoryClass;
	this.wsdefMarshallerClass = wsdefMarshallerClass;
    }

    public String getIfdFactoryClass() {
	return this.ifdFactoryClass;
    }

    public String getWsdefMarshallerClass() {
	return this.wsdefMarshallerClass;
    }
}
