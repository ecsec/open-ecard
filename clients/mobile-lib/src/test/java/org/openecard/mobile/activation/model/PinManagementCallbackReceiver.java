/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmPinCanNewPinOperation;
import org.openecard.mobile.activation.PinManagementInteraction;
import static org.openecard.mobile.activation.model.Timeout.WAIT_TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openecard.mobile.activation.ConfirmPinCanOperation;

/**
 *
 * @author Neil Crossley
 */
public class PinManagementCallbackReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private Promise<ConfirmPinCanOperation> promisedOperationEnterOldNewPassword;
    private Promise<ConfirmPasswordOperation> promisedOperationUnblock;
    private Promise<ConfirmPinCanNewPinOperation> promisedOperationEnterCan;

    private final World world;
    public final PinManagementInteraction interaction;

    public PinManagementCallbackReceiver(World world, PinManagementInteraction interaction) {
	this.world = world;
	this.interaction = interaction;

	promisedOperationEnterOldNewPassword = new Promise<>();
	promisedOperationEnterCan = new Promise<>();
	promisedOperationUnblock = new Promise<>();

	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onPinChangeable().");
	    if (promisedOperationEnterOldNewPassword.isDelivered()) {
		promisedOperationEnterOldNewPassword = new Promise();
	    }
	    promisedOperationEnterOldNewPassword.deliver(arg0.getArgument(1));
	    return null;
	}).when(interaction).onPinChangeable(anyInt(), any());
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onCanRequired().");
	    if (promisedOperationEnterCan.isDelivered()) {
		promisedOperationEnterCan = new Promise();
	    }
	    promisedOperationEnterCan.deliver(arg0.getArgument(0));
	    return null;
	}).when(interaction).onPinCanNewPinRequired(any());
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onCanRequired().");
	    if (promisedOperationUnblock.isDelivered()) {
		promisedOperationUnblock = new Promise();
	    }
	    promisedOperationUnblock.deliver(arg0.getArgument(0));
	    return null;
	}).when(interaction).onPinBlocked(any());
    }

    private void expectPinChangeWithSuccess(String currentPin, String newPin) {
	try {
	    ConfirmPinCanOperation operation = promisedOperationEnterOldNewPassword.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    if (operation == null) {
		throw new IllegalStateException();
	    }
	    operation.enter(currentPin, newPin);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void expectSuccessfulPinChange() {
	expectPinChangeWithSuccess("123123", "123123");
    }

    public void expectIncorrectPinChangeToFail() {
	expectPinChangeWithSuccess("123123", "847826");
    }

}
