/** **************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
package org.openecard.scio;

import org.robovm.apple.ext.corenfc.NFCISO7816Tag;
import org.robovm.apple.ext.corenfc.NFCTagReaderSession;
import org.robovm.apple.ext.corenfc.NFCTagReaderSessionDelegateAdapter;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSObject;

/**
 *
 * @author Florian Otto
 */
public class IOSNFCDelegate extends NFCTagReaderSessionDelegateAdapter {

    private final IOSNFCCard cardObj;

    public IOSNFCDelegate(IOSNFCCard cardObj) {
	super();
	this.cardObj = cardObj;
    }
    @Override
    public void tagReaderSession$didDetectTags$(NFCTagReaderSession session, NSArray<?> tags) {

	for (NSObject t : tags) {
	    session.connectToTag$completionHandler$(t, (NSError er) -> {

		NFCISO7816Tag tag = session.getConnectedTag().asNFCISO7816Tag();
		cardObj.setTag(tag);

	    });
	}
    }

}
