/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace;

import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.i18n.I18N;
import org.openecard.ifd.protocol.pace.gui.GUIContentMap;
import org.openecard.ifd.protocol.pace.gui.PINStep;

import java.util.Locale;


/**
 * Implements a user consent GUI for the PACE protocol.
 *
 * @author Moritz Horsch
 */
public class PACEUserConsent {

    // GUI translation constants
    private static final String USER_CONSENT = "step_pace_userconsent";

    private UserConsent gui;

    /**
     * Creates a new PACEUserConsent.
     *
     * @param gui GUI
     */
    protected PACEUserConsent(UserConsent gui) {
	this.gui = gui;
    }

    /**
     * Shows the user consent.
     *
     * @param content GUI content
     */
    public void show(GUIContentMap content) {
	final UserConsentDescription uc = new UserConsentDescription(
		I18N.strings.INSTANCE.getPace_step_pace_userconsent().localized(Locale.getDefault())
	);


	final PINStep pinStep = new PINStep(content);

	uc.getSteps().add(pinStep.step);

	UserConsentNavigator navigator = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();

	pinStep.processResult(exec.getResults());
    }

}
