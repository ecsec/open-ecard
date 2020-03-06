/** **************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.scio.AbstractNFCCard;
import org.openecard.scio.NFCCardTerminal;

/**
 *
 * @author Neil Crossley
 */
public class FakeNFCCard extends AbstractNFCCard {

    private final AbstractNFCCard delegate;

    public FakeNFCCard(AbstractNFCCard delegate, NFCCardTerminal terminal) {
	super(terminal);
	this.delegate = delegate;
    }

    @Override
    public boolean isTagPresent() {
	return delegate.isTagPresent();
    }

    @Override
    public boolean terminateTag() throws SCIOException {
	return delegate.terminateTag();
    }

    @Override
    public SCIOATR getATR() {
	return delegate.getATR();
    }

    @Override
    public byte[] transceive(byte[] apdu) throws IOException {
	return delegate.transceive(apdu);
    }

    @Override
    public boolean tagWasPresent() {
	return delegate.tagWasPresent();
    }

}
