/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.auth.eac;

import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmPinCanOperation;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import org.openecard.mobile.activation.ServerData;
import skid.mob.lib.SkidEacInteraction;


/**
 *
 * @author Tobias Wich
 */
public class EacInteractionWrapper implements EacInteraction {

    private final SkidEacInteraction eacInteract;

    EacInteractionWrapper(SkidEacInteraction eacInteract) {
	this.eacInteract = eacInteract;
    }

    @Override
    public void onCanRequest(ConfirmPasswordOperation enterCan) {
	eacInteract.onCanRequest(enterCan);
    }

    @Override
    public void onPinRequest(ConfirmPasswordOperation enterPin) {
	eacInteract.onPinRequest(enterPin);
    }

    @Override
    public void onPinRequest(int attempt, ConfirmPasswordOperation enterPin) {
	eacInteract.onPinRequest(attempt, enterPin);
    }

    @Override
    public void onPinCanRequest(ConfirmPinCanOperation enterPinCan) {
	eacInteract.onPinCanRequest(enterPinCan);
    }

    @Override
    public void onCardBlocked() {
	eacInteract.onCardBlocked();
    }

    @Override
    public void onCardDeactivated() {
	eacInteract.onCardDeactivated();
    }

    @Override
    public void onServerData(ServerData data, String transactionData, ConfirmAttributeSelectionOperation selectReadWrite) {
	// ignore this call and just forward the attribute selection as this is already part of the skidentity UI interaction
	selectReadWrite.enterAttributeSelection(data.getReadAccessAttributes(), data.getWriteAccessAttributes());
    }

    @Override
    public void onCardAuthenticationSuccessful() {
	eacInteract.onCardAuthenticationSuccessful();
    }

    @Override
    public void requestCardInsertion() {
	eacInteract.requestCardInsertion();
    }

    @Override
    public void requestCardInsertion(NFCOverlayMessageHandler msgHandler) {
	eacInteract.requestCardInsertion(msgHandler);
    }

    @Override
    public void onCardInteractionComplete() {
	eacInteract.onCardInteractionComplete();
    }

    @Override
    public void onCardRecognized() {
	eacInteract.onCardRecognized();
    }

    @Override
    public void onCardRemoved() {
	eacInteract.onCardRemoved();
    }

}
