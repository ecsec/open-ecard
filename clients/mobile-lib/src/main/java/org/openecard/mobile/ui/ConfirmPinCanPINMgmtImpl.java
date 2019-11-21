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
import org.openecard.mobile.activation.ConfirmPinCanOperation;

/**
 *
 * @author Florian Otto
 */
public class ConfirmPinCanPINMgmtImpl implements ConfirmPinCanOperation {

    private Promise<List<OutputInfoUnit>> waitForPWD = new Promise<>();
    private String PWD_ID;
    private String CAN_ID;

    public ConfirmPinCanPINMgmtImpl(Promise<List<OutputInfoUnit>> waitForPWD, String PIN_ID, String CAN_ID) {
	this.waitForPWD = waitForPWD;
	this.PWD_ID = PWD_ID;
	this.CAN_ID = CAN_ID;
    }

    @Override
    public void enter(String pin, String can) {
	List<OutputInfoUnit> lst = new ArrayList<>();
	PasswordField pinField = new PasswordField(PWD_ID);
	pinField.setValue(pin.toCharArray());
	lst.add(pinField);
	PasswordField canField = new PasswordField(CAN_ID);
	canField.setValue(can.toCharArray());
	lst.add(canField);
	waitForPWD.deliver(lst);

    }
}