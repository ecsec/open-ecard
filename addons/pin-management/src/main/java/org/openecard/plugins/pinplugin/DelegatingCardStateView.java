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
package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;

/**
 *
 * @author Neil Crossley
 */
public class DelegatingCardStateView implements CardStateView {

    private CardStateView delegate;

    DelegatingCardStateView(CardStateView delegate) {
	this.delegate = delegate;
    }

    @Override
    public ConnectionHandleType getHandle() {
	return this.delegate.getHandle();
    }

    @Override
    public RecognizedState getPinState() {
	return this.delegate.getPinState();
    }

    @Override
    public boolean capturePin() {
	return this.delegate.capturePin();
    }

    @Override
    public boolean isRemoved() {
	return this.delegate.isRemoved();
    }

    @Override
    public boolean isDisconnected() {
	return this.delegate.isDisconnected();
    }
    
    public void setDelegate(CardStateView delegate) {
	this.delegate = delegate;
    }

    public CardStateView getDelegate() {
	return this.delegate;
    }

}
