/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.gui.android.eac;

import android.content.Context;
import java.util.Arrays;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiImplTest {

    @Mocked
    Context androidCtx;
    @Mocked
    UserConsentDescription ucd;
    @Mocked
    EacGuiService anyEacGuiService;

    @Test
    public void testPinOkFirstTime() {
	new Expectations(EacGuiImpl.class) {{
	    anyEacGuiService.getServiceImpl(); result = new EacGuiImpl();
	    ucd.getDialogType(); result = "EAC";
	    ucd.getSteps(); result = createInitialSteps();
	}};

	EacNavigator nav = new EacNavigator(androidCtx, ucd);
    }

    private List<Step> createInitialSteps() {
	Step step1 = new Step("PROTOCOL_EAC_GUI_STEP_CVC");

	return Arrays.asList(step1);
    }

}
