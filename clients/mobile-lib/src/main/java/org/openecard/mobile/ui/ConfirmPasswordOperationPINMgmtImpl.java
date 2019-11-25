/****************************************************************************
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
 ***************************************************************************/
package org.openecard.mobile.ui;

import java.util.ArrayList;
import java.util.List;
import org.openecard.common.util.Promise;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.mobile.activation.ConfirmPasswordOperation;

/**
 *
 * @author Florian Otto
 */
public class ConfirmPasswordOperationPINMgmtImpl implements ConfirmPasswordOperation {

    private final Promise<List<OutputInfoUnit>> waitForPWD;
    private final String PWD_ID;

    public ConfirmPasswordOperationPINMgmtImpl(Promise<List<OutputInfoUnit>> waitForPWD, String PWD_ID) {
	this.waitForPWD = waitForPWD;
	this.PWD_ID = PWD_ID;
    }

    @Override
    public void enter(String password) {
	List<OutputInfoUnit> lst = new ArrayList<>();
	PasswordField pwd = new PasswordField(PWD_ID);
	pwd.setValue(password.toCharArray());
	lst.add(pwd);
	waitForPWD.deliver(lst);

    }
}
