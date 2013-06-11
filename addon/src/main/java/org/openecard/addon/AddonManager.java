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

package org.openecard.addon;

import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.sal.SALProtocol;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AddonManager {

    public AddonRegistry _unnamed_AddonRegistry_;
    public Context _unnamed_Context_;

    public AddonRegistry getRegistry() {
	throw new UnsupportedOperationException();
    }

    public IFDProtocol getIFDAction(String aUri) {
	throw new UnsupportedOperationException();
    }

    public SALProtocol getSALAction(String aUri) {
	throw new UnsupportedOperationException();
    }

    public AppExtensionAction getAppExtensionAction(String aPluginId, String aActionId) {
	throw new UnsupportedOperationException();
    }

    public AppPluginAction getAppPluginAction(String aPluginId, String aResourceName) {
	throw new UnsupportedOperationException();
    }

}
