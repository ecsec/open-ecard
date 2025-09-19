/****************************************************************************
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.tr03124

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.richclient.sc.CardWatcher
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sc.apdu.command.SecurityCommandResult
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse

class EacProcessState(
	val terminals: Terminals,
	val cardWatcher: CardWatcher,
	val uiStep: UiStep,
) {
	var terminalName: String? = null
	var nativePace: Boolean = false
	var status: SecurityCommandResult? = null
	val selectedChat = uiStep.guiData.requiredChat.copy()

	var paceDid: PaceDid? = null
	var paceResponse: PaceEstablishChannelResponse? = null

	/**
	 * Holds the result of the EAC process, once the process is finished.
	 */
	var bindingResponse: BindingResponse? = null
}
