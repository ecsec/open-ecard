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

package org.openecard.android.lib.async.tasks;

import org.openecard.android.lib.AppContext;
import org.openecard.android.lib.AppResponse;


/**
 * Contains the app context of the initialization process {@link StartTask} and a response which represents the
 * state of the initialization, for example if the initialization was successful, then a positive response is created.
 *
 * @author Mike Prechtl
 */
public class StartTaskResponse extends TaskResponse {

    private final AppContext ctx;

    public StartTaskResponse(AppContext ctx, AppResponse response) {
	super(response);
	this.ctx = ctx;
    }

    public AppContext getCtx() {
	return ctx;
    }

}
