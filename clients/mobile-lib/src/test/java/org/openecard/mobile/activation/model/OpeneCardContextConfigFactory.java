/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mobile.activation.model;

import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.ws.android.AndroidMarshaller;
import org.openecard.ws.marshal.WSMarshaller;

/**
 *
 * @author Neil Crossley
 */
public final class OpeneCardContextConfigFactory implements Builder<OpeneCardContextConfig> {

    private final String ifdFactoryClass;
    private final String wsdefMarshaller;

    private OpeneCardContextConfigFactory(String ifdFactoryClass, String wsdefMarshaller) {
	this.ifdFactoryClass = ifdFactoryClass;
	this.wsdefMarshaller = wsdefMarshaller;
    }

    public OpeneCardContextConfig create() {
	return new OpeneCardContextConfig(this.ifdFactoryClass, this.wsdefMarshaller);
    }

    @Override
    public OpeneCardContextConfig build() {
	return this.create();
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

    public static OpeneCardContextConfigFactory mobile(TerminalFactory terminalFactory) {
	DelegatingMobileNfcTerminalFactory.setDelegate(terminalFactory);

	return instance().withTerminalFactory(DelegatingMobileNfcTerminalFactory.class)
		.withWsdefMarshaller(AndroidMarshaller.class);
    }
}
