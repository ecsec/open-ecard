/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.gui.android.eac;

import org.openecard.gui.android.eac.types.ServerData;
import org.openecard.gui.android.eac.types.BoxItem;


interface EacGui {

	/**
	 * Gets the server data according to the CertificateDescription data structure.
	 */
	ServerData getServerData();

	/**
	 * Sends the selection made by the user back to the service.
	 * The selection should be made based on the values received in the ServerInfo. The values must not be null.
	 */
	void selectAttributes(in List<BoxItem> readAccessAttr, in List<BoxItem> writeAccessAttr);

	/**
	 * Get status of the PIN on the card.
	 * This method uses the name value of the PinStatus enum type. The enum type has values to check whether CAN must be
	 * entered or whether the PIN is operational.
	 */
	String getPinStatus();

	/**
	 * Enter the PIN and if needed also the CAN.
	 * In case the CAN is not needed, the parameter may be null.
	 * @return rue if PIN is accepted, false otherwise.
	 */
	boolean enterPin(String can, String pin);

	/**
	 * Cancel EAC process.
	 */
	void cancel();

}
