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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.util.Promise;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmTwoPasswordsOperation;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import org.openecard.mobile.activation.SelectableItem;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.mobile.activation.common.anonymous.NFCOverlayMessageHandlerImpl;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.anytype.PasswordID;
import org.openecard.sal.protocol.eac.gui.CHATStep;
import org.openecard.sal.protocol.eac.gui.CVCStep;
import org.openecard.sal.protocol.eac.gui.ErrorStep;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.openecard.sal.protocol.eac.gui.PinState;
import org.openecard.sal.protocol.eac.gui.ProcessingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ConfirmTwoPasswordsOperationEACImpl implements ConfirmTwoPasswordsOperation {

    private final Step pinStep;
    private final Promise<List<OutputInfoUnit>> waitForPin;
    private final EacInteraction interaction;
    private final NFCDialogMsgSetter msgSetter;
    private final EacNavigator eacNavigator;

    public ConfirmTwoPasswordsOperationEACImpl(EacNavigator eacNavigator, EacInteraction interaction, NFCDialogMsgSetter msgSetter, Step pinStep, Promise<List<OutputInfoUnit>> waitForPin) {
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
