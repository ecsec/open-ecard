/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.fakes;

import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalFactory;

/**
 *
 * @author Neil Crossley
 */
public class FakeMobileNfcTerminalFactory implements TerminalFactory {

    @Override
    public String getType() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SCIOTerminals terminals() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
