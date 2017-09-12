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

import android.os.RemoteException;
import java.util.List;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.ServerData;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiImpl extends EacGui.Stub {

    @Override
    public ServerData getServerData() throws RemoteException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void selectAttributes(List<BoxItem> readAccessAttr, List<BoxItem> writeAccessAttr) throws RemoteException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean needsCAN() throws RemoteException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enterPin(String can, String pin) throws RemoteException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
