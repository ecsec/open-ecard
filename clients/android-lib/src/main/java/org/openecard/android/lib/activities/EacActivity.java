/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.lib.activities;

import android.content.Intent;
import org.openecard.android.lib.AppConstants;
import org.openecard.android.lib.AppResponse;
import org.openecard.gui.android.eac.EacGuiService;


/**
 * @author Mike Prechtl
 */
public abstract class EacActivity extends NfcActivity {

	protected static final int REQUEST_CODE_START = 1;
	protected static final int REQUEST_CODE_TERMINATE = 2;

	protected Intent createGuiIntent() {
		return new Intent(ctx, EacGuiService.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == REQUEST_CODE_START || requestCode == REQUEST_CODE_TERMINATE)  && resultCode == RESULT_OK) {
			AppResponse response = data.getParcelableExtra(AppConstants.INTENT_KEY_FOR_RESPONSE);
			handleAppResponse(response);
		}
	}

	/**
	 * Handle responses from the Open eCard App in the Activity. Implement {@see AppResponseStatusCodes} to
	 * get the available status codes.
	 *
	 * @param response of an API call.
	 */
	public abstract void handleAppResponse(AppResponse response);

}
