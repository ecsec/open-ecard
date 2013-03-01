/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins.testplugin;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.tlv.TLV;
import org.openecard.plugins.PluginAction;


/**
 * An action for testing forbidden use loading of classes named as existing classes.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ExistingClassAction implements PluginAction {

    /**
     * Creates a new ExistingClassAction.
     */
    public ExistingClassAction() {
    }

    @Override
    public String getID() {
	return "";
    }

    @Override
    public String getName() {
	return "ExistingClassAction";
    }

    @Override
    public String getDescription() {
	return "This Action tries to load a class named like an existing class.";
    }

    @Override
    public InputStream getLogo() {
	return null;
    }

    @Override
    public void perform() throws DispatcherException, InvocationTargetException {
	new TLV();
    }

    @Override
    public boolean isMainActivity() {
	return false;
    }

}
