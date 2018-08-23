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

import org.openecard.addon.AbstractFactory;
import org.openecard.addon.Context;
import org.openecard.addon.ActionInitializationException;


/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class AppExtensionActionProxy extends AbstractFactory<AppExtensionAction> implements AppExtensionAction {

    private AppExtensionAction c;

    public AppExtensionActionProxy(String implClass, ClassLoader classLoader) {
	super(implClass, classLoader);
    }

    @Override
    public void execute() throws AppExtensionException {
	c.execute();
    }

    @Override 
    public void init(Context ctx) throws ActionInitializationException {
	c = loadInstance(ctx, AppExtensionAction.class);
    }

    @Override
    public void destroy() {
	c.destroy();
    }

}
