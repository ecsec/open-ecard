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

package org.openecard.sal.protocol.eac;

import org.openecard.common.ECardConstants;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.Protocol;
import org.openecard.common.sal.ProtocolFactory;
import org.openecard.gui.UserConsent;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EACProtocolFactory implements ProtocolFactory {

    @Override
    public String getProtocol() {
	return ECardConstants.Protocol.EAC;
    }

    @Override
    public Protocol createInstance(Dispatcher dispatcher, UserConsent gui) {
	return new EACProtocol(dispatcher, gui);
    }

}
