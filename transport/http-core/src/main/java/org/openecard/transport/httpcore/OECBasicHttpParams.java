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

package org.openecard.transport.httpcore;

import org.openecard.apache.http.HttpVersion;
import org.openecard.apache.http.params.BasicHttpParams;
import org.openecard.apache.http.params.CoreProtocolPNames;
import org.openecard.common.Version;


/**
 * Extension of BasicHttpParams with useful default values.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OECBasicHttpParams extends BasicHttpParams {

    private static final long serialVersionUID = 1L;

    /**
     * Create an instance with sensible defaults for clients. <br/>
     * The parameters include charset, protocol version and a user agent identifier.
     */
    public OECBasicHttpParams() {
	setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
	setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	setParameter(CoreProtocolPNames.USER_AGENT, "Open-eCard-App/" + Version.getVersion());
    }

}
