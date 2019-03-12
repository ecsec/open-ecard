/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import javax.annotation.Nonnull;
import org.openecard.common.I18n;
import org.openecard.common.anytype.pin.PINCompareMarkerType;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.executor.CancelAction;
import org.openecard.mdlw.sal.enums.PinState;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class PinEntryStep extends Step {

    private static final Logger LOG = LoggerFactory.getLogger(PinEntryStep.class);
    private static final I18n LANG = I18n.getTranslation("pinplugin");

    public static final String STEP_ID = "sal.middleware.pin-compare.gui-entry.pin-step.id";

    protected static final String PIN_FIELD = "PIN_FIELD";

    private final boolean protectedAuthPath;
    private final PINCompareMarkerType pinMarker;
    private final MwSession session;
    private PinState pinState;
    private boolean lastTryFailed = false;
    private boolean pinAuthenticated = false;
    private boolean pinBlocked = false;
    private boolean unkownError = false;

    public PinEntryStep(boolean protectedAuthPath, boolean performContextSpecificLogin,
	    @Nonnull PINCompareMarkerType pinMarker, @Nonnull MwSession session) throws CryptokiException {
	super(STEP_ID);

	this.protectedAuthPath = protectedAuthPath;
	this.pinMarker = pinMarker;
	this.session = session;

	setAction(new PinEntryStepAction(this, performContextSpecificLogin));

	updateState();
    }

    protected MwSession getSession() {
	return session;
    }

    boolean isProtectedAuthPath() {
	return protectedAuthPath;
    }

    void setPinAuthenticated() {
	this.pinAuthenticated = true;
    }

    void setPinBlocked() {
	this.pinBlocked = true;
    }

    void setUnkownError() {
	this.unkownError = true;
    }

    private void generateGui() {
	if (pinBlocked) {
	    setTitle(LANG.translationForKey("action.error.title"));
	    setAction(new CancelAction(this));
	    createBlockedGui();
	    return;
	} else if (unkownError) {
	    setTitle(LANG.translationForKey("action.error.title"));
	    setAction(new CancelAction(this));
	    createErrorGui();
	    return;
	}

	switch (pinState) {
	    case PIN_OK:
	    case PIN_COUNT_LOW:
	    case PIN_FINAL_TRY:
		setTitle(LANG.translationForKey("action.changepin.userconsent.pinstep.title"));
		if (protectedAuthPath) {
		    createPinEntryNativeGui();
		} else {
		    createPinEntryGui();
		}
		break;
	    case PIN_LOCKED:
	    case PIN_NEEDS_CHANGE:
	    case PIN_NOT_INITIALIZED:
		setTitle(LANG.translationForKey("action.error.title"));
		setAction(new CancelAction(this));
		createBlockedGui();
		break;
	    default:
		String msg = "Invalid pin state found.";
		LOG.error(msg);
		throw new IllegalStateException(msg);
	}
    }

    protected void setLastTryFailed() {
	this.lastTryFailed = true;
    }

    protected final void updateState() throws CryptokiException {
	pinState = PinState.getUserPinState(session.getSlot().getTokenInfo());
	setStatusFlags();
	getInputInfoUnits().clear();
	generateGui();
    }

    private void setStatusFlags() {
	switch (pinState) {
	    case PIN_LOCKED:
	    case PIN_NOT_INITIALIZED:
	    case PIN_NEEDS_CHANGE:
		setPinBlocked();
	}
    }

    private void createPinEntryGui() {
	setInstantReturn(false);

	if (lastTryFailed) {
	    addVerifyFailed();
	} else {
	    String desc = LANG.translationForKey("action.pinentry.userconsent.pinstep.enter_pin");
	    Text descText = new Text(desc);
	    getInputInfoUnits().add(descText);
	}

	PasswordField pass = new PasswordField(PIN_FIELD);
	pass.setDescription("PIN");

	// set length restrictions based on DID description. No info means no value set
	PasswordAttributesType pwAttr = pinMarker.getPasswordAttributes();
	if (pwAttr != null) {
	    if (pwAttr.getMinLength() != null) {
		pass.setMinLength(pwAttr.getMinLength().intValue());
	    }
	    if (pwAttr.getMaxLength() != null) {
		pass.setMaxLength(pwAttr.getMaxLength().intValue());
	    }
	}

	getInputInfoUnits().add(pass);

	if (pinState == PinState.PIN_FINAL_TRY) {
	    String noteStr = LANG.translationForKey("action.pinentry.userconsent.pinstep.final_try_note");
	    Text noteText = new Text(noteStr);
	    getInputInfoUnits().add(noteText);
	}
    }

    private void createPinEntryNativeGui() {
	setInstantReturn(true);

	if (lastTryFailed) {
	    addVerifyFailed();
	} else {
	    String desc = LANG.translationForKey("action.pinentry.userconsent.pinstep.enter_pin_term");
	    Text descText = new Text(desc);
	    getInputInfoUnits().add(descText);
	}

	if (pinState == PinState.PIN_FINAL_TRY) {
	    String noteStr = LANG.translationForKey("action.pinentry.userconsent.pinstep.final_try_note");
	    Text noteText = new Text(noteStr);
	    getInputInfoUnits().add(noteText);
	}
    }

    private void createBlockedGui() {
	setInstantReturn(false);

	setReversible(false);
//	if (lastTryFailed) {
//	    addVerifyFailed();
//	}

	String errorStr = LANG.translationForKey("action.changepin.userconsent.errorstep.blocked");
	Text errorText = new Text(errorStr);
	getInputInfoUnits().add(errorText);
    }

    private void createErrorGui() {
	setInstantReturn(false);

	setReversible(false);
//	if (lastTryFailed) {
//	    addVerifyFailed();
//	}

	String errorStr = LANG.translationForKey("action.error.internal");
	Text errorText = new Text(errorStr);
	getInputInfoUnits().add(errorText);
    }

    private void addVerifyFailed() {
	Text incorrectInput = new Text();
	incorrectInput.setText(LANG.translationForKey("action.changepin.userconsent.pinstep.incorrect_input", "PIN"));
	getInputInfoUnits().add(incorrectInput);
    }

    boolean isPinAuthenticated() {
	return pinAuthenticated;
    }

    boolean isPinBlocked() {
	return pinBlocked;
    }

}
