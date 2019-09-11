/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.ws.marshal.WSMarshaller;

/**
 *
 * @author Neil Crossley
 */
public final class OpeneCardContextConfigFactory {

    private final String ifdFactoryClass;
    private final String wsdefMarshaller;

    private OpeneCardContextConfigFactory(String ifdFactoryClass, String wsdefMarshaller) {
	this.ifdFactoryClass = ifdFactoryClass;
	this.wsdefMarshaller = wsdefMarshaller;
    }

    public OpeneCardContextConfig create() {
	return new OpeneCardContextConfig(this.ifdFactoryClass, this.wsdefMarshaller);
    }

    public OpeneCardContextConfigFactory withIdf(String givenIfdFactory) {
	return new OpeneCardContextConfigFactory(givenIfdFactory, this.wsdefMarshaller);
    }

    public <T extends TerminalFactory> OpeneCardContextConfigFactory withTerminalFactory(Class<T> givenIfdFactory) {
	return new OpeneCardContextConfigFactory(givenIfdFactory.getCanonicalName(), this.wsdefMarshaller);
    }

    public <T extends WSMarshaller> OpeneCardContextConfigFactory withWsdefMarshaller(Class<T> givenWsdefMarshaller) {
	return new OpeneCardContextConfigFactory(this.ifdFactoryClass, givenWsdefMarshaller.getCanonicalName());
    }

    public OpeneCardContextConfigFactory withWsdefMarshaller(String givenWsdefMarshaller) {
	return new OpeneCardContextConfigFactory(this.ifdFactoryClass, givenWsdefMarshaller);
    }

    public static OpeneCardContextConfigFactory instance() {
	return new OpeneCardContextConfigFactory("dummy.ifdFactory", "dummy.wsdefMarschaller");
    }
}
