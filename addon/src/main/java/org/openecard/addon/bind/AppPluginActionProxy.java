/****************************************************************************
 * Copyright (C) 2013-2016 ecsec GmbH.
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

package org.openecard.addon.bind;

import java.util.List;
import java.util.Map;
import org.openecard.addon.AbstractFactory;
import org.openecard.addon.Context;
import org.openecard.addon.ActionInitializationException;


/**
 * Proxy class wrapping a AppPluginAction.
 * The proxy loads the action and calls the actual execute function of the plug-in implementation. <br>
 * If the plug-in has a custom function and it is found by the proxy, then this one is called directly (not implemented).
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class AppPluginActionProxy  extends AbstractFactory<AppPluginAction> implements AppPluginAction {

    private AppPluginAction c;

    public AppPluginActionProxy(String implClass, ClassLoader classLoader) {
	super(implClass, classLoader);
    }

    public void getActionDescription() {
	throw new UnsupportedOperationException();
    }

    @Override
    public BindingResult execute(RequestBody body, Map<String, String> parameters, Headers headers, List<Attachment> attachments) {
	//TODO use annotations to find the right function
	return c.execute(body, parameters, headers, attachments);
    }

    @Override
    public void init(Context ctx) throws ActionInitializationException {
	c = loadInstance(ctx, AppPluginAction.class);
    }

    @Override
    public void destroy() {
	c.destroy();
    }

}
