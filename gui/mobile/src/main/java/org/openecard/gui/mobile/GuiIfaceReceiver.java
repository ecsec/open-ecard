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

package org.openecard.gui.mobile;

import javax.annotation.Nonnull;
import org.openecard.common.util.Promise;


/**
 *
 * @author Tobias Wich
 * @param <T> Type of the UI interface.
 */
public class GuiIfaceReceiver <T> {

    private Promise<T> uiInterface;

    public GuiIfaceReceiver() {
	// always be prepared
	initialise();
    }

    public final synchronized void initialise() {
	// clean promise
	uiInterface = new Promise<>();
    }

    public final synchronized void terminate() {
	// invalidate promise
	if (uiInterface != null) {
	    uiInterface.cancel();
	}
	// and reinitialise
	initialise();
    }

    public Promise<T> getUiInterface() {
	return uiInterface;
    }

    public void setUiInterface(@Nonnull T uiInterface) {
	this.uiInterface.deliver(uiInterface);
    }

}
