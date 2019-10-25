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
public class DuplicateCardEntry extends Exception {

    public DuplicateCardEntry(String msg) {
	super(msg);
    }

    public DuplicateCardEntry(String msg, Throwable cause) {
	super(msg, cause);
    }

}
