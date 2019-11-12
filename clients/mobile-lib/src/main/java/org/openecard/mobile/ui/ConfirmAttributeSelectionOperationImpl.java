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

import java.util.List;
import org.openecard.common.util.Promise;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.SelectableItem;


/**
 *
 * @author Tobias Wich
 */

public class ConfirmAttributeSelectionOperationImpl implements ConfirmAttributeSelectionOperation {

    private final ServerDataImpl sd;
    private final Promise<List<OutputInfoUnit>> waitForAttributes;

    public ConfirmAttributeSelectionOperationImpl(ServerDataImpl sd, Promise<List<OutputInfoUnit>> waitForAttributes) {
	this.sd = sd;
	this.waitForAttributes = waitForAttributes;
    }

    @Override
    public void enter(List<SelectableItem> readAttr, List<SelectableItem> writeAttr) {
	List<OutputInfoUnit> outInfo = sd.getSelection(readAttr, writeAttr);
	waitForAttributes.deliver(outInfo);
    }
}
