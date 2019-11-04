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

import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import static org.openecard.common.ECardException.makeException;

/**
 *
 * @author Tobias Wich
 */
public class NoSuchSession extends ECardException {

    private static final long serialVersionUID = 1L;

    public NoSuchSession(String message) {
	makeException(this, ECardConstants.Minor.SAL.NO_SESSION, message);
    }

}
