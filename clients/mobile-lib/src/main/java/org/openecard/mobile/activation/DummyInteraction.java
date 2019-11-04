/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mobile.activation;

/**
 *
 * @author Tobias Wich
 */
public class DummyInteraction implements ActivationInteraction {

    @Override
    public void requestCardInsertion() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onCardRecognized() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onCardRecognized(NFCOverlayMessageHandler msgHandler) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onCardRemoved() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
