/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.pkcs11;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestDispatcher {

    private static class TestImpl {

	@PKCS11Dispatchable
	public PKCS11Result C_Initialize(JSONObject obj) {
	    return new PKCS11Result(PKCS11ReturnCode.CKR_OK);
	}

	@PKCS11Dispatchable
	public PKCS11Result C_Finalize(JSONObject obj) {
	    return new PKCS11Result(PKCS11ReturnCode.CKR_OK);
	}

    }

    @Test
    public void testLoad() throws JSONException {
	PKCS11Dispatcher d = new PKCS11Dispatcher(new TestImpl());
	PKCS11Result r = d.dispatch(PKCS11Functions.C_Initialize.name(), "{}");
	Assert.assertEquals(r.resultCode, PKCS11ReturnCode.CKR_OK.code);
    }

}
