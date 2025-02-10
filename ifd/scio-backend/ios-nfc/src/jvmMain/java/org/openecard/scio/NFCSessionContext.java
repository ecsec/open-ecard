/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.scio;

import org.robovm.apple.corenfc.NFCTagReaderSession;
import org.robovm.apple.corenfc.NFCTagReaderSessionDelegateAdapter;

/**
 *
 * @author Neil Crossley
 */
public class NFCSessionContext {
    public final NFCTagReaderSessionDelegateAdapter adapter;
    public final NFCTagReaderSession session;
    public final Object tagLock = new Object();
    
    public NFCSessionContext(NFCTagReaderSessionDelegateAdapter adapter, NFCTagReaderSession session) {
	this.adapter = adapter;
	this.session = session;
    }
}