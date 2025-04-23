/****************************************************************************
 * Copyright (C) 2012-2020 ecsec GmbH.
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

import org.openecard.gui.UserConsent
import org.openecard.ws.IFD
import org.openecard.ws.Management
import org.openecard.ws.SAL

/**
 *
 * @author Johannes Schmoelz
 */
interface Environment {
	var gui: UserConsent?

	var ifd: IFD?

	fun addIfdCtx(ctx: ByteArray)

	fun removeIfdCtx(ctx: ByteArray)

	val ifdCtx: List<ByteArray>

	var sal: SAL?

	var eventDispatcher: EventDispatcher?

	var dispatcher: Dispatcher?

	var recognition: CardRecognition?

	var cifProvider: CIFProvider?

	var salSelector: SalSelector?

	fun setGenericComponent(
		id: String,
		component: Any,
	)

	fun getGenericComponent(id: String): Any?

	var management: Management?
}
