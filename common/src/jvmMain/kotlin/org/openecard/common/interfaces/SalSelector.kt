/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
package org.openecard.common.interfaces

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.ws.SAL

/**
 * Interface describing the methods of a class capable of selecting a particular SAL instance.
 *
 * @author Tobias Wich
 */
interface SalSelector {
	fun getSalForCardType(cardType: String): SAL

	fun getSalForProtocol(protocolUri: String): List<SAL>

	fun getSalForHandle(handle: ConnectionHandleType): SAL

	fun getSalForPath(path: CardApplicationPathType): SAL
}
