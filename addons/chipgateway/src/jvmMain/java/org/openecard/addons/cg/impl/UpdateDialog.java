/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import java.net.MalformedURLException;
import org.openecard.common.AppVersion;
import org.openecard.common.I18n;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Hyperlink;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;


/**
 *
 * @author Tobias Wich
 */
public class UpdateDialog {

    private static final I18n LANG = I18n.getTranslation("chipgateway");

    private static final String TITLE = "dialog.dl.title";
    private static final String TEXT_REQUIRED = "dialog.dl.text_required";
    private static final String TEXT_OPTIONAL = "dialog.dl.text_optional";
    private static final String TEXT_INSTRUCTIONS = "dialog.dl.text_instructions";

    private final UserConsent gui;
    private final String dlUrl;
    private final boolean updateRequired;
    private final UserConsentDescription ucDesc;

    public UpdateDialog(UserConsent gui, String dlUrl, boolean updateRequired) throws MalformedURLException {
	this.gui = gui;
	this.dlUrl = dlUrl;
	this.updateRequired = updateRequired;
	this.ucDesc = createDialog();
    }

    private UserConsentDescription createDialog() throws MalformedURLException {
	UserConsentDescription uc = new UserConsentDescription(LANG.translationForKey(TITLE), "update_dialog");

	Step s = new Step(LANG.translationForKey(TITLE));
	uc.getSteps().add(s);

	Text t;
	if (updateRequired) {
	    t = new Text(LANG.translationForKey(TEXT_REQUIRED, AppVersion.getName()));
	} else {
	    t = new Text(LANG.translationForKey(TEXT_OPTIONAL, AppVersion.getName()));
	}
	s.getInputInfoUnits().add(t);
	s.getInputInfoUnits().add(new Text(LANG.translationForKey(TEXT_INSTRUCTIONS)));

	Hyperlink link = new Hyperlink();
	link.setHref(dlUrl);
	s.getInputInfoUnits().add(link);

	return uc;
    }

    public void display() {
	UserConsentNavigator nav = gui.obtainNavigator(ucDesc);
	ExecutionEngine ee = new ExecutionEngine(nav);
	ee.process();
    }

}
