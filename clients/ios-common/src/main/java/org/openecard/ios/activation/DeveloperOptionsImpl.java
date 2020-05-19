/** **************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.ios.activation;

import org.openecard.common.util.TR03112Utils;
import org.openecard.ios.logging.JulConfigHelper;
import org.openecard.ios.logging.LogLevel;


/**
 *
 * @author Neil Crossley
 */
public class DeveloperOptionsImpl implements DeveloperOptions {

    @Override
    public void setDebugLogLevel() {
	JulConfigHelper.setLogLevel("", LogLevel.DEBUG);
    }

    @Override
    public void enableTR03112DeveloperMOde() {
	TR03112Utils.DEVELOPER_MODE = true;
    }

}
