/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard Client.
 *
 * GNU General Public License Usage
 *
 * Open eCard Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open eCard Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Other Usage
 *
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ****************************************************************************/

package org.openecard.client.sal;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ECardException;
import org.openecard.client.common.sal.state.HandlePrinter;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class UnknownConnectionHandle extends ECardException {

    public UnknownConnectionHandle(ConnectionHandleType handle) {
	makeException(this, ECardConstants.Minor.SAL.UNKNOWN_HANDLE, HandlePrinter.printHandle(handle));
    }

}
