/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.state;

/**
 *
 * @author Tobias Wich
 */
class SessionAlreadyExists extends Exception {

    public SessionAlreadyExists(String msg) {
	super(msg);
    }

    public SessionAlreadyExists(String msg, Throwable cause) {
	super(msg, cause);
    }

}
