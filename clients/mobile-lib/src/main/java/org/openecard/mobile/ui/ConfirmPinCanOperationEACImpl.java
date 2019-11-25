/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

import java.util.List;
import org.openecard.common.util.Promise;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.activation.common.anonymous.NFCOverlayMessageHandlerImpl;
import org.openecard.mobile.activation.ConfirmPinCanOperation;


/**
 *
 * @author Tobias Wich
 */
public class ConfirmPinCanOperationEACImpl implements ConfirmPinCanOperation {

    private final Step pinStep;
    private final Promise<List<OutputInfoUnit>> waitForPin;
    private final EacInteraction interaction;
    private final NFCDialogMsgSetter msgSetter;
    private final EacNavigator eacNavigator;

    public ConfirmPinCanOperationEACImpl(EacNavigator eacNavigator, EacInteraction interaction, NFCDialogMsgSetter msgSetter, Step pinStep, Promise<List<OutputInfoUnit>> waitForPin) {
	this.eacNavigator = eacNavigator;
	this.interaction = interaction;
	this.msgSetter = msgSetter;
	this.pinStep = pinStep;
	this.waitForPin = waitForPin;
    }

    @Override
    public void enter(String pin, String can) {
	if (msgSetter.isSupported()) {
	    interaction.requestCardInsertion(new NFCOverlayMessageHandlerImpl(msgSetter));
	} else {
	    interaction.requestCardInsertion();
	}
	List<OutputInfoUnit> outInfo = eacNavigator.getPinResult(pinStep, pin, can);
	eacNavigator.writeBackValues(pinStep.getInputInfoUnits(), outInfo);
	waitForPin.deliver(outInfo);
    }
}
