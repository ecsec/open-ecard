/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.android.activation;

import android.app.Activity;
import android.app.Dialog;
import java.util.Set;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.mobile.activation.ActivationResult;


/**
 * Implementation of the EacActivationHandler which uses a composite object to provide the missing functions.
 *
 * @author Tobias Wich
 */
public final class CompositeEacActivationHandler extends EacActivationHandler<Activity> {

    private final ActivationCallbackInterface cbObj;

    /**
     * Creates a composite handler using the given composite object.
     *
     * @param a Activity used in the handler.
     * @param cbObj Composite callback object containing the missing functionality.
     */
    public CompositeEacActivationHandler(Activity a, ActivationCallbackInterface cbObj) {
	super(a);
	this.cbObj = cbObj;
    }

    @Override
    public Set<String> getSupportedCards() {
	return cbObj.getSupportedCards();
    }

    @Override
    public void onCardInserted(String cardType) {
	cbObj.onCardInserted(cardType);
    }

    @Override
    public void onGuiIfaceSet(EacGui androidGui) {
	cbObj.onGuiIfaceSet(androidGui);
    }

    @Override
    public void onAuthenticationSuccess(ActivationResult result) {
	cbObj.onAuthenticationSuccess(result);
    }

    @Override
    public void onAuthenticationFailure(ActivationResult result) {
	cbObj.onAuthenticationFailure(result);
    }

    @Override
    public void onAuthenticationInterrupted(ActivationResult result) {
	super.onAuthenticationInterrupted(result);
    }

    @Override
    public Dialog showCardRemoveDialog() throws IllegalStateException {
	// not used in this setting, as it is part of onAuthenticationSuccess
	throw new IllegalStateException("Use of this function is not allowed in this configuration.");
    }

}
