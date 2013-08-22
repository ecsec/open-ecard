/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import java.util.List;
import java.util.Map;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.Body;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PKCS11Action implements AppPluginAction {

    private PKCS11Dispatcher p11Dispatcher;

    @Override
    public BindingResult execute(Body body, Map<String, String> parameters, List<Attachment> attachments) {
	// TODO: get JSON object out of the body and dispatch it with the p11Dispatcher, convert it back afterwards
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Context ctx) throws ActionInitializationException {
	p11Dispatcher = new PKCS11Dispatcher(ctx);
    }

    @Override
    public void destroy() {
	p11Dispatcher = null;
    }

}
