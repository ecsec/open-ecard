/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.mobile.ui;


import org.openecard.common.util.Promise;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.mobile.activation.CardLinkInteraction;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.activation.common.anonymous.NFCOverlayMessageHandlerImpl;

import java.util.List;


/**
 * @author Mike Prechtl
 */
public class ConfirmCardLinkCanImpl implements ConfirmPasswordOperation {

	private final Promise<List<OutputInfoUnit>> waitForCan;
	private final CardLinkNavigator cardLinkNavigator;
	private final NFCDialogMsgSetter msgSetter;
	private final CardLinkInteraction interaction;
	private final Step canConfirmStep;

	public ConfirmCardLinkCanImpl(
		Promise<List<OutputInfoUnit>> waitForCan,
		Step step,
		CardLinkInteraction interaction,
		NFCDialogMsgSetter msgSetter,
		CardLinkNavigator cardLinkNavigator
	) {
		this.waitForCan = waitForCan;
		this.canConfirmStep = step;
		this.msgSetter = msgSetter;
		this.interaction = interaction;
		this.cardLinkNavigator = cardLinkNavigator;
	}

	@Override
	public void confirmPassword(String password) {
		if (msgSetter.isSupported()) {
			interaction.requestCardInsertion(new NFCOverlayMessageHandlerImpl(msgSetter));
		} else {
			interaction.requestCardInsertion();
		}

		var outputInfoUnit = cardLinkNavigator.writeBackValues(canConfirmStep, password);
		waitForCan.deliver(outputInfoUnit);
	}
}
