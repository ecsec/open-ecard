/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.control.module.tctoken.gui;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import org.openecard.client.common.I18n;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import org.openecard.client.recognition.CardRecognition;


/**
 * Implements the insert card user consent dialog.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class InsertCardUserConsent {

    // FIXME translation
    // GUI translation constants
    private static final String TITLE = "insert_card_user_consent_title";
    private final I18n lang = I18n.getTranslation("insertCard");

    private final UserConsent gui;

    private CardRecognition reg;

    private ConnectionHandleType conHandle;
    private CardStateMap cardStates;

    /**
     *  Creates a new InsertCardUserConsent.
     * @param gui
     * @param reg
     * @param conHandle
     * @param cardStates
     */
    public InsertCardUserConsent(UserConsent gui, CardRecognition reg, ConnectionHandleType conHandle,
	    CardStateMap cardStates) {
	this.gui = gui;
	this.reg = reg;
	this.conHandle = conHandle;
	this.cardStates = cardStates;
    }

    /**
     * Shows the GUI.
     * 
     * @return the ConnectionHandle of the inserted card or null if no card was inserted
     */
    public ConnectionHandleType show() {
	UserConsentDescription uc = new UserConsentDescription(TITLE);
	// create step
	Step s = new Step("insert-card", "Karte einstecken");
	uc.getSteps().add(s);
	// add text instructing user
	Text i1 = new Text();
	s.getInputInfoUnits().add(i1);
	CardInfoType info = reg.getCardInfo(conHandle.getRecognitionInfo().getCardType());
	String cardName = "";
	for (InternationalStringType typ : info.getCardType().getCardTypeName()) {
	    if (typ.getLang().equalsIgnoreCase("de")) {
		cardName = typ.getValue();
	    }
	}

	i1.setText("Bitte stecken Sie eine Karte vom Typ " + cardName + " in einen angeschlossenen Kartenleser.");

	UserConsentNavigator ucr = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(ucr);
	// add custom insertCardAction to wait for card insertion
	InsertCardStepAction insertCardAction = new InsertCardStepAction(cardStates, "insert-card", conHandle);
	exec.addCustomAction(insertCardAction);
	// run gui
	ResultStatus status = exec.process();

	if (status == ResultStatus.CANCEL) {
	    return null;
	}
	return insertCardAction.getResponse();
    }

}
