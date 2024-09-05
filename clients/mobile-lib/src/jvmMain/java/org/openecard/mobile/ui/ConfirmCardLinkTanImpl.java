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
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import java.util.List;


/**
 * @author Mike Prechtl
 */
public class ConfirmCardLinkTanImpl implements ConfirmPasswordOperation {

	private final Promise<List<OutputInfoUnit>> waitForTan;
	private final CardLinkNavigator cardLinkNavigator;
	private final Step tanConfirmStep;

	public ConfirmCardLinkTanImpl(Promise<List<OutputInfoUnit>> waitForTan, Step step, CardLinkNavigator cardLinkNavigator) {
		this.waitForTan = waitForTan;
		this.tanConfirmStep = step;
		this.cardLinkNavigator = cardLinkNavigator;
	}

	@Override
	public void confirmPassword(String password) {
		var outputInfoUnit = cardLinkNavigator.writeBackValues(tanConfirmStep, password);
		waitForTan.deliver(outputInfoUnit);
	}
}
