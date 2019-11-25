/** **************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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
package org.openecard.mobile.ui;

import java.util.ArrayList;
import java.util.List;
import org.openecard.common.util.Promise;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.mobile.activation.ConfirmOldSetNewPasswordOperation;
import org.openecard.plugins.pinplugin.gui.GenericPINStep;

/**
 *
 * @author Florian Otto
 */
public class ConfirmOldSetNewPasswordOperationPINMgmtImpl implements ConfirmOldSetNewPasswordOperation {

    private final Promise<List<OutputInfoUnit>> waitForPIN;

    public ConfirmOldSetNewPasswordOperationPINMgmtImpl(Promise<List<OutputInfoUnit>> waitForPIN) {
	this.waitForPIN = waitForPIN;
    }

    @Override
    public void enter(String oldPassword, String newPassword) {
	List<OutputInfoUnit> lst = new ArrayList<>();
	PasswordField opwd = new PasswordField(GenericPINStep.OLD_PIN_FIELD);
	opwd.setValue(oldPassword.toCharArray());
	PasswordField npwd = new PasswordField(GenericPINStep.NEW_PIN_FIELD);
	npwd.setValue(newPassword.toCharArray());
	PasswordField ncpwd = new PasswordField(GenericPINStep.NEW_PIN_REPEAT_FIELD);
	ncpwd.setValue(newPassword.toCharArray());
	lst.add(opwd);
	lst.add(npwd);
	lst.add(ncpwd);
	waitForPIN.deliver(lst);
    }
}
