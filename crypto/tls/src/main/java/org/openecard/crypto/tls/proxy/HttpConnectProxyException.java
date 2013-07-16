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

package org.openecard.crypto.tls.proxy;

import java.net.SocketException;


/**
 * Exception indicating a failed attempt to open a HTTP proxy tunnel.
 * The exception contains the result code and the textual description of the error.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class HttpConnectProxyException extends SocketException {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final String description;

    public HttpConnectProxyException(String msg, int code, String description) {
	super(msg);
	this.code = code;
	this.description = description;
    }

    public int getCode() {
	return code;
    }

    public String getDescription() {
	return description;
    }

}
