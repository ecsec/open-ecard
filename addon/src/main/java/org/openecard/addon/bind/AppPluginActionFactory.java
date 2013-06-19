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

package org.openecard.addon.bind;

import java.util.Map;
import org.openecard.addon.AbstractFactory;
import org.openecard.addon.Context;
import org.openecard.addon.FactoryInitializationException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AppPluginActionFactory  extends AbstractFactory implements AppPluginAction {

    private AppPluginAction c;

    public AppPluginActionFactory(String implClass, ClassLoader classLoader) {
	super(implClass, classLoader);
    }

    public void getActionDescription() {
	throw new UnsupportedOperationException();
    }

    @Override
    public BindingResult execute(Body body, Map<String, String> parameters, Attachment attachments) {
	//TODO use annotations to find the right function
	return c.execute(body, parameters, attachments);
    }

    @Override
    public void destroy() {
	c.destroy();
    }

    @Override
    public void init(Context aCtx) throws FactoryInitializationException {
	c = super.initialize(aCtx, AppPluginAction.class);
    }

}
