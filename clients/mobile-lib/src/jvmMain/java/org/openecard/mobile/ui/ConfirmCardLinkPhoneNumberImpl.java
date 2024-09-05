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
import org.openecard.mobile.activation.ConfirmTextOperation;
import java.util.List;


/**
 * @author Mike Prechtl
 */
public class ConfirmCardLinkPhoneNumberImpl implements ConfirmTextOperation {

	private final Promise<List<OutputInfoUnit>> waitForText;
	private final CardLinkNavigator cardLinkNavigator;
	private final Step phoneNumberStep;

	public ConfirmCardLinkPhoneNumberImpl(Promise<List<OutputInfoUnit>> waitForText, Step step, CardLinkNavigator cardLinkNavigator) {
		this.waitForText = waitForText;
		this.phoneNumberStep = step;
		this.cardLinkNavigator = cardLinkNavigator;
	}

	@Override
	public void confirmText(String text) {
		var outputInfoUnit = cardLinkNavigator.writeBackValues(phoneNumberStep, text);
		waitForText.deliver(outputInfoUnit);
	}
}
