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

package org.openecard.gui;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.gui.definition.Step;

/**
 * Class for a step which has a connection handle.
 *
 * @author Tobias Wich
 */
public class StepWithConnection extends Step {

	private final ConnectionHandleType connectionHandle;

	public StepWithConnection(String id, String title, ConnectionHandleType connectionHandle) {
		super(id, title);
		this.connectionHandle = connectionHandle;
	}

	public ConnectionHandleType getConnectionHandle() {
		return connectionHandle;
	}
}