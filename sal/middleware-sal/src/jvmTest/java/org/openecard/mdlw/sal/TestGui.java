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

package org.openecard.mdlw.sal;

import mockit.Expectations;
import mockit.Mocked;
import org.openecard.common.anytype.pin.PINCompareMarkerType;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.mdlw.sal.enums.Flag;
import org.openecard.mdlw.sal.enums.UserType;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.PinIncorrectException;
import org.testng.annotations.Test;


@Test(groups = "gui")
public class TestGui {

    @Mocked MwSession session;
    @Mocked MwSlot slot;
    @Mocked MwToken token;
    @Mocked PINCompareMarkerType marker;

    @Test
    public void testPinEntry() throws Exception {
	SwingDialogWrapper wrapper = new SwingDialogWrapper();
	SwingUserConsent uc = new SwingUserConsent(wrapper);

	new Expectations() {{
	    token.containsFlag(Flag.CKF_USER_PIN_INITIALIZED); result = true;
	    session.login(UserType.User, null);
	}};

	PinEntryDialog d = new PinEntryDialog(uc, false, marker, session);
	d.show();
    }

    @Test
    public void testNativePinEntry() throws Exception {
	SwingDialogWrapper wrapper = new SwingDialogWrapper();
	SwingUserConsent uc = new SwingUserConsent(wrapper);

	new Expectations() {{
	    token.containsFlag(Flag.CKF_USER_PIN_INITIALIZED); result = true;
	    session.loginExternal(UserType.User);
	}};

	PinEntryDialog d = new PinEntryDialog(uc, true, marker, session);
	d.show();
    }

    @Test
    public void testPinEntryLastTry() throws Exception {
	SwingDialogWrapper wrapper = new SwingDialogWrapper();
	SwingUserConsent uc = new SwingUserConsent(wrapper);

	new Expectations() {{
	    token.containsFlag(Flag.CKF_USER_PIN_INITIALIZED); result = true;
	    token.containsFlag(Flag.CKF_USER_PIN_FINAL_TRY); result = true;
	    session.login(UserType.User, null);
	}};

	PinEntryDialog d = new PinEntryDialog(uc, false, marker, session);
	d.show();
    }

    @Test
    public void testNativePinEntryLastTry() throws Exception {
	SwingDialogWrapper wrapper = new SwingDialogWrapper();
	SwingUserConsent uc = new SwingUserConsent(wrapper);

	new Expectations() {{
	    token.containsFlag(Flag.CKF_USER_PIN_INITIALIZED); result = true;
	    token.containsFlag(Flag.CKF_USER_PIN_FINAL_TRY); result = true;
	    session.loginExternal(UserType.User);
	}};

	PinEntryDialog d = new PinEntryDialog(uc, true, marker, session);
	d.show();
    }

    @Test
    public void testPinEntryFailedFinalTry() throws Exception {
	SwingDialogWrapper wrapper = new SwingDialogWrapper();
	SwingUserConsent uc = new SwingUserConsent(wrapper);

	new Expectations() {{
	    token.containsFlag(Flag.CKF_USER_PIN_INITIALIZED); result = true;
	    token.containsFlag(Flag.CKF_USER_PIN_COUNT_LOW); returns(true, false);
	    token.containsFlag(Flag.CKF_USER_PIN_FINAL_TRY); result = true;
	    session.login(UserType.User, null); result = new PinIncorrectException("PIN entry failed.", 0);
	    result = null;
	}};

	PinEntryDialog d = new PinEntryDialog(uc, false, marker, session);
	d.show();
    }

    @Test
    public void testPinBlocked() throws Exception {
	SwingDialogWrapper wrapper = new SwingDialogWrapper();
	SwingUserConsent uc = new SwingUserConsent(wrapper);

	new Expectations() {{
	    token.containsFlag(Flag.CKF_USER_PIN_INITIALIZED); result = true;
	    token.containsFlag(Flag.CKF_USER_PIN_LOCKED); result = true;
	    session.login(UserType.User, null);
	}};

	PinEntryDialog d = new PinEntryDialog(uc, false, marker, session);
	d.show();
    }

}
