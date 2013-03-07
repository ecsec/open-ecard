/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.control.module.tctoken;

import generated.TCTokenType;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.openecard.control.module.tctoken.hacks.ObjectTag;
import org.openecard.control.module.tctoken.hacks.PathSecurityParameters;


/**
 * Class to fetch a TCToken and return it as @{link TCTokenType}.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenFactory {

    public static TCTokenType generateTCToken(URL tcTokenURL) throws TCTokenException, IOException {
	// Get TCToken from the given url
	String data = TCTokenGrabber.getResource(tcTokenURL);
	return generateTCToken(data);
    }

    public static TCTokenType generateTCToken(String data) throws TCTokenException {
	// FIXME: Hack
	data = PathSecurityParameters.fix(data);
	// FIXME: Hack
	data = ObjectTag.fix(data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCTokenType> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException("TCToken not available");
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens.get(0));
	ver.verify();

	return tokens.get(0);
    }

}
