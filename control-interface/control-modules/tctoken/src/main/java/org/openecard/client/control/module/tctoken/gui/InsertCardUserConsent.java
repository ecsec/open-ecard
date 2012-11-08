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
import java.util.Locale;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the insert card user consent dialog.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class InsertCardUserConsent {

    private static final Logger logger = LoggerFactory.getLogger(InsertCardUserConsent.class);
    private final I18n lang = I18n.getTranslation("tctoken");

    private final UserConsent gui;
    private final CardRecognition reg;
    private final ConnectionHandleType conHandle;
    private CardStateMap cardStates;

    /**
     *  Creates a new InsertCardUserConsent.
     * @param gui the UserConsent to show on
     * @param reg to get information out of the card info of the requested card
     * @param conHandle to get the requested card type from
     * @param cardStates card states of the client
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

	UserConsentNavigator ucr = gui.obtainNavigator(createInsertCardUserConsent());
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

    /**
     * Creates the insert card user consent.
     * @return the created user consent description
     */
    private UserConsentDescription createInsertCardUserConsent() {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey("title"));

	// create step
	Step s = new Step("insert-card", lang.translationForKey("step.title"));
	s.setInstantReturn(true);

	// create and add text instructing user
	Text i1 = new Text();
	String cardName = this.getTranslatedCardName();
	i1.setText(lang.translationForKey("step.message", cardName));
	s.getInputInfoUnits().add(i1);

	// add step
	uc.getSteps().add(s);

	return uc;
    }

    /**
     * 
     * @return a card name matching the users locale or the English name as default
     */
    private String getTranslatedCardName() {
	CardInfoType info = reg.getCardInfo(conHandle.getRecognitionInfo().getCardType());

	Locale userLocale = Locale.getDefault();
	String lang = userLocale.getLanguage();
	String enFallback = "";

	for (InternationalStringType typ : info.getCardType().getCardTypeName()) {
	    if (typ.getLang().equalsIgnoreCase("en")) {
		enFallback = typ.getValue();
	    }
	    if (typ.getLang().equalsIgnoreCase(lang)) {
		return typ.getValue();
	    }
	}
	return enFallback;
    }

}
